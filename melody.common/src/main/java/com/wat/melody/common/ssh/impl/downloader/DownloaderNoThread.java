package com.wat.melody.common.ssh.impl.downloader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpATTRS;
import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.ssh.Messages;
import com.wat.melody.common.ssh.exception.SshSessionException;
import com.wat.melody.common.ssh.filesfinder.RemoteResource;
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

	private static Log log = LogFactory.getLog(DownloaderNoThread.class);

	private ChannelSftp _channel;
	private RemoteResource _remoteResource;

	protected DownloaderNoThread(ChannelSftp channel, RemoteResource rr) {
		setChannel(channel);
		setRemoteResource(rr);
	}

	protected void download() throws DownloaderException {
		RemoteResource rr = getRemoteResource();
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
			} else if (rr.isFile()) {
				get(rr.getPath(), rr.getDestination(), rr.getTransferBehavior());
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

	protected void ln(RemoteResource rr) throws SshSessionException {
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

	protected void ln_copy_unsafe(RemoteResource rr) throws SshSessionException {
		if (rr.isSafeLink()) {
			ln_keep(rr);
		} else {
			ln_copy(rr);
		}
	}

	protected void ln_keep(RemoteResource rr) throws SshSessionException {
		Path link = rr.getDestination();
		Path target = rr.getSymbolicLinkTarget();
		// TODO : ensure link
		FSHelper.ln(link, target);

	}

	protected void ln_copy(RemoteResource rr) throws SshSessionException {
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
		} else if (rr.isFile()) {
			get(rr.getPath(), rr.getDestination(), rr.getTransferBehavior());
			chmod(rr.getDestination(), rr.getFileModifiers());
			chgrp(rr.getDestination(), rr.getGroup());
		} else {
			mkdir(rr.getDestination());
			chmod(rr.getDestination(), rr.getDirModifiers());
			chgrp(rr.getDestination(), rr.getGroup());
		}
	}

	protected void mkdir(Path dir) throws SshSessionException {
		if (dir == null) {
			throw new IllegalArgumentException("null: Not accpeted. "
					+ "Must be a " + Path.class.getCanonicalName()
					+ " (a Directory Path, relative or absolute).");
		}
		// TODO : ensure Dir
		FSHelper.mkdir(dir);
	}

	protected void get(Path source, Path dest, TransferBehavior tb)
			throws SshSessionException {
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
		String unixFile = SftpHelper.convertToUnixPath(dest);
		// TODO : ensure File
		SftpHelper.scp_get(getChannel(), source.toString(), unixFile);
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

	protected RemoteResource getRemoteResource() {
		return _remoteResource;
	}

	private RemoteResource setRemoteResource(RemoteResource rr) {
		if (rr == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ RemoteResource.class.getCanonicalName() + ".");
		}
		RemoteResource previous = getRemoteResource();
		_remoteResource = rr;
		return previous;
	}

}