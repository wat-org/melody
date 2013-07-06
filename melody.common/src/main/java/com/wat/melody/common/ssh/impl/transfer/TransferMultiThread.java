package com.wat.melody.common.ssh.impl.transfer;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.ChannelSftp;
import com.wat.melody.common.ex.ConsolidatedException;
import com.wat.melody.common.ex.MelodyInterruptedException;
import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.ssh.Messages;
import com.wat.melody.common.ssh.TemplatingHandler;
import com.wat.melody.common.ssh.filesfinder.Resource;
import com.wat.melody.common.ssh.filesfinder.ResourcesSpecification;
import com.wat.melody.common.ssh.impl.SshSession;
import com.wat.melody.common.ssh.impl.uploader.UploaderMultiThread;

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

	private SshSession _session;
	private List<ResourcesSpecification> _resourcesSpecifications;
	private int _maxPar;
	private List<Resource> _resources;
	private TemplatingHandler _templatingHandler;

	private short _state;
	private ThreadGroup _threadGroup;
	private List<TransferThread> _threads;
	private ConsolidatedException _exceptions;

	public TransferMultiThread(SshSession session,
			List<ResourcesSpecification> r, int maxPar, TemplatingHandler th) {
		/*
		 * TODO : should accept a local and a remote FileSystem (and a transfer
		 * protocol) make operation on it. Doing this, Uploader and Downloader
		 * will be simplier.
		 */
		setSession(session);
		setResourcesSpecifications(r);
		setMaxPar(maxPar);
		setResources(new ArrayList<Resource>());
		setTemplatingHandler(th);

		markState(SUCCEED);
		setThreadGroup(null);
		setThreads(new ArrayList<TransferThread>());
		setExceptions(new ConsolidatedException());
	}

	public void transfer() throws TransferException, InterruptedException {
		// compute resources to transfer
		if (getResourcesSpecifications().size() == 0) {
			return;
		}
		computeResources();
		if (getResources().size() == 0) {
			return;
		}
		// do transfer
		try {
			log.debug(Msg.bind(Messages.TransferMsg_START, getSourceSystem(),
					getDestinationSystem(), getTransferProtocol()));
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
		if (getResources().size() < max) {
			max = getResources().size();
		}
		for (int i = 0; i < max; i++) {
			getThreads().add(new TransferThread(this, i + 1));
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
					getSession().getConnectionDatas()), getExceptions());
		} else if (isFailed()) {
			throw new TransferException(Msg.bind(Messages.TransferEx_MANAGED,
					getSession().getConnectionDatas()), getExceptions());
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

	protected SshSession getSession() {
		return _session;
	}

	/**
	 * @return <tt>null</tt> if there is no more {@link Resource} to transfer.
	 */
	public Resource getNextResourceToTransfer() {
		synchronized (getResources()) {
			if (getResources().size() == 0) {
				return null;
			}
			return getResources().remove(0);
		}
	}

	public void _transfer(ChannelSftp chan, Resource r)
			throws TransferException, InterruptedException {
		try {
			transfer(chan, r);
		} catch (TransferException Ex) {
			TransferException e = new TransferException(Msg.bind(
					Messages.TransferEx_FAILED, r), Ex);
			markState(UploaderMultiThread.FAILED);
			getExceptions().addCause(e);
		}
	}

	public abstract String getThreadName();

	public abstract String getSourceSystem();

	public abstract String getDestinationSystem();

	public abstract String getTransferProtocol();

	public abstract void computeResources() throws TransferException;

	public abstract void transfer(ChannelSftp chan, Resource r)
			throws TransferException, InterruptedException;

	private SshSession setSession(SshSession session) {
		if (session == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + SshSession.class.getCanonicalName()
					+ ".");
		}
		SshSession previous = getSession();
		_session = session;
		return previous;
	}

	protected List<ResourcesSpecification> getResourcesSpecifications() {
		return _resourcesSpecifications;
	}

	private List<ResourcesSpecification> setResourcesSpecifications(
			List<ResourcesSpecification> lrss) {
		if (lrss == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + List.class.getCanonicalName() + "<"
					+ ResourcesSpecification.class.getCanonicalName() + ">.");
		}
		List<ResourcesSpecification> previous = getResourcesSpecifications();
		_resourcesSpecifications = lrss;
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
	 * @return the list of {@link Resource}, computed from this object's
	 *         {@link ResourcesSpecification}.
	 */
	protected List<Resource> getResources() {
		return _resources;
	}

	private List<Resource> setResources(List<Resource> aft) {
		if (aft == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + List.class.getCanonicalName() + "<"
					+ Resource.class.getCanonicalName() + ">.");
		}
		List<Resource> previous = getResources();
		_resources = aft;
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