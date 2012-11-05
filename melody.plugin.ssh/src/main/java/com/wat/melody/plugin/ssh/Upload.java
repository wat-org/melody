package com.wat.melody.plugin.ssh;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.wat.melody.api.annotation.Attribute;
import com.wat.melody.api.annotation.NestedElement;
import com.wat.melody.api.annotation.NestedElement.Type;
import com.wat.melody.api.exception.ExpressionSyntaxException;
import com.wat.melody.common.typedef.GroupID;
import com.wat.melody.common.typedef.Modifiers;
import com.wat.melody.common.typedef.ResourceMatcher;
import com.wat.melody.common.typedef.Resources;
import com.wat.melody.common.typedef.SimpleResource;
import com.wat.melody.common.typedef.exception.ResourceException;
import com.wat.melody.common.utils.Tools;
import com.wat.melody.plugin.ssh.common.AbstractSshOperation;
import com.wat.melody.plugin.ssh.common.Messages;
import com.wat.melody.plugin.ssh.common.exception.SshException;

public class Upload extends AbstractSshOperation {

	private static Log log = LogFactory.getLog(Upload.class);

	/**
	 * The 'scp' XML element used in the Sequence Descriptor
	 */
	public static final String UPLOAD = "upload";

	/**
	 * The 'maxpar' XML Attribute
	 */
	public static final String MAXPAR_ATTR = "maxpar";

	/**
	 * The 'resources' XML Nested Element
	 */
	public static final String RESOURCES_NE = "resources";

	public static final short NEW = 16;
	public static final short RUNNING = 8;
	public static final short SUCCEED = 0;
	public static final short FAILED = 1;
	public static final short INTERRUPTED = 2;
	public static final short CRITICAL = 4;

	private List<Resources> maResourcesList;
	private int miMaxPar;

	private Session moSession;
	private List<SimpleResource> maSimpleResourcesList;
	private short miState;
	private ThreadGroup moThreadGroup;
	private List<UploadThread> maThreadsList;
	private List<Throwable> maExceptionsList;

	public Upload() {
		super();
		setResourcesList(new ArrayList<Resources>());
		try {
			setMaxPar(10);
		} catch (SshException Ex) {
			throw new RuntimeException("TODO impossible");
		}
		setSimpleResourcesList(new ArrayList<SimpleResource>());
		markState(SUCCEED);
		setThreadGroup(null);
		setThreadsList(new ArrayList<UploadThread>());
		setExceptionsList(new ArrayList<Throwable>());
	}

	@Override
	public void validate() throws SshException {
		super.validate();

		for (Resources resources : getResourcesList()) {
			try {
				// validate each inner include/exclude ResourceMatcher
				validateResourceElementLsit(resources.getIncludes(),
						Resources.INCLUDE_NE);
				validateResourceElementLsit(resources.getExcludes(),
						Resources.EXCLUDE_NE);
				// validate itself, if it has no include ResourceMatcher
				if (resources.getIncludes().size() == 0) {
					validateResourceElement(resources, RESOURCES_NE);
				}
				// Add all found SimpleResource to the global list
				List<SimpleResource> ar = resources.findResources();
				getSimpleResourcesList().removeAll(ar); // remove duplicated
				getSimpleResourcesList().addAll(ar);
			} catch (IOException Ex) {
				throw new RuntimeException("IO Error while finding files.", Ex);
			} catch (SshException Ex) {
				throw new SshException(Messages.bind(
						Messages.UploadEx_INVALID_NE, RESOURCES_NE), Ex);
			}
		}
	}

	private void validateResourceElementLsit(
			List<ResourceMatcher> aResourceMatcher, String which)
			throws SshException {
		for (ResourceMatcher r : aResourceMatcher) {
			validateResourceElement(r, which);
		}
	}

	private void validateResourceElement(ResourceMatcher r, String which)
			throws SshException {
		if (r.getLocalBaseDir() == null) {
			try {
				r.setLocalBaseDir(getContext().getProcessorManager()
						.getSequenceDescriptor().getBaseDir());
			} catch (ResourceException Ex) {
				throw new SshException(Messages.bind(
						Messages.UploadEx_MISSING_ATTR,
						Resources.LOCAL_BASEDIR_ATTR, which), Ex);
			}
		}
		if (r.getMatch() == null) {
			throw new SshException(
					Messages.bind(Messages.UploadEx_MISSING_ATTR,
							Resources.MATCH_ATTR, which));
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

	@Override
	public void doProcessing() throws SshException, InterruptedException {
		try {
			setThreadGroup(new ThreadGroup(Thread.currentThread().getName()
					+ ">" + UPLOAD));
			getThreadGroup().setDaemon(true);
			setSession(openSession());
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
			if (getSession() != null) {
				getSession().disconnect();
			}
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
			getContext().handleProcessorStateUpdates();
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
				+ "for " + UPLOAD + " to finish.");
	}

	private void quit() throws SshException, InterruptedException {
		for (UploadThread ft : getThreadsList()) {
			markState(ft.getFinalState());
			if (ft.getFinalState() == FAILED || ft.getFinalState() == CRITICAL) {
				getExceptionsList().add(ft.getFinalError());
			}
		}

		if (isCritical()) {
			throw new SshException(Messages.bind(Messages.UploadEx_UNMANAGED,
					new Object[] { UPLOAD, HOST_ATTR,
							getHost().getValue().getHostAddress(), PORT_ATTR,
							getPort().getValue(), LOGIN_ATTR, getLogin() }),
					buildUploadTrace());
		} else if (isFailed()) {
			throw new SshException(Messages.bind(Messages.UploadEx_MANAGED,
					new Object[] { UPLOAD, HOST_ATTR,
							getHost().getValue().getHostAddress(), PORT_ATTR,
							getPort().getValue(), LOGIN_ATTR, getLogin() }),
					buildUploadTrace());
		} else if (isInterrupted()) {
			throw new InterruptedException(Messages.bind(
					Messages.UploadEx_INTERRUPTED, UPLOAD));
		}
	}

	private SshException buildUploadTrace() {
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
		return new SshException(err);
	}

	public void upload(ChannelSftp channel, SimpleResource r)
			throws SshException {
		if (channel == null) {
			throw new IllegalArgumentException("null: Not accpeted. "
					+ "Must be a SftpChannel.");
		}
		log.debug("Uploading:" + r);
		try {
			if (r.isDirectory()) {
				mkdirs(channel, r);
			} else if (r.isFile()) {
				put(channel, r);
			} else {
				throw new SshException(Messages.bind(
						Messages.UploadEx_NOTFOUND, r));
			}
			if (r.getGroup() != null && !r.isSymbolicLink()) {
				chgrp(channel, r.getDestination(), r.getGroup());
			}
		} catch (SshException Ex) {
			throw new SshException(Messages.bind(Messages.UploadEx_FAILED,
					new Object[] { getHost().getValue().getHostAddress(),
							getPort().getValue(), getLogin(), r }), Ex);
		}
		log.info("Uploaded:" + r);
	}

	protected void mkdirs(ChannelSftp channel, SimpleResource r)
			throws SshException {
		if (r.isSymbolicLink()) {
			mkdirs(channel, r.getDestination().getParent().normalize());
			ln(channel, r);
		} else {
			mkdirs(channel, r.getDestination());
			chmod(channel, r.getDestination(), r.getDirModifiers());
		}
	}

	protected void put(ChannelSftp channel, SimpleResource r)
			throws SshException {
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
			throws SshException {
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
	 * @throws SshException
	 */
	protected void mkdirs(ChannelSftp channel, Path dir) throws SshException {
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
					throw new SshException(Messages.bind(
							Messages.UploadEx_STAT, dir), Ex);
				}
			}
			// if the dirPath doesn't exists => create it
			try {
				mkdir(channel, dir);
				return;
			} catch (SshException Ex) {
				// if the top first dirPath cannot be created => raise an error
				if (dir.getNameCount() <= 1) {
					throw Ex;
				}
			}
		}
		// if the dirPath cannot be created => create its parent
		Path parent = null;
		try {
			parent = dir.resolve("..").normalize();
			mkdirs(channel, parent);
		} catch (SshException Ex) {
			throw new SshException(Messages.bind(Messages.UploadEx_MKDIRS,
					parent), Ex);
		}
		mkdirs(channel, dir);
	}

	protected void ln_keep(ChannelSftp channel, SimpleResource r)
			throws SshException {
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
				throw new SshException(Messages.bind(Messages.UploadEx_STAT,
						r.getDestination()), Ex);
			}
		}
		Path symbolinkLinkTarget = null;
		try {
			symbolinkLinkTarget = r.getSymbolicLinkTarget();
		} catch (IOException Ex) {
			throw new SshException(Ex);
		}
		try {
			channel.symlink(symbolinkLinkTarget.toString(), r.getDestination()
					.toString());
		} catch (SftpException Ex) {
			throw new SshException(Messages.bind(Messages.UploadEx_LN,
					symbolinkLinkTarget, r.getDestination()), Ex);
		}
	}

	protected void ln_copy(ChannelSftp channel, SimpleResource r)
			throws SshException {
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
			throws SshException {
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
			throw new SshException(Ex);
		}
	}

	protected void template(ChannelSftp channel, SimpleResource r)
			throws SshException {
		if (r.getTemplate() == true) {
			String fileContent = null;
			try {
				fileContent = getContext().expand(r.getPath());
			} catch (IOException Ex) {
				throw new SshException(Messages.bind(
						Messages.SshEx_READ_IO_ERROR, r.getPath()), Ex);
			} catch (ExpressionSyntaxException Ex) {
				throw new SshException(Ex);
			}
			Path template = null;
			try {
				Files.createDirectories(Paths.get(getContext()
						.getProcessorManager().getWorkingFolderPath()
						.toString()));
				template = Files.createTempFile(
						Paths.get(getContext().getProcessorManager()
								.getWorkingFolderPath().toString()),
						"template.", ".txt");
				Files.write(template, fileContent.getBytes());
			} catch (IOException Ex) {
				throw new SshException(Messages.bind(
						Messages.SshEx_WRITE_IO_ERROR, template), Ex);
			}

			put(channel, template, r.getDestination());
		} else {
			put(channel, r.getPath(), r.getDestination());
		}
	}

	protected void put(ChannelSftp channel, Path source, Path dest)
			throws SshException {
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
			throw new SshException(Messages.bind(Messages.UploadEx_PUT, source,
					dest), Ex);
		}
	}

	protected void chmod(ChannelSftp channel, Path file, Modifiers modifiers)
			throws SshException {
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
			throw new SshException(Messages.bind(Messages.UploadEx_CHMOD,
					modifiers, file), Ex);
		}
	}

	protected void chgrp(ChannelSftp channel, Path file, GroupID group)
			throws SshException {
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
			throw new SshException(Messages.bind(Messages.UploadEx_CHGRP,
					group, file), Ex);
		}
	}

	protected void mkdir(ChannelSftp channel, Path dir) throws SshException {
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
			throw new SshException(Messages.bind(Messages.UploadEx_MKDIR, dir),
					Ex);
		}
	}

	public List<Resources> getResourcesList() {
		return maResourcesList;
	}

	public List<Resources> setResourcesList(List<Resources> aResources) {
		List<Resources> previous = getResourcesList();
		maResourcesList = aResources;
		return previous;
	}

	@NestedElement(name = RESOURCES_NE, mandatory = true, type = Type.ADD)
	public void addResources(Resources resources) {
		getResourcesList().add(resources);
	}

	public int getMaxPar() {
		return miMaxPar;
	}

	@Attribute(name = MAXPAR_ATTR)
	public int setMaxPar(int iMaxPar) throws SshException {
		if (iMaxPar < 1 || iMaxPar > 10) {
			throw new SshException(Messages.bind(
					Messages.UploadEx_INVALID_MAXPAR_ATTR, iMaxPar));
		}
		int previous = getMaxPar();
		miMaxPar = iMaxPar;
		return previous;
	}

	protected Session getSession() {
		return moSession;
	}

	private Session setSession(Session session) {
		// can be null
		Session previous = getSession();
		moSession = session;
		return previous;
	}

	protected List<SimpleResource> getSimpleResourcesList() {
		return maSimpleResourcesList;
	}

	private List<SimpleResource> setSimpleResourcesList(List<SimpleResource> aft) {
		if (aft == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid List<SimpleResource>.");
		}
		List<SimpleResource> previous = getSimpleResourcesList();
		maSimpleResourcesList = aft;
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
