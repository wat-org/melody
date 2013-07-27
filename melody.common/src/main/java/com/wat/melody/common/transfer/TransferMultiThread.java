package com.wat.melody.common.transfer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wat.melody.common.ex.ConsolidatedException;
import com.wat.melody.common.ex.MelodyInterruptedException;
import com.wat.melody.common.files.FileSystem;
import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.transfer.exception.TransferException;
import com.wat.melody.common.transfer.finder.TransferableFilesIterator;
import com.wat.melody.common.transfer.finder.TransferablesFinder;
import com.wat.melody.common.transfer.finder.TransferablesTree;
import com.wat.melody.common.transfer.resources.ResourcesSpecification;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class TransferMultiThread {

	private static Logger log = LoggerFactory
			.getLogger(TransferMultiThread.class);

	protected static final short NEW = 16;
	protected static final short RUNNING = 8;
	protected static final short SUCCEED = 0;
	protected static final short FAILED = 1;
	protected static final short INTERRUPTED = 2;
	protected static final short CRITICAL = 4;

	private List<ResourcesSpecification> _resourcesSpecifications;
	private int _maxPar;
	private TransferablesTree _transferables;
	private TransferableFilesIterator _filesIterator;
	private TemplatingHandler _templatingHandler;

	private short _state;
	private ThreadGroup _threadGroup;
	private List<TransferThread> _threads;
	private ConsolidatedException _exceptions;

	public TransferMultiThread(List<ResourcesSpecification> rss, int maxPar,
			TemplatingHandler th) {
		setResourcesSpecifications(rss);
		setMaxPar(maxPar);
		setTemplatingHandler(th);

		markState(SUCCEED);
		setThreadGroup(null);
		setThreads(new ArrayList<TransferThread>());
		setExceptions(new ConsolidatedException());
	}

	public void transfer() throws TransferException, InterruptedException {
		if (getResourcesSpecifications().size() == 0) {
			return;
		}
		// compute transferables to transfer
		computeTransferables();
		if (getTransferablesTree().getDirectoriesCount() == 0) {
			return;
		}
		// do transfer
		try {
			log.debug(Msg.bind(Messages.TransferMsg_START,
					getSourceSystemDescription(),
					getDestinationSystemDescription(),
					getTransferProtocolDescription()));
			// create destination directories first
			createDestinationDirectories();
			// then launch multi-thread file transfer
			setThreadGroup(new ThreadGroup(Thread.currentThread().getName()
					+ ">" + getThreadName()));
			getThreadGroup().setDaemon(true);
			initializeTransferThreads();
			try {
				startTransferThreads();
			} catch (InterruptedException Ex) {
				markState(INTERRUPTED);
			} catch (Throwable Ex) {
				getExceptions().addCause(Ex);
				markState(CRITICAL);
			} finally {
				// If an error occurred while starting thread, some thread may
				// have been launched without any problem
				// We must wait for these threads to die
				waitForTransferThreadsToBeDone();
				quit();
				log.debug(Messages.TransferMsg_FINISH);
			}
		} finally {
			// This allow the doProcessing method to be called multiple time
			// (will certainly be useful someday)
			setThreadGroup(null);
		}
	}

	private void initializeTransferThreads() {
		int max = getMaxPar();
		int filesCount = getTransferablesTree().getAllFilesCount();
		if (filesCount < max) {
			max = filesCount;
		}
		for (int i = 0; i < max; i++) {
			getThreads().add(
					new TransferThread(this, newSourceFileSystem(),
							newDestinationFileSystem(), i + 1));
		}
	}

	private void startTransferThreads() throws InterruptedException {
		int threadToLaunchID = getThreads().size();
		List<TransferThread> runningThreads = new ArrayList<TransferThread>();

		while (threadToLaunchID > 0 || runningThreads.size() > 0) {
			// Start ready threads
			while (threadToLaunchID > 0 && runningThreads.size() < getMaxPar()) {
				TransferThread ft = getThreads().get(--threadToLaunchID);
				runningThreads.add(ft);
				ft.startProcessing();
			}
			// Sleep a little
			if (runningThreads.size() > 0)
				runningThreads.get(0).waitTillProcessingIsDone(50);
			// Remove ended threads
			for (int i = runningThreads.size() - 1; i >= 0; i--) {
				TransferThread ft = runningThreads.get(i);
				if (ft.getFinalState() != NEW && ft.getFinalState() != RUNNING)
					runningThreads.remove(ft);
			}
		}
	}

	private void waitForTransferThreadsToBeDone() {
		int nbTry = 2;
		while (nbTry-- > 0) {
			try {
				for (TransferThread ft : getThreads())
					ft.waitTillProcessingIsDone();
				return;
			} catch (InterruptedException Ex) {
				// If the processing was stopped wait for each thread to end
				if (!isInterrupted()) {
					log.info(Messages.TransferMsg_GRACEFUL_SHUTDOWN);
				}
				markState(INTERRUPTED);
				getExceptions().addCause(Ex);
			}
		}
		throw new RuntimeException("Fatal error occurred while waiting "
				+ "for " + TransferMultiThread.class.getCanonicalName()
				+ " to finish.", getExceptions());
	}

	private void quit() throws TransferException, InterruptedException {
		for (TransferThread ft : getThreads()) {
			markState(ft.getFinalState());
			if (ft.getFinalState() == FAILED || ft.getFinalState() == CRITICAL) {
				getExceptions().addCause(ft.getFinalError());
			}
		}

		if (isCritical()) {
			throw new TransferException(Msg.bind(Messages.TransferEx_UNMANAGED,
					getSourceSystemDescription(),
					getDestinationSystemDescription(),
					getTransferProtocolDescription()), getExceptions());
		} else if (isFailed()) {
			throw new TransferException(Msg.bind(Messages.TransferEx_MANAGED,
					getSourceSystemDescription(),
					getDestinationSystemDescription(),
					getTransferProtocolDescription()), getExceptions());
		} else if (isInterrupted()) {
			throw new MelodyInterruptedException(
					Messages.TransferEx_INTERRUPTED, getExceptions());
		}
	}

	protected short markState(short state) {
		return _state |= state;
	}

	private boolean isFailed() {
		return FAILED == (_state & FAILED);
	}

	private boolean isInterrupted() {
		return INTERRUPTED == (_state & INTERRUPTED);
	}

	private boolean isCritical() {
		return CRITICAL == (_state & CRITICAL);
	}

	protected void computeTransferables() throws TransferException {
		FileSystem sfs = null;
		try {
			sfs = newSourceFileSystem();
			try {
				setTransferablesTree(TransferablesFinder.find(sfs,
						getResourcesSpecifications()));
			} catch (IOException Ex) {
				throw new TransferException(Msg.bind(
						Messages.TransferEx_FAILED_TO_FIND_TRANSFERABLES,
						getSourceSystemDescription()), Ex);
			}
		} catch (TransferException Ex) {
			throw new TransferException(Msg.bind(Messages.TransferEx_MANAGED,
					getSourceSystemDescription(),
					getDestinationSystemDescription(),
					getTransferProtocolDescription()), Ex);
		} catch (Throwable Ex) {
			throw new TransferException(Msg.bind(Messages.TransferEx_UNMANAGED,
					getSourceSystemDescription(),
					getDestinationSystemDescription(),
					getTransferProtocolDescription()), Ex);
		} finally {
			if (sfs != null) {
				sfs.release();
				sfs = null;
			}
		}
		System.out.println(getTransferablesTree());
	}

	protected void createDestinationDirectories() throws TransferException {
		FileSystem dfs = null;
		try {
			dfs = newDestinationFileSystem();
			for (Transferable t : getTransferablesTree().getAllDirectories()) {
				log.debug(Msg.bind(Messages.TransferMsg_BEGIN, t));
				try {
					if (!t.linkShouldBeConvertedToFile()) {
						TransferHelper.createSymbolicLink(dfs,
								t.getDestinationPath(),
								t.getSymbolicLinkTarget(),
								t.getExpectedAttributes());
					} else {
						TransferHelper.createDirectory(dfs,
								t.getDestinationPath(),
								t.getExpectedAttributes());
					}
				} catch (IOException Ex) {
					throw new TransferException(Msg.bind(
							Messages.TransferEx_FAILED, t), Ex);
				}
				log.info(Msg.bind(Messages.TransferMsg_END, t));
			}
		} catch (TransferException Ex) {
			throw new TransferException(Msg.bind(Messages.TransferEx_MANAGED,
					getSourceSystemDescription(),
					getDestinationSystemDescription(),
					getTransferProtocolDescription()), Ex);
		} catch (Throwable Ex) {
			throw new TransferException(Msg.bind(Messages.TransferEx_UNMANAGED,
					getSourceSystemDescription(),
					getDestinationSystemDescription(),
					getTransferProtocolDescription()), Ex);
		} finally {
			if (dfs != null) {
				dfs.release();
				dfs = null;
			}
		}
		// store iterator on all files to transfer
		_filesIterator = getTransferablesTree().getAllFiles();
	}

	/**
	 * @return <tt>null</tt> if there is no more {@link transferable} to
	 *         transfer.
	 */
	public Transferable getNextTransferable() {
		try {
			return _filesIterator.next();
		} catch (NoSuchElementException ignored) {
			return null;
		}
	}

	public void _transfer(FileSystem sourceFileSystem,
			FileSystem destinationFileSystem, Transferable t)
			throws TransferException, InterruptedException {
		try {
			newTransferNoThread(sourceFileSystem, destinationFileSystem, t)
					.doTransfer();
		} catch (TransferException Ex) {
			TransferException e = new TransferException(Msg.bind(
					Messages.TransferEx_FAILED, t), Ex);
			markState(FAILED);
			getExceptions().addCause(e);
		}
	}

	public abstract String getThreadName();

	public abstract FileSystem newSourceFileSystem();

	public abstract FileSystem newDestinationFileSystem();

	public abstract String getSourceSystemDescription();

	public abstract String getDestinationSystemDescription();

	public abstract String getTransferProtocolDescription();

	public abstract TransferNoThread newTransferNoThread(
			FileSystem sourceFileSystem, FileSystem destinationFileSystem,
			Transferable t);

	protected List<ResourcesSpecification> getResourcesSpecifications() {
		return _resourcesSpecifications;
	}

	private List<ResourcesSpecification> setResourcesSpecifications(
			List<ResourcesSpecification> rss) {
		if (rss == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + List.class.getCanonicalName() + "<"
					+ ResourcesSpecification.class.getCanonicalName() + ">.");
		}
		List<ResourcesSpecification> previous = getResourcesSpecifications();
		_resourcesSpecifications = rss;
		return previous;
	}

	protected int getMaxPar() {
		return _maxPar;
	}

	/**
	 * @param maxPar
	 *            is the maximum number of {@link TransferThread} this object
	 *            can run simultaneously.
	 * 
	 * @throws ForeachException
	 *             if the given value is not >= 1 and < 10.
	 */
	private int setMaxPar(int maxPar) {
		if (maxPar < 1) {
			maxPar = 1; // security
		} else if (maxPar > 10) {
			maxPar = 10; // maximum number of opened JSch channel
		}
		int previous = getMaxPar();
		_maxPar = maxPar;
		return previous;
	}

	/**
	 * @return the tree of {@link Transferable}, computed from this object's
	 *         {@link ResourcesSpecification}s.
	 */
	protected TransferablesTree getTransferablesTree() {
		return _transferables;
	}

	private TransferablesTree setTransferablesTree(TransferablesTree tt) {
		if (tt == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + List.class.getCanonicalName() + "<"
					+ Transferable.class.getCanonicalName() + ">.");
		}
		TransferablesTree previous = getTransferablesTree();
		_transferables = tt;
		return previous;
	}

	protected TemplatingHandler getTemplatingHandler() {
		return _templatingHandler;
	}

	private TemplatingHandler setTemplatingHandler(TemplatingHandler th) {
		TemplatingHandler previous = getTemplatingHandler();
		_templatingHandler = th;
		return previous;
	}

	/**
	 * @return the {@link ThreadGroup} which holds all {@link ForeachThread}
	 *         managed by this object.
	 */
	protected ThreadGroup getThreadGroup() {
		return _threadGroup;
	}

	private ThreadGroup setThreadGroup(ThreadGroup tg) {
		// Can be null
		ThreadGroup previous = getThreadGroup();
		_threadGroup = tg;
		return previous;
	}

	/**
	 * @return all the {@link TransferThread} managed by this object.
	 */
	private List<TransferThread> getThreads() {
		return _threads;
	}

	private List<TransferThread> setThreads(List<TransferThread> aft) {
		if (aft == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + List.class.getCanonicalName() + "<"
					+ TransferThread.class.getCanonicalName() + ">.");
		}
		List<TransferThread> previous = getThreads();
		_threads = aft;
		return previous;
	}

	/**
	 * @return the exceptions that append during the transfer.
	 */
	private ConsolidatedException getExceptions() {
		return _exceptions;
	}

	private ConsolidatedException setExceptions(ConsolidatedException cex) {
		if (cex == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ ConsolidatedException.class.getCanonicalName() + ".");
		}
		ConsolidatedException previous = getExceptions();
		_exceptions = cex;
		return previous;
	}

}