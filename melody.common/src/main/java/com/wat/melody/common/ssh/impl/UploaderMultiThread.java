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
import com.wat.melody.common.ssh.TemplatingHandler;
import com.wat.melody.common.ssh.types.SimpleResource;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
class UploaderMultiThread {

	private static Log log = LogFactory.getLog(UploaderMultiThread.class);

	protected static final short NEW = 16;
	protected static final short RUNNING = 8;
	protected static final short SUCCEED = 0;
	protected static final short FAILED = 1;
	protected static final short INTERRUPTED = 2;
	protected static final short CRITICAL = 4;

	private SshSession _session;
	private List<SimpleResource> _simpleResourcesList;
	private int _maxPar;
	private TemplatingHandler _templatingHandler;

	private short _state;
	private ThreadGroup _threadGroup;
	private List<UploaderThread> _threadsList;
	private ConsolidatedException _exceptionsSet;

	protected UploaderMultiThread(SshSession session, List<SimpleResource> r,
			int maxPar, TemplatingHandler th) {
		setSession(session);
		setSimpleResourcesList(r);
		setMaxPar(maxPar);
		setTemplatingHandler(th);

		markState(SUCCEED);
		setThreadGroup(null);
		setThreadsList(new ArrayList<UploaderThread>());
		setExceptionsSet(new ConsolidatedException());
	}

	protected void upload() throws UploaderException, InterruptedException {
		if (getSimpleResourcesList().size() == 0) {
			return;
		}
		try {
			log.debug(Msg.bind(Messages.UploadMsg_START, getSession()
					.getConnectionDatas()));
			setThreadGroup(new ThreadGroup(Thread.currentThread().getName()
					+ ">uploader"));
			getThreadGroup().setDaemon(true);
			initializeUploadThreads();
			try {
				startUploadThreads();
			} catch (InterruptedException Ex) {
				markState(INTERRUPTED);
			} catch (Throwable Ex) {
				getExceptionsSet().addCause(Ex);
				markState(CRITICAL);
			} finally {
				// If an error occurred while starting thread, some thread may
				// have been launched without any problem
				// We must wait for these threads to die
				waitForUploadThreadsToBeDone();
				quit();
				log.debug(Messages.UploadMsg_FINISH);
			}
		} finally {
			// This allow the doProcessing method to be called multiple time
			// (will certainly be useful someday)
			setThreadGroup(null);
		}
	}

	protected void upload(ChannelSftp channel, SimpleResource r) {
		try {
			new UploaderNoThread(channel, r, getTemplatingHandler()).upload();
		} catch (UploaderException Ex) {
			UploaderException e = new UploaderException(Msg.bind(
					Messages.UploadEx_FAILED, r), Ex);
			markState(UploaderMultiThread.FAILED);
			getExceptionsSet().addCause(e);
		}
	}

	private void initializeUploadThreads() {
		for (int i = 0; i < getMaxPar(); i++) {
			getThreadsList().add(new UploaderThread(this, i + 1));
		}
	}

	private void startUploadThreads() throws InterruptedException {
		int threadToLaunchID = getThreadsList().size();
		List<UploaderThread> runningThreads = new ArrayList<UploaderThread>();

		while (threadToLaunchID > 0 || runningThreads.size() > 0) {
			// Start ready threads
			while (threadToLaunchID > 0 && runningThreads.size() < getMaxPar()) {
				UploaderThread ft = getThreadsList().get(--threadToLaunchID);
				runningThreads.add(ft);
				ft.startProcessing();
			}
			// Sleep a little
			if (runningThreads.size() > 0)
				runningThreads.get(0).waitTillProcessingIsDone(50);
			// Remove ended threads
			for (int i = runningThreads.size() - 1; i >= 0; i--) {
				UploaderThread ft = runningThreads.get(i);
				if (ft.getFinalState() != NEW && ft.getFinalState() != RUNNING)
					runningThreads.remove(ft);
			}
		}
	}

	private void waitForUploadThreadsToBeDone() {
		int nbTry = 2;
		while (nbTry-- > 0) {
			try {
				for (UploaderThread ft : getThreadsList())
					ft.waitTillProcessingIsDone();
				return;
			} catch (InterruptedException Ex) {
				// If the processing was stopped wait for each thread to end
				if (!isInterrupted()) {
					log.info(Messages.UploadMsg_GRACEFUL_SHUTDOWN);
				}
				markState(INTERRUPTED);
				getExceptionsSet().addCause(Ex);
			}
		}
		throw new RuntimeException("Fatal error occurred while waiting "
				+ "for " + UploaderMultiThread.class.getCanonicalName()
				+ " to finish.", getExceptionsSet());
	}

	private void quit() throws UploaderException, InterruptedException {
		for (UploaderThread ft : getThreadsList()) {
			markState(ft.getFinalState());
			if (ft.getFinalState() == FAILED || ft.getFinalState() == CRITICAL) {
				getExceptionsSet().addCause(ft.getFinalError());
			}
		}

		if (isCritical()) {
			throw new UploaderException(Msg.bind(Messages.UploadEx_UNMANAGED,
					getSession().getConnectionDatas()), getExceptionsSet());
		} else if (isFailed()) {
			throw new UploaderException(Msg.bind(Messages.UploadEx_MANAGED,
					getSession().getConnectionDatas()), getExceptionsSet());
		} else if (isInterrupted()) {
			throw new MelodyInterruptedException(Messages.UploadEx_INTERRUPTED,
					getExceptionsSet());
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

	protected List<SimpleResource> getSimpleResourcesList() {
		return _simpleResourcesList;
	}

	private List<SimpleResource> setSimpleResourcesList(List<SimpleResource> aft) {
		if (aft == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + List.class.getCanonicalName() + "<"
					+ SimpleResource.class.getCanonicalName() + ">.");
		}
		List<SimpleResource> previous = getSimpleResourcesList();
		_simpleResourcesList = aft;
		return previous;
	}

	protected int getMaxPar() {
		return _maxPar;
	}

	/**
	 * @param iMaxPar
	 *            is the maximum number of {@link UploaderThread} this object
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

	private TemplatingHandler getTemplatingHandler() {
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
	 * @return all the {@link UploaderThread} managed by this object.
	 */
	private List<UploaderThread> getThreadsList() {
		return _threadsList;
	}

	private List<UploaderThread> setThreadsList(List<UploaderThread> aft) {
		if (aft == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid List<UploadThread>.");
		}
		List<UploaderThread> previous = getThreadsList();
		_threadsList = aft;
		return previous;
	}

	/**
	 * @return the exceptions that append during the upload.
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