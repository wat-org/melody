package com.wat.melody.common.ssh.impl.downloader;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.jcraft.jsch.ChannelSftp;
import com.wat.melody.common.ex.ConsolidatedException;
import com.wat.melody.common.ex.MelodyInterruptedException;
import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.ssh.Messages;
import com.wat.melody.common.ssh.filesfinder.RemoteResource;
import com.wat.melody.common.ssh.filesfinder.RemoteResourcesFinder;
import com.wat.melody.common.ssh.filesfinder.RemoteResourcesSpecification;
import com.wat.melody.common.ssh.filesfinder.ResourcesSpecification;
import com.wat.melody.common.ssh.impl.SshSession;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class DownloaderMultiThread {

	private static Log log = LogFactory.getLog(DownloaderMultiThread.class);

	protected static final short NEW = 16;
	protected static final short RUNNING = 8;
	protected static final short SUCCEED = 0;
	protected static final short FAILED = 1;
	protected static final short INTERRUPTED = 2;
	protected static final short CRITICAL = 4;

	private SshSession _session;
	private List<RemoteResourcesSpecification> _remoteResourcesSpecifications;
	private int _maxPar;
	private List<RemoteResource> _remoteResources;

	private short _state;
	private ThreadGroup _threadGroup;
	private List<DownloaderThread> _threads;
	private ConsolidatedException _exceptions;

	public DownloaderMultiThread(SshSession session,
			List<RemoteResourcesSpecification> r, int maxPar) {
		setSession(session);
		setResourcesSpecifications(r);
		setMaxPar(maxPar);
		setRemoteResources(new ArrayList<RemoteResource>());

		markState(SUCCEED);
		setThreadGroup(null);
		setThreads(new ArrayList<DownloaderThread>());
		setExceptions(new ConsolidatedException());
	}

	public void download() throws DownloaderException, InterruptedException {
		if (getResourcesSpecifications().size() == 0) {
			return;
		}
		computeRemoteResources();
		if (getRemoteResources().size() == 0) {
			return;
		}
		try {
			log.debug(Msg.bind(Messages.DownloadMsg_START, getSession()
					.getConnectionDatas()));
			setThreadGroup(new ThreadGroup(Thread.currentThread().getName()
					+ ">downloader"));
			getThreadGroup().setDaemon(true);
			initializeDownloadThreads();
			try {
				startDownloadThreads();
			} catch (InterruptedException Ex) {
				markState(INTERRUPTED);
			} catch (Throwable Ex) {
				getExceptions().addCause(Ex);
				markState(CRITICAL);
			} finally {
				// If an error occurred while starting thread, some thread may
				// have been launched without any problem
				// We must wait for these threads to die
				waitForDownloadThreadsToBeDone();
				quit();
				log.debug(Messages.DownloadMsg_FINISH);
			}
		} finally {
			// This allow the doProcessing method to be called multiple time
			// (will certainly be useful someday)
			setThreadGroup(null);
		}
	}

	private void computeRemoteResources() throws DownloaderException {
		ChannelSftp chan = null;
		try {
			chan = getSession().openSftpChannel();
			for (ResourcesSpecification rs : getResourcesSpecifications()) {
				List<RemoteResource> rrs;
				rrs = RemoteResourcesFinder.findResources(chan, rs);
				getRemoteResources().removeAll(rrs); // remove duplicated
				getRemoteResources().addAll(rrs);
			}
		} finally {
			if (chan != null) {
				chan.disconnect();
			}
		}
		for (RemoteResource r : getRemoteResources()) {
			System.out.println(r);
		}
	}

	protected void download(ChannelSftp channel, RemoteResource rr) {
		try {
			new DownloaderNoThread(channel, rr).download();
		} catch (DownloaderException Ex) {
			DownloaderException e = new DownloaderException(Msg.bind(
					Messages.DownloadEx_FAILED, rr), Ex);
			markState(DownloaderMultiThread.FAILED);
			getExceptions().addCause(e);
		}
	}

	private void initializeDownloadThreads() {
		int max = getMaxPar();
		if (getRemoteResources().size() < max) {
			max = getRemoteResources().size();
		}
		for (int i = 0; i < max; i++) {
			getThreads().add(new DownloaderThread(this, i + 1));
		}
	}

	private void startDownloadThreads() throws InterruptedException {
		int threadToLaunchID = getThreads().size();
		List<DownloaderThread> runningThreads = new ArrayList<DownloaderThread>();

		while (threadToLaunchID > 0 || runningThreads.size() > 0) {
			// Start ready threads
			while (threadToLaunchID > 0 && runningThreads.size() < getMaxPar()) {
				DownloaderThread ft = getThreads().get(--threadToLaunchID);
				runningThreads.add(ft);
				ft.startProcessing();
			}
			// Sleep a little
			if (runningThreads.size() > 0)
				runningThreads.get(0).waitTillProcessingIsDone(50);
			// Remove ended threads
			for (int i = runningThreads.size() - 1; i >= 0; i--) {
				DownloaderThread ft = runningThreads.get(i);
				if (ft.getFinalState() != NEW && ft.getFinalState() != RUNNING)
					runningThreads.remove(ft);
			}
		}
	}

	private void waitForDownloadThreadsToBeDone() {
		int nbTry = 2;
		while (nbTry-- > 0) {
			try {
				for (DownloaderThread ft : getThreads())
					ft.waitTillProcessingIsDone();
				return;
			} catch (InterruptedException Ex) {
				// If the processing was stopped wait for each thread to end
				if (!isInterrupted()) {
					log.info(Messages.DownloadMsg_GRACEFUL_SHUTDOWN);
				}
				markState(INTERRUPTED);
				getExceptions().addCause(Ex);
			}
		}
		throw new RuntimeException("Fatal error occurred while waiting "
				+ "for " + DownloaderMultiThread.class.getCanonicalName()
				+ " to finish.", getExceptions());
	}

	private void quit() throws DownloaderException, InterruptedException {
		for (DownloaderThread ft : getThreads()) {
			markState(ft.getFinalState());
			if (ft.getFinalState() == FAILED || ft.getFinalState() == CRITICAL) {
				getExceptions().addCause(ft.getFinalError());
			}
		}

		if (isCritical()) {
			throw new DownloaderException(Msg.bind(
					Messages.DownloadEx_UNMANAGED, getSession()
							.getConnectionDatas()), getExceptions());
		} else if (isFailed()) {
			throw new DownloaderException(Msg.bind(Messages.DownloadEx_MANAGED,
					getSession().getConnectionDatas()), getExceptions());
		} else if (isInterrupted()) {
			throw new MelodyInterruptedException(
					Messages.DownloadEx_INTERRUPTED, getExceptions());
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

	protected List<RemoteResourcesSpecification> getResourcesSpecifications() {
		return _remoteResourcesSpecifications;
	}

	private List<RemoteResourcesSpecification> setResourcesSpecifications(
			List<RemoteResourcesSpecification> rrss) {
		if (rrss == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + List.class.getCanonicalName() + "<"
					+ RemoteResourcesSpecification.class.getCanonicalName()
					+ ">.");
		}
		List<RemoteResourcesSpecification> previous = getResourcesSpecifications();
		_remoteResourcesSpecifications = rrss;
		return previous;
	}

	protected int getMaxPar() {
		return _maxPar;
	}

	/**
	 * @param maxPar
	 *            is the maximum number of {@link DownloaderThread} this object
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
	 * @return the list of {@link RemoteResource}, computed from this object's
	 *         {@link ResourcesSpecification}.
	 */
	protected List<RemoteResource> getRemoteResources() {
		return _remoteResources;
	}

	private List<RemoteResource> setRemoteResources(List<RemoteResource> aft) {
		if (aft == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + List.class.getCanonicalName() + "<"
					+ RemoteResource.class.getCanonicalName() + ">.");
		}
		List<RemoteResource> previous = getRemoteResources();
		_remoteResources = aft;
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
	 * @return all the {@link DownloaderThread} managed by this object.
	 */
	private List<DownloaderThread> getThreads() {
		return _threads;
	}

	private List<DownloaderThread> setThreads(List<DownloaderThread> aft) {
		if (aft == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + List.class.getCanonicalName() + "<"
					+ DownloaderThread.class.getCanonicalName() + ">.");
		}
		List<DownloaderThread> previous = getThreads();
		_threads = aft;
		return previous;
	}

	/**
	 * @return the exceptions that append during the download.
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