package com.wat.melody.common.ssh.impl;

import java.io.IOException;
import java.nio.file.Path;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.ssh.Messages;
import com.wat.melody.common.ssh.TemplatingHandler;
import com.wat.melody.common.ssh.exception.TemplatingException;
import com.wat.melody.common.ssh.types.GroupID;
import com.wat.melody.common.ssh.types.Modifiers;
import com.wat.melody.common.ssh.types.SimpleResource;
import com.wat.melody.common.ssh.types.TransferBehavior;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
class UploaderNoThread {

	private static Log log = LogFactory.getLog(UploaderNoThread.class);

	private ChannelSftp _channel;
	private SimpleResource _simpleResource;
	private TemplatingHandler _templatingHandler;

	protected UploaderNoThread(ChannelSftp channel, SimpleResource r,
			TemplatingHandler th) {
		setChannel(channel);
		setResource(r);
		setTemplatingHandler(th);
	}

	protected void upload() throws UploaderException {
		log.debug(Msg.bind(Messages.UploadMsg_BEGIN, getResource()));
		if (getResource().isDirectory()) {
			mkdirs(getResource());
		} else if (getResource().isFile()) {
			put(getResource());
		} else {
			log.warn(Msg.bind(Messages.UploadMsg_NOTFOUND, getResource()));
			return;
		}
		if (getResource().getGroup() != null && !getResource().isSymbolicLink()) {
			chgrp(getResource().getDestination(), getResource().getGroup());
		}
		log.info(Msg.bind(Messages.UploadMsg_END, getResource()));
	}

	protected void mkdirs(SimpleResource r) throws UploaderException {
		if (r.isSymbolicLink()) {
			mkdirs(r.getDestination().getParent().normalize());
			ln(r);
		} else {
			mkdirs(r.getDestination());
			chmod(r.getDestination(), r.getDirModifiers());
		}
	}

	protected void put(SimpleResource r) throws UploaderException {
		if (r == null) {
			throw new IllegalArgumentException("null: Not accpeted. "
					+ "Must be a valid "
					+ SimpleResource.class.getCanonicalName() + ".");
		}
		if (r.getDestination().getNameCount() > 1) {
			mkdirs(r.getDestination().resolve("..").normalize());
		}
		if (r.isSymbolicLink()) {
			ln(r);
		} else {
			template(r);
			chmod(r.getDestination(), r.getFileModifiers());
		}
	}

	protected void ln(SimpleResource r) throws UploaderException {
		if (r == null) {
			throw new IllegalArgumentException("null: Not accpeted. "
					+ "Must be a valid "
					+ SimpleResource.class.getCanonicalName() + ".");
		}
		switch (r.getLinkOption()) {
		case KEEP_LINKS:
			ln_keep(r);
			break;
		case COPY_LINKS:
			ln_copy(r);
			break;
		case COPY_UNSAFE_LINKS:
			ln_copy_unsafe(r);
			break;
		}
	}

	protected void mkdirs(Path dir) throws UploaderException {
		if (dir == null) {
			throw new IllegalArgumentException("null: Not accpeted. "
					+ "Must be a " + Path.class.getCanonicalName()
					+ " (a Directory Path, relative or absolute).");
		}
		if (dir.toString().length() == 0 || dir.getNameCount() < 1) {
			return;
		}
		// stat + mkdir must be atomic at the session level
		synchronized (getLock()) {
			// if dir exists => nothing to do
			String unixDir = convertToUnixPath(dir);
			try {
				getChannel().stat(unixDir);
				return;
			} catch (SftpException Ex) {
				if (Ex.id != ChannelSftp.SSH_FX_NO_SUCH_FILE) {
					throw new UploaderException(Msg.bind(
							Messages.UploadEx_LSTAT, unixDir), Ex);
				}
			}
			// if dir doesn't exists => create it
			try {
				mkdir(dir);
				return;
			} catch (UploaderException Ex) {
				// if the top first dir cannot be created => raise an error
				if (dir.getNameCount() <= 1) {
					throw Ex;
				}
			}
			// if dir cannot be created => create its parent
			Path parent = null;
			try {
				parent = dir.resolve("..").normalize();
				mkdirs(parent);
			} catch (UploaderException Ex) {
				throw new UploaderException(Msg.bind(Messages.UploadEx_MKDIRS,
						parent), Ex);
			}
			mkdir(dir);
		}
	}

	protected void ln_keep(SimpleResource r) throws UploaderException {
		if (r == null) {
			throw new IllegalArgumentException("null: Not accpeted. "
					+ "Must be a valid "
					+ SimpleResource.class.getCanonicalName() + ".");
		}
		String unixItem = convertToUnixPath(r.getDestination());
		try {
			// if the link exists => nothing to do
			/*
			 * Note that the link's target may be invalid. in this situation,
			 * because lstat will not follow link, lstat will not failed
			 */
			getChannel().lstat(unixItem);
			return;
		} catch (SftpException Ex) {
			if (Ex.id != ChannelSftp.SSH_FX_NO_SUCH_FILE) {
				throw new UploaderException(Msg.bind(Messages.UploadEx_LSTAT,
						unixItem), Ex);
			}
		}
		Path symbolinkLinkTarget = null;
		try {
			symbolinkLinkTarget = r.getSymbolicLinkTarget();
		} catch (IOException Ex) {
			throw new UploaderException(Ex);
		}
		String unixTarget = convertToUnixPath(symbolinkLinkTarget);
		try {
			getChannel().symlink(unixTarget, unixItem);
		} catch (SftpException Ex) {
			throw new UploaderException(Msg.bind(Messages.UploadEx_LN,
					unixTarget, unixItem), Ex);
		}
	}

	protected void ln_copy(SimpleResource r) throws UploaderException {
		if (r == null) {
			throw new IllegalArgumentException("null: Not accpeted. "
					+ "Must be a valid "
					+ SimpleResource.class.getCanonicalName() + ".");
		}
		if (!r.exists()) {
			log.warn(Messages
					.bind(Messages.UploadMsg_COPY_UNSAFE_IMPOSSIBLE, r));
			return;
		} else if (r.isFile()) {
			template(r);
			chmod(r.getDestination(), r.getFileModifiers());
		} else {
			mkdirs(r.getDestination());
		}
	}

	protected void ln_copy_unsafe(SimpleResource r) throws UploaderException {
		if (r == null) {
			throw new IllegalArgumentException("null: Not accpeted. "
					+ "Must be a valid "
					+ SimpleResource.class.getCanonicalName() + ".");
		}
		try {
			if (r.isSafeLink()) {
				ln_keep(r);
			} else {
				ln_copy(r);
			}
		} catch (IOException Ex) {
			throw new UploaderException(Ex);
		}
	}

	protected void template(SimpleResource r) throws UploaderException {
		if (r.getTemplate() == true) {
			if (getTemplatingHandler() == null) {
				throw new UploaderException(
						Messages.UploadEx_NO_TEMPLATING_HANDLER);
			}
			Path template;
			try {
				template = getTemplatingHandler().doTemplate(r.getPath());
			} catch (TemplatingException Ex) {
				throw new UploaderException(Ex);
			}
			put(template, r.getDestination(), r.getTransferBehavior());
		} else {
			put(r.getPath(), r.getDestination(), r.getTransferBehavior());
		}
	}

	protected void put(Path source, Path dest, TransferBehavior tb)
			throws UploaderException {
		if (source == null) {
			throw new IllegalArgumentException("null: Not accpeted. "
					+ "Must be a valid " + Path.class.getCanonicalName()
					+ " (the source file Path).");
		}
		if (dest == null) {
			throw new IllegalArgumentException("null: Not accpeted. "
					+ "Must be a valid " + Path.class.getCanonicalName()
					+ " (the destination file Path).");
		}
		String unixFile = convertToUnixPath(dest);
		if (!shouldTranferFile(source, unixFile, tb)) {
			return;
		}
		try {
			getChannel().put(source.toString(), unixFile);
		} catch (SftpException Ex) {
			throw new UploaderException(Msg.bind(Messages.UploadEx_PUT, source,
					unixFile), Ex);
		}
	}

	protected void chmod(Path item, Modifiers modifiers)
			throws UploaderException {
		if (item == null) {
			throw new IllegalArgumentException("null: Not accpeted. "
					+ "Must be a " + Path.class.getCanonicalName()
					+ " (a directory or file Path, relative or absolute).");
		}
		if (modifiers == null) {
			throw new IllegalArgumentException("null: Not accpeted. "
					+ "Must be a " + Modifiers.class.getCanonicalName() + ".");
		}
		String unixFile = convertToUnixPath(item);
		try {
			getChannel().chmod(modifiers.toInt(), unixFile);
		} catch (SftpException Ex) {
			throw new UploaderException(Msg.bind(Messages.UploadEx_CHMOD,
					modifiers, unixFile), Ex);
		}
	}

	protected void chgrp(Path item, GroupID group) throws UploaderException {
		if (item == null) {
			throw new IllegalArgumentException("null: Not accpeted. "
					+ "Must be a " + Path.class.getCanonicalName()
					+ " (a directory or file Path, relative or absolute).");
		}
		if (group == null) {
			throw new IllegalArgumentException("null: Not accpeted. "
					+ "Must be a " + GroupID.class.getCanonicalName() + ".");
		}
		String unixFile = convertToUnixPath(item);
		try {
			getChannel().chgrp(group.toInt(), unixFile);
		} catch (SftpException Ex) {
			throw new UploaderException(Msg.bind(Messages.UploadEx_CHGRP,
					group, unixFile), Ex);
		}
	}

	protected void mkdir(Path dir) throws UploaderException {
		if (dir == null) {
			throw new IllegalArgumentException("null: Not accpeted. "
					+ "Must be a " + Path.class.getCanonicalName()
					+ " (a directory Path, relative or absolute).");
		}
		String unixDir = convertToUnixPath(dir);
		try {
			getChannel().mkdir(unixDir);
		} catch (SftpException Ex) {
			throw new UploaderException(Msg.bind(Messages.UploadEx_MKDIR,
					unixDir), Ex);
		}
	}

	private static String convertToUnixPath(Path path) {
		return path.toString().replaceAll("\\\\", "/");
	}

	/**
	 * @param file
	 *            is the local file to test.
	 * @param unixDest
	 *            is the remote path of the given local file.
	 * @param tb
	 *            is the desired transfer behavior.
	 * 
	 * @return <tt>true</tt> if the given local file should be transfered, or
	 *         <tt>false</tt> otherwise. More formally :
	 *         <ul>
	 *         <li>return <tt>true</tt> if the desired transfer behavior is
	 *         equal to {@link TransferBehavior#FORCE_OVERWRITE} ;</li>
	 *         <li>return <tt>true</tt> if the desired transfer behavior is
	 *         equal to {@link TransferBehavior#OVERWRITE_IF_LOCAL_NEWER} and
	 *         the local file size is not equal to the remote file size ;</li>
	 *         <li>return <tt>true</tt> if the desired transfer behavior is
	 *         equal to {@link TransferBehavior#OVERWRITE_IF_LOCAL_NEWER} and
	 *         the local file size is equal to the remote file size and the
	 *         local file last modification time is newer than the remote file
	 *         last modification time ;</li>
	 *         <li>return <tt>false</tt> otherwise ;</li>
	 *         </ul>
	 * 
	 * @throws UploaderException
	 *             if it fails to retrieve the stats of the remote file.
	 */
	private boolean shouldTranferFile(Path file, String unixDest,
			TransferBehavior tb) throws UploaderException {
		if (tb == TransferBehavior.FORCE_OVERWRITE) {
			// => transfer
			return true;
		}
		// Get remote file attributes
		SftpATTRS remoteFileAttrs = null;
		try {
			remoteFileAttrs = getChannel().lstat(unixDest);
		} catch (SftpException Ex) {
			if (Ex.id != ChannelSftp.SSH_FX_NO_SUCH_FILE) {
				// The remote file doesn't exists => should be transfered
				return true;
			} else {
				throw new UploaderException(Msg.bind(Messages.UploadEx_LSTAT,
						unixDest), Ex);
			}
		}
		if (remoteFileAttrs.getMTime() > file.toFile().lastModified() / 1000
				&& remoteFileAttrs.getSize() == file.toFile().length()) {
			log.info(Messages.UploadMsg_DONT_UPLOAD_CAUSE_LOCAL_OLDER);
			// => do not transfer
			return false;
		}
		// => transfer
		return true;
	}

	private static Object BASIC_LOCK = new Integer(0);

	/**
	 * @return a session scope lock.
	 */
	private Object getLock() {
		try {
			return getChannel().getSession();
		} catch (JSchException Ex) {
			return BASIC_LOCK;
		}
	}

	protected ChannelSftp getChannel() {
		return _channel;
	}

	private ChannelSftp setChannel(ChannelSftp channel) {
		if (channel == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + ChannelSftp.class.getCanonicalName()
					+ ".");
		}
		ChannelSftp previous = getChannel();
		_channel = channel;
		return previous;
	}

	protected SimpleResource getResource() {
		return _simpleResource;
	}

	private SimpleResource setResource(SimpleResource aft) {
		if (aft == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ SimpleResource.class.getCanonicalName() + ".");
		}
		SimpleResource previous = getResource();
		_simpleResource = aft;
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

}