package com.wat.melody.common.ssh.impl.downloader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpATTRS;
import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.ssh.Messages;
import com.wat.melody.common.ssh.TemplatingHandler;
import com.wat.melody.common.ssh.exception.SshSessionException;
import com.wat.melody.common.ssh.exception.TemplatingException;
import com.wat.melody.common.ssh.filesfinder.EnhancedFileAttributes;
import com.wat.melody.common.ssh.filesfinder.Resource;
import com.wat.melody.common.ssh.impl.FSHelper;
import com.wat.melody.common.ssh.impl.SftpHelper;
import com.wat.melody.common.ssh.types.GroupID;
import com.wat.melody.common.ssh.types.Modifiers;
import com.wat.melody.common.ssh.types.TransferBehavior;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
class DownloaderNoThread {

	private static Logger log = LoggerFactory
			.getLogger(DownloaderNoThread.class);

	private ChannelSftp _channel;
	private Resource _resource;
	private TemplatingHandler _templatingHandler;

	protected DownloaderNoThread(ChannelSftp channel, Resource rr,
			TemplatingHandler th) {
		setChannel(channel);
		setResource(rr);
		setTemplatingHandler(th);
	}

	protected void download() throws DownloaderException {
		Resource rr = getResource();
		log.debug(Msg.bind(Messages.DownloadMsg_BEGIN, rr));
		try {// ensure parent directory exists
			Path dest = rr.getDestination().normalize();
			if (dest.getNameCount() > 1) {
				Files.createDirectories(dest.getParent());
				// neither chmod nor chgrp.
			}

			// deal with resource, regarding its type
			if (rr.isSymbolicLink()) {
				ln(rr);
			} else if (rr.isDirectory()) {
				mkdir(rr.getDestination());
				chmod(rr.getDestination(), rr.getDirModifiers());
				chgrp(rr.getDestination(), rr.getGroup());
			} else if (rr.isRegularFile()) {
				template(rr);
				chmod(rr.getDestination(), rr.getFileModifiers());
				chgrp(rr.getDestination(), rr.getGroup());
			} else {
				log.warn(Msg.bind(Messages.DownloadMsg_NOTFOUND, rr));
				return;
			}
		} catch (SshSessionException | IOException Ex) {
			throw new DownloaderException(Ex);
		}

		log.info(Msg.bind(Messages.DownloadMsg_END, rr));
	}

	protected void ln(Resource r) throws IOException, SshSessionException {
		// TODO : use getResource instead
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

	protected void ln_copy_unsafe(Resource r) throws IOException,
			SshSessionException {
		if (r.isSafeLink()) {
			ln_keep(r);
		} else {
			ln_copy(r);
		}
	}

	protected void ln_keep(Resource r) throws IOException, SshSessionException {
		Path link = r.getDestination();
		Path target = r.getSymbolicLinkTarget();
		if (FSHelper.ensureLink(target, link)) {
			log.info(Messages.DownloadMsg_DONT_DOWNLOAD_CAUSE_LINK_ALREADY_EXISTS);
			return;
		}
		FSHelper.ln(link, target);
	}

	protected void ln_copy(Resource r) throws IOException, SshSessionException {
		if (!r.exists()) {
			String unixPath = SftpHelper.convertToUnixPath(r.getDestination());
			SftpATTRS attrs = SftpHelper.scp_lstat(getChannel(), unixPath);
			if (attrs != null) {
				if (attrs.isDir()) {
					SftpHelper.scp_rmdirs(getChannel(), unixPath);
				} else {
					SftpHelper.scp_rm(getChannel(), unixPath);
				}
			}
			log.warn(Messages.bind(Messages.DownloadMsg_COPY_UNSAFE_IMPOSSIBLE,
					r));
		} else if (r.isRegularFile()) {
			template(r);
			chmod(r.getDestination(), r.getFileModifiers());
			chgrp(r.getDestination(), r.getGroup());
		} else {
			mkdir(r.getDestination());
			chmod(r.getDestination(), r.getDirModifiers());
			chgrp(r.getDestination(), r.getGroup());
		}
	}

	protected void template(Resource r) throws IOException, SshSessionException {
		if (r.getTemplate() == true) {
			if (getTemplatingHandler() == null) {
				throw new SshSessionException(
						Messages.DownloadEx_NO_TEMPLATING_HANDLER);
			}
			Path template;
			try {
				template = getTemplatingHandler().doTemplate(r.getPath());
			} catch (TemplatingException Ex) {
				throw new SshSessionException(Ex);
			}
			get(template, r.getAttributes(), r.getDestination(),
					r.getTransferBehavior());
		} else {
			get(r.getPath(), r.getAttributes(), r.getDestination(),
					r.getTransferBehavior());
		}
	}

	protected void mkdir(Path dir) throws IOException, SshSessionException {
		if (dir == null) {
			throw new IllegalArgumentException("null: Not accpeted. "
					+ "Must be a " + Path.class.getCanonicalName()
					+ " (a Directory Path, relative or absolute).");
		}
		if (FSHelper.ensureDir(dir)) {
			log.info(Messages.DownloadMsg_DONT_DOWNLOAD_CAUSE_DIR_ALREADY_EXISTS);
			return;
		}
		FSHelper.mkdir(dir);
	}

	protected void get(Path source, EnhancedFileAttributes remoteFileAttrs,
			Path dest, TransferBehavior tb) throws IOException,
			SshSessionException {
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
		String unixFile = SftpHelper.convertToUnixPath(source);
		if (FSHelper.ensureFile(remoteFileAttrs, dest, tb)) {
			log.info(Messages.DownloadMsg_DONT_DOWNLOAD_CAUSE_FILE_ALREADY_EXISTS);
			return;
		}
		SftpHelper.scp_get(getChannel(), unixFile, dest.toString());
	}

	protected void chmod(Path path, Modifiers modifiers)
			throws SshSessionException {
		if (modifiers == null) {
			return;
		}
		if (path == null) {
			throw new IllegalArgumentException("null: Not accpeted. "
					+ "Must be a " + Path.class.getCanonicalName()
					+ " (a directory or file Path, relative or absolute).");
		}
		// TODO : local chmod
	}

	protected void chgrp(Path path, GroupID group) throws SshSessionException {
		if (group == null) {
			return;
		}
		if (path == null) {
			throw new IllegalArgumentException("null: Not accpeted. "
					+ "Must be a " + Path.class.getCanonicalName()
					+ " (a directory or file Path, relative or absolute).");
		}
		// TODO : local chgrp
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

	protected Resource getResource() {
		return _resource;
	}

	private Resource setResource(Resource r) {
		if (r == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Resource.class.getCanonicalName()
					+ ".");
		}
		Resource previous = getResource();
		_resource = r;
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