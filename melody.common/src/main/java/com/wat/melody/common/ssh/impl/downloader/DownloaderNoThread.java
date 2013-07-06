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
import com.wat.melody.common.ssh.exception.SshSessionException;
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

	protected DownloaderNoThread(ChannelSftp channel, Resource rr) {
		setChannel(channel);
		setResource(rr);
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
				get(rr.getPath(), rr.getAttributes(), rr.getDestination(),
						rr.getTransferBehavior());
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

	protected void ln(Resource rr) throws IOException, SshSessionException {
		// TODO : use getResource instead
		switch (rr.getLinkOption()) {
		case KEEP_LINKS:
			ln_keep(rr);
			break;
		case COPY_LINKS:
			ln_copy(rr);
			break;
		case COPY_UNSAFE_LINKS:
			ln_copy_unsafe(rr);
			break;
		}
	}

	protected void ln_copy_unsafe(Resource rr) throws IOException,
			SshSessionException {
		if (rr.isSafeLink()) {
			ln_keep(rr);
		} else {
			ln_copy(rr);
		}
	}

	protected void ln_keep(Resource rr) throws IOException, SshSessionException {
		Path link = rr.getDestination();
		Path target = rr.getSymbolicLinkTarget();
		if (FSHelper.ensureLink(target, link)) {
			log.info(Messages.DownloadMsg_DONT_DOWNLOAD_CAUSE_LINK_ALREADY_EXISTS);
			return;
		}
		FSHelper.ln(link, target);
	}

	protected void ln_copy(Resource rr) throws IOException, SshSessionException {
		if (!rr.exists()) {
			String unixPath = SftpHelper.convertToUnixPath(rr.getDestination());
			SftpATTRS attrs = SftpHelper.scp_lstat(getChannel(), unixPath);
			if (attrs != null) {
				if (attrs.isDir()) {
					SftpHelper.scp_rmdirs(getChannel(), unixPath);
				} else {
					SftpHelper.scp_rm(getChannel(), unixPath);
				}
			}
			log.warn(Messages.bind(Messages.DownloadMsg_COPY_UNSAFE_IMPOSSIBLE,
					rr));
		} else if (rr.isRegularFile()) {
			get(rr.getPath(), rr.getAttributes(), rr.getDestination(),
					rr.getTransferBehavior());
			chmod(rr.getDestination(), rr.getFileModifiers());
			chgrp(rr.getDestination(), rr.getGroup());
		} else {
			mkdir(rr.getDestination());
			chmod(rr.getDestination(), rr.getDirModifiers());
			chgrp(rr.getDestination(), rr.getGroup());
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

	private Resource setResource(Resource rr) {
		if (rr == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Resource.class.getCanonicalName()
					+ ".");
		}
		Resource previous = getResource();
		_resource = rr;
		return previous;
	}

}