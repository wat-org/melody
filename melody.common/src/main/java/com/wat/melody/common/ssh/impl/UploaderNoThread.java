package com.wat.melody.common.ssh.impl;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import com.wat.melody.common.ssh.Messages;
import com.wat.melody.common.ssh.TemplatingHandler;
import com.wat.melody.common.ssh.exception.TemplatingException;
import com.wat.melody.common.ssh.types.GroupID;
import com.wat.melody.common.ssh.types.Modifiers;
import com.wat.melody.common.ssh.types.SimpleResource;

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
		log.debug(Messages.bind(Messages.UploadMsg_BEGIN, getResource()));
		if (getResource().isDirectory()) {
			mkdirs(getResource());
		} else if (getResource().isFile()) {
			put(getResource());
		} else {
			log.warn(Messages.bind(Messages.UploadMsg_NOTFOUND, getResource()));
			return;
		}
		if (getResource().getGroup() != null && !getResource().isSymbolicLink()) {
			chgrp(getResource().getDestination(), getResource().getGroup());
		}
		log.info(Messages.bind(Messages.UploadMsg_END, getResource()));
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
					+ "Must be a valid SimpleResource.");
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
					+ "Must be a valid SimpleResource.");
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

	/**
	 * 
	 * @param channel
	 * @param dir
	 *            can be an absolute or relative directory path.
	 * @throws UploaderException
	 */
	protected void mkdirs(Path dir) throws UploaderException {
		if (dir == null) {
			throw new IllegalArgumentException("null: Not accpeted. "
					+ "Must be a Path (a Directory Path, relative or "
					+ "absolute).");
		}
		if (dir.toString().length() == 0 || dir.getNameCount() < 1) {
			return;
		}
		// stat + mkdir must be atomic at the session level
		synchronized (getLock()) {
			// if the dirPath exists => nothing to do
			try {
				getChannel().stat(dir.toString());
				return;
			} catch (SftpException Ex) {
				if (Ex.id != ChannelSftp.SSH_FX_NO_SUCH_FILE) {
					throw new UploaderException(Messages.bind(
							Messages.UploadEx_STAT, dir), Ex);
				}
			}
			// if the dirPath doesn't exists => create it
			try {
				mkdir(dir);
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
				mkdirs(parent);
			} catch (UploaderException Ex) {
				throw new UploaderException(Messages.bind(
						Messages.UploadEx_MKDIRS, parent), Ex);
			}
			mkdir(dir);
		}
	}

	protected void ln_keep(SimpleResource r) throws UploaderException {
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
			getChannel().lstat(r.getDestination().toString());
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
			getChannel().symlink(symbolinkLinkTarget.toString(),
					r.getDestination().toString());
		} catch (SftpException Ex) {
			throw new UploaderException(Messages.bind(Messages.UploadEx_LN,
					symbolinkLinkTarget, r.getDestination()), Ex);
		}
	}

	protected void ln_copy(SimpleResource r) throws UploaderException {
		if (r == null) {
			throw new IllegalArgumentException("null: Not accpeted. "
					+ "Must be a valid SimpleResource.");
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
					+ "Must be a valid SimpleResource.");
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
			Path template;
			try {
				template = getTemplatingHandler().doTemplate(r.getPath());
			} catch (TemplatingException Ex) {
				throw new UploaderException(Ex);
			}
			put(template, r.getDestination());
		} else {
			put(r.getPath(), r.getDestination());
		}
	}

	protected void put(Path source, Path dest) throws UploaderException {
		if (source == null) {
			throw new IllegalArgumentException("null: Not accpeted. "
					+ "Must be a valid String (a File Path).");
		}
		if (dest == null) {
			throw new IllegalArgumentException("null: Not accpeted. "
					+ "Must be a valid String (a File Path).");
		}
		try {
			getChannel().put(source.toString(), dest.toString());
		} catch (SftpException Ex) {
			throw new UploaderException(Messages.bind(Messages.UploadEx_PUT,
					source, dest), Ex);
		}
	}

	protected void chmod(Path file, Modifiers modifiers)
			throws UploaderException {
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
			getChannel().chmod(modifiers.toInt(), file.toString());
		} catch (SftpException Ex) {
			throw new UploaderException(Messages.bind(Messages.UploadEx_CHMOD,
					modifiers, file), Ex);
		}
	}

	protected void chgrp(Path file, GroupID group) throws UploaderException {
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
			getChannel().chgrp(group.toInt(), file.toString());
		} catch (SftpException Ex) {
			throw new UploaderException(Messages.bind(Messages.UploadEx_CHGRP,
					group, file), Ex);
		}
	}

	protected void mkdir(Path dir) throws UploaderException {
		if (dir == null) {
			throw new IllegalArgumentException("null: Not accpeted. "
					+ "Must be a String (a Directory Path, relative or "
					+ "absolute).");
		}
		try {
			getChannel().mkdir(dir.toString());
		} catch (SftpException Ex) {
			throw new UploaderException(Messages.bind(Messages.UploadEx_MKDIR,
					dir), Ex);
		}
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
					+ "Must be a valid " + List.class.getCanonicalName() + "<"
					+ SimpleResource.class.getCanonicalName() + ">.");
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