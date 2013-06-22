package com.wat.melody.common.ssh.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.jcraft.jsch.ChannelSftp;
import com.wat.melody.common.ex.ConsolidatedException;
import com.wat.melody.common.ex.MelodyInterruptedException;
import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.ssh.Messages;
import com.wat.melody.common.ssh.types.ResourceMatcher;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
class DownloaderMultiThread {

	private static Log log = LogFactory.getLog(DownloaderMultiThread.class);

	protected static final short NEW = 16;
	protected static final short RUNNING = 8;
	protected static final short SUCCEED = 0;
	protected static final short FAILED = 1;
	protected static final short INTERRUPTED = 2;
	protected static final short CRITICAL = 4;

	private SshSession _session;
	private List<ResourceMatcher> _resourceMatcherList;
	private int _maxPar;

	private short _state;
	private ThreadGroup _threadGroup;
	private List<DownloaderThread> _threadsList;
	private ConsolidatedException _exceptionsSet;

	protected DownloaderMultiThread(SshSession session,
			List<ResourceMatcher> r, int maxPar) {
		setSession(session);
		setResourceMatcherList(r);
		setMaxPar(maxPar);

		markState(SUCCEED);
		setThreadGroup(null);
		setThreadsList(new ArrayList<DownloaderThread>());
		setExceptionsSet(new ConsolidatedException());
	}

	protected void download() throws DownloaderException, InterruptedException {
		if (getResourceMatcherList().size() == 0) {
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
				getExceptionsSet().addCause(Ex);
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

	protected void download(ChannelSftp channel, ResourceMatcher r) {
		try {
			new DownloaderNoThread(channel, r).download();
		} catch (DownloaderException Ex) {
			DownloaderException e = new DownloaderException(Msg.bind(
					Messages.DownloadEx_FAILED, r), Ex);
			markState(DownloaderMultiThread.FAILED);
			getExceptionsSet().addCause(e);
		}
	}

	private void initializeDownloadThreads() {
		int max = getMaxPar();
		if (getResourceMatcherList().size() < max) {
			max = getResourceMatcherList().size();
		}
		for (int i = 0; i < max; i++) {
			getThreadsList().add(new DownloaderThread(this, i + 1));
		}
	}

	private void startDownloadThreads() throws InterruptedException {
		int threadToLaunchID = getThreadsList().size();
		List<DownloaderThread> runningThreads = new ArrayList<DownloaderThread>();

		while (threadToLaunchID > 0 || runningThreads.size() > 0) {
			// Start ready threads
			while (threadToLaunchID > 0 && runningThreads.size() < getMaxPar()) {
				DownloaderThread ft = getThreadsList().get(--threadToLaunchID);
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
				for (DownloaderThread ft : getThreadsList())
					ft.waitTillProcessingIsDone();
				return;
			} catch (InterruptedException Ex) {
				// If the processing was stopped wait for each thread to end
				if (!isInterrupted()) {
					log.info(Messages.DownloadMsg_GRACEFUL_SHUTDOWN);
				}
				markState(INTERRUPTED);
				getExceptionsSet().addCause(Ex);
			}
		}
		throw new RuntimeException("Fatal error occurred while waiting "
				+ "for " + DownloaderMultiThread.class.getCanonicalName()
				+ " to finish.", getExceptionsSet());
	}

	private void quit() throws DownloaderException, InterruptedException {
		for (DownloaderThread ft : getThreadsList()) {
			markState(ft.getFinalState());
			if (ft.getFinalState() == FAILED || ft.getFinalState() == CRITICAL) {
				getExceptionsSet().addCause(ft.getFinalError());
			}
		}

		if (isCritical()) {
			throw new DownloaderException(Msg.bind(
					Messages.DownloadEx_UNMANAGED, getSession()
							.getConnectionDatas()), getExceptionsSet());
		} else if (isFailed()) {
			throw new DownloaderException(Msg.bind(Messages.DownloadEx_MANAGED,
					getSession().getConnectionDatas()), getExceptionsSet());
		} else if (isInterrupted()) {
			throw new MelodyInterruptedException(
					Messages.DownloadEx_INTERRUPTED, getExceptionsSet());
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

	protected List<ResourceMatcher> getResourceMatcherList() {
		return _resourceMatcherList;
	}

	private List<ResourceMatcher> setResourceMatcherList(
			List<ResourceMatcher> aft) {
		if (aft == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + List.class.getCanonicalName() + "<"
					+ ResourceMatcher.class.getCanonicalName() + ">.");
		}
		List<ResourceMatcher> previous = getResourceMatcherList();
		_resourceMatcherList = aft;
		return previous;
	}

	protected int getMaxPar() {
		return _maxPar;
	}

	/**
	 * @param iMaxPar
	 *            is the maximum number of {@link DownloaderThread} this object
	 *            can run simultaneously.
	 * 
	 * @throws ForeachException
	 *             if the given value is not >= 1 and < 10.
	 */
	private int setMaxPar(int iMaxPar) {
		if (iMaxPar < 1) {
			iMaxPar = 1; // security
		} else if (iMaxPar > 10) {
			iMaxPar = 10; // maximum number of opened JSch channel
		}
		int previous = getMaxPar();
		_maxPar = iMaxPar;
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
	private List<DownloaderThread> getThreadsList() {
		return _threadsList;
	}

	private List<DownloaderThread> setThreadsList(List<DownloaderThread> aft) {
		if (aft == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + List.class.getCanonicalName() + "<"
					+ DownloaderThread.class.getCanonicalName() + ">.");
		}
		List<DownloaderThread> previous = getThreadsList();
		_threadsList = aft;
		return previous;
	}

	/**
	 * @return the exceptions that append during the download.
	 */
	private ConsolidatedException getExceptionsSet() {
		return _exceptionsSet;
	}

	private ConsolidatedException setExceptionsSet(ConsolidatedException cex) {
		if (cex == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ ConsolidatedException.class.getCanonicalName() + ".");
		}
		ConsolidatedException previous = getExceptionsSet();
		_exceptionsSet = cex;
		return previous;
	}

}