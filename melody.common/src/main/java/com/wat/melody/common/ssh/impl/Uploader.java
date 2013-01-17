package com.wat.melody.common.ssh.impl;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;
import com.wat.melody.common.ssh.Messages;
import com.wat.melody.common.ssh.TemplatingHandler;
import com.wat.melody.common.ssh.exception.TemplatingException;
import com.wat.melody.common.ssh.types.GroupID;
import com.wat.melody.common.ssh.types.Modifiers;
import com.wat.melody.common.ssh.types.SimpleResource;
import com.wat.melody.common.utils.Tools;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
class Uploader {

	private static Log log = LogFactory.getLog(Uploader.class);

	protected static final short NEW = 16;
	protected static final short RUNNING = 8;
	protected static final short SUCCEED = 0;
	protected static final short FAILED = 1;
	protected static final short INTERRUPTED = 2;
	protected static final short CRITICAL = 4;

	private SshSession _session;
	private List<SimpleResource> maSimpleResourcesList;
	private int miMaxPar;
	private TemplatingHandler _templatingHandler;

	private short miState;
	private ThreadGroup moThreadGroup;
	private List<UploadThread> maThreadsList;
	private List<Throwable> maExceptionsList;

	protected Uploader(SshSession session, List<SimpleResource> r, int maxPar,
			TemplatingHandler th) {
		setSession(session);
		setSimpleResourcesList(r);
		setMaxPar(maxPar);
		setTemplatingHandler(th);

		markState(SUCCEED);
		setThreadGroup(null);
		setThreadsList(new ArrayList<UploadThread>());
		setExceptionsList(new ArrayList<Throwable>());
	}

	protected void upload() throws UploaderException, InterruptedException {
		if (getSimpleResourcesList().size() == 0) {
			return;
		}
		try {
			setThreadGroup(new ThreadGroup(Thread.currentThread().getName()
					+ ">uploader"));
			getThreadGroup().setDaemon(true);
			initializeUploadThreads();
			try {
				startUploadThreads();
			} catch (InterruptedException Ex) {
				markState(INTERRUPTED);
			} catch (Throwable Ex) {
				getExceptionsList().add(Ex);
				markState(CRITICAL);
			} finally {
				// If an error occurred while starting thread, some thread may
				// have been launched without any problem
				// We must wait for these threads to die
				waitForUploadThreadsToBeDone();
				quit();
			}
		} finally {
			// This allow the doProcessing method to be called multiple time
			// (will certainly be useful someday)
			setThreadGroup(null);
		}
	}

	private void initializeUploadThreads() {
		for (int i = 0; i < getMaxPar(); i++) {
			getThreadsList().add(new UploadThread(this, i + 1));
		}
	}

	private void startUploadThreads() throws InterruptedException {
		int threadToLaunchID = getThreadsList().size();
		List<UploadThread> runningThreads = new ArrayList<UploadThread>();

		while (threadToLaunchID > 0 || runningThreads.size() > 0) {
			// Start ready threads
			while (threadToLaunchID > 0 && runningThreads.size() < getMaxPar()) {
				UploadThread ft = getThreadsList().get(--threadToLaunchID);
				runningThreads.add(ft);
				ft.startProcessing();
			}
			// Sleep a little
			if (runningThreads.size() > 0)
				runningThreads.get(0).waitTillProcessingIsDone(50);
			// Remove ended threads
			for (int i = runningThreads.size() - 1; i >= 0; i--) {
				UploadThread ft = runningThreads.get(i);
				if (ft.getFinalState() != NEW && ft.getFinalState() != RUNNING)
					runningThreads.remove(ft);
			}
		}
	}

	private void waitForUploadThreadsToBeDone() {
		int nbTry = 2;
		while (nbTry-- > 0) {
			try {
				for (UploadThread ft : getThreadsList())
					ft.waitTillProcessingIsDone();
				return;
			} catch (InterruptedException Ex) {
				// If the processing was stopped wait for each thread to end
				markState(INTERRUPTED);
			}
		}
		throw new RuntimeException("Fatal error occurred while waiting "
				+ "for " + Uploader.class.getCanonicalName() + " to finish.");
	}

	private void quit() throws UploaderException, InterruptedException {
		for (UploadThread ft : getThreadsList()) {
			markState(ft.getFinalState());
			if (ft.getFinalState() == FAILED || ft.getFinalState() == CRITICAL) {
				getExceptionsList().add(ft.getFinalError());
			}
		}

		if (isCritical()) {
			throw new UploaderException(Messages.bind(
					Messages.UploadEx_UNMANAGED, _session), buildUploadTrace());
		} else if (isFailed()) {
			throw new UploaderException(Messages.bind(
					Messages.UploadEx_MANAGED, _session), buildUploadTrace());
		} else if (isInterrupted()) {
			throw new InterruptedException(Messages.UploadEx_INTERRUPTED);
		}
	}

	private UploaderException buildUploadTrace() {
		if (getExceptionsList().size() == 0) {
			return null;
		}
		String err = "";
		for (int i = 0; i < getExceptionsList().size(); i++) {
			err += Tools.NEW_LINE
					+ "Error "
					+ (i + 1)
					+ " : "
					+ Tools.getUserFriendlyStackTrace(getExceptionsList()
							.get(i));
		}
		err = err.replaceAll(Tools.NEW_LINE, Tools.NEW_LINE + "   ");
		return new UploaderException(err);
	}

	protected void upload(ChannelSftp channel, SimpleResource r) {
		log.debug("Uploading:" + r);
		try {
			if (r.isDirectory()) {
				mkdirs(channel, r);
			} else if (r.isFile()) {
				put(channel, r);
			} else {
				log.warn(Messages.bind(Messages.UploadMsg_NOTFOUND, r));
				return;
			}
			if (r.getGroup() != null && !r.isSymbolicLink()) {
				chgrp(channel, r.getDestination(), r.getGroup());
			}
		} catch (UploaderException Ex) {
			UploaderException e = new UploaderException(Messages.bind(
					Messages.UploadEx_FAILED, _session), Ex);
			markState(Uploader.FAILED);
			getExceptionsList().add(e);
		}
		log.info("Uploaded:" + r);
	}

	protected void mkdirs(ChannelSftp channel, SimpleResource r)
			throws UploaderException {
		if (r.isSymbolicLink()) {
			mkdirs(channel, r.getDestination().getParent().normalize());
			ln(channel, r);
		} else {
			mkdirs(channel, r.getDestination());
			chmod(channel, r.getDestination(), r.getDirModifiers());
		}
	}

	protected void put(ChannelSftp channel, SimpleResource r)
			throws UploaderException {
		if (channel == null) {
			throw new IllegalArgumentException("null: Not accpeted. "
					+ "Must be a SftpChannel.");
		}
		if (r == null) {
			throw new IllegalArgumentException("null: Not accpeted. "
					+ "Must be a valid SimpleResource.");
		}
		if (r.getDestination().getNameCount() > 1) {
			mkdirs(channel, r.getDestination().resolve("..").normalize());
		}
		if (r.isSymbolicLink()) {
			ln(channel, r);
		} else {
			template(channel, r);
			chmod(channel, r.getDestination(), r.getFileModifiers());
		}
	}

	protected void ln(ChannelSftp channel, SimpleResource r)
			throws UploaderException {
		if (channel == null) {
			throw new IllegalArgumentException("null: Not accpeted. "
					+ "Must be a SftpChannel.");
		}
		if (r == null) {
			throw new IllegalArgumentException("null: Not accpeted. "
					+ "Must be a valid SimpleResource.");
		}
		switch (r.getLinkOption()) {
		case KEEP_LINKS:
			ln_keep(channel, r);
			break;
		case COPY_LINKS:
			ln_copy(channel, r);
			break;
		case COPY_UNSAFE_LINKS:
			ln_copy_unsafe(channel, r);
			break;
		}
	}

	/**
	 * 
	 * @param channel
	 * @param dir
	 *            can be an absolute or relative directory path.
	 * @throws UploaderException
	 */
	protected void mkdirs(ChannelSftp channel, Path dir)
			throws UploaderException {
		if (channel == null) {
			throw new IllegalArgumentException("null: Not accpeted. "
					+ "Must be a SftpChannel.");
		}
		if (dir == null) {
			throw new IllegalArgumentException("null: Not accpeted. "
					+ "Must be a Path (a Directory Path, relative or "
					+ "absolute).");
		}
		if (dir.toString().length() == 0 || dir.getNameCount() < 1) {
			return;
		}
		synchronized (this) { // stat + mkdir must be atomic
			// if the dirPath exists => nothing to do
			try {
				channel.stat(dir.toString());
				return;
			} catch (SftpException Ex) {
				if (Ex.id != ChannelSftp.SSH_FX_NO_SUCH_FILE) {
					throw new UploaderException(Messages.bind(
							Messages.UploadEx_STAT, dir), Ex);
				}
			}
			// if the dirPath doesn't exists => create it
			try {
				mkdir(channel, dir);
				return;
			} catch (UploaderException Ex) {
				// if the top first dirPath cannot be created => raise an error
				if (dir.getNameCount() <= 1) {
					throw Ex;
				}
			}
			// if the dirPath cannot be created => create its parent
			Path parent = null;
			try {
				parent = dir.resolve("..").normalize();
				mkdirs(channel, parent);
			} catch (UploaderException Ex) {
				throw new UploaderException(Messages.bind(
						Messages.UploadEx_MKDIRS, parent), Ex);
			}
			mkdir(channel, dir);
		}
	}

	protected void ln_keep(ChannelSftp channel, SimpleResource r)
			throws UploaderException {
		if (channel == null) {
			throw new IllegalArgumentException("null: Not accpeted. "
					+ "Must be a SftpChannel.");
		}
		if (r == null) {
			throw new IllegalArgumentException("null: Not accpeted. "
					+ "Must be a valid SimpleResource.");
		}
		try {
			// if the link exists => nothing to do
			/*
			 * Note that the link's target may be invalid. in this situation,
			 * because lstat will not follow link, lstat will not failed
			 */
			channel.lstat(r.getDestination().toString());
			return;
		} catch (SftpException Ex) {
			if (Ex.id != ChannelSftp.SSH_FX_NO_SUCH_FILE) {
				throw new UploaderException(Messages.bind(
						Messages.UploadEx_STAT, r.getDestination()), Ex);
			}
		}
		Path symbolinkLinkTarget = null;
		try {
			symbolinkLinkTarget = r.getSymbolicLinkTarget();
		} catch (IOException Ex) {
			throw new UploaderException(Ex);
		}
		try {
			channel.symlink(symbolinkLinkTarget.toString(), r.getDestination()
					.toString());
		} catch (SftpException Ex) {
			throw new UploaderException(Messages.bind(Messages.UploadEx_LN,
					symbolinkLinkTarget, r.getDestination()), Ex);
		}
	}

	protected void ln_copy(ChannelSftp channel, SimpleResource r)
			throws UploaderException {
		if (channel == null) {
			throw new IllegalArgumentException("null: Not accpeted. "
					+ "Must be a SftpChannel.");
		}
		if (r == null) {
			throw new IllegalArgumentException("null: Not accpeted. "
					+ "Must be a valid SimpleResource.");
		}
		if (!r.exists()) {
			log.warn(Messages
					.bind(Messages.UploadMsg_COPY_UNSAFE_IMPOSSIBLE, r));
			return;
		} else if (r.isFile()) {
			template(channel, r);
			chmod(channel, r.getDestination(), r.getFileModifiers());
		} else {
			mkdirs(channel, r.getDestination());
		}
	}

	protected void ln_copy_unsafe(ChannelSftp channel, SimpleResource r)
			throws UploaderException {
		if (channel == null) {
			throw new IllegalArgumentException("null: Not accpeted. "
					+ "Must be a SftpChannel.");
		}
		if (r == null) {
			throw new IllegalArgumentException("null: Not accpeted. "
					+ "Must be a valid SimpleResource.");
		}
		try {
			if (r.isSafeLink()) {
				ln_keep(channel, r);
			} else {
				ln_copy(channel, r);
			}
		} catch (IOException Ex) {
			throw new UploaderException(Ex);
		}
	}

	protected void template(ChannelSftp channel, SimpleResource r)
			throws UploaderException {
		if (r.getTemplate() == true) {
			Path template;
			try {
				template = getTemplatingHandler().doTemplate(r.getPath());
			} catch (TemplatingException Ex) {
				throw new UploaderException(Ex);
			}
			put(channel, template, r.getDestination());
		} else {
			put(channel, r.getPath(), r.getDestination());
		}
	}

	protected void put(ChannelSftp channel, Path source, Path dest)
			throws UploaderException {
		if (channel == null) {
			throw new IllegalArgumentException("null: Not accpeted. "
					+ "Must be a SftpChannel.");
		}
		if (source == null) {
			throw new IllegalArgumentException("null: Not accpeted. "
					+ "Must be a valid String (a File Path).");
		}
		if (dest == null) {
			throw new IllegalArgumentException("null: Not accpeted. "
					+ "Must be a valid String (a File Path).");
		}
		try {
			channel.put(source.toString(), dest.toString());
		} catch (SftpException Ex) {
			throw new UploaderException(Messages.bind(Messages.UploadEx_PUT,
					source, dest), Ex);
		}
	}

	protected void chmod(ChannelSftp channel, Path file, Modifiers modifiers)
			throws UploaderException {
		if (channel == null) {
			throw new IllegalArgumentException("null: Not accpeted. "
					+ "Must be a SftpChannel.");
		}
		if (file == null) {
			throw new IllegalArgumentException("null: Not accpeted. "
					+ "Must be a Path (a File or Directory Path, relative or "
					+ "absolute).");
		}
		if (modifiers == null) {
			throw new IllegalArgumentException("null: Not accpeted. "
					+ "Must be a Modifiers.");
		}
		try {
			channel.chmod(modifiers.toInt(), file.toString());
		} catch (SftpException Ex) {
			throw new UploaderException(Messages.bind(Messages.UploadEx_CHMOD,
					modifiers, file), Ex);
		}
	}

	protected void chgrp(ChannelSftp channel, Path file, GroupID group)
			throws UploaderException {
		if (channel == null) {
			throw new IllegalArgumentException("null: Not accpeted. "
					+ "Must be a SftpChannel.");
		}
		if (file == null) {
			throw new IllegalArgumentException("null: Not accpeted. "
					+ "Must be a Path (a File or Directory Path, relative or "
					+ "absolute).");
		}
		if (group == null) {
			throw new IllegalArgumentException("null: Not accpeted. "
					+ "Must be a Group.");
		}
		try {
			channel.chgrp(group.toInt(), file.toString());
		} catch (SftpException Ex) {
			throw new UploaderException(Messages.bind(Messages.UploadEx_CHGRP,
					group, file), Ex);
		}
	}

	protected void mkdir(ChannelSftp channel, Path dir)
			throws UploaderException {
		if (channel == null) {
			throw new IllegalArgumentException("null: Not accpeted. "
					+ "Must be a SftpChannel.");
		}
		if (dir == null) {
			throw new IllegalArgumentException("null: Not accpeted. "
					+ "Must be a String (a Directory Path, relative or "
					+ "absolute).");
		}
		try {
			channel.mkdir(dir.toString());
		} catch (SftpException Ex) {
			throw new UploaderException(Messages.bind(Messages.UploadEx_MKDIR,
					dir), Ex);
		}
	}

	protected short markState(short state) {
		return miState |= state;
	}

	private boolean isFailed() {
		return FAILED == (miState & FAILED);
	}

	private boolean isInterrupted() {
		return INTERRUPTED == (miState & INTERRUPTED);
	}

	private boolean isCritical() {
		return CRITICAL == (miState & CRITICAL);
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
		return maSimpleResourcesList;
	}

	private List<SimpleResource> setSimpleResourcesList(List<SimpleResource> aft) {
		if (aft == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + List.class.getCanonicalName() + "<"
					+ SimpleResource.class.getCanonicalName() + ">.");
		}
		List<SimpleResource> previous = getSimpleResourcesList();
		maSimpleResourcesList = aft;
		return previous;
	}

	protected int getMaxPar() {
		return miMaxPar;
	}

	private int setMaxPar(int iMaxPar) {
		if (iMaxPar < 1) {
			iMaxPar = 1; // security
		} else if (iMaxPar > 10) {
			iMaxPar = 10; // maximum number of opened JSch channel
		}
		int previous = getMaxPar();
		miMaxPar = iMaxPar;
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

	protected ThreadGroup getThreadGroup() {
		return moThreadGroup;
	}

	private ThreadGroup setThreadGroup(ThreadGroup tg) {
		// Can be null
		ThreadGroup previous = getThreadGroup();
		moThreadGroup = tg;
		return previous;
	}

	private List<UploadThread> getThreadsList() {
		return maThreadsList;
	}

	private List<UploadThread> setThreadsList(List<UploadThread> aft) {
		if (aft == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid List<UploadThread>.");
		}
		List<UploadThread> previous = getThreadsList();
		maThreadsList = aft;
		return previous;
	}

	protected List<Throwable> getExceptionsList() {
		return maExceptionsList;
	}

	private List<Throwable> setExceptionsList(List<Throwable> at) {
		if (at == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid List<Throwable>.");
		}
		List<Throwable> previous = getExceptionsList();
		maExceptionsList = at;
		return previous;
	}

}