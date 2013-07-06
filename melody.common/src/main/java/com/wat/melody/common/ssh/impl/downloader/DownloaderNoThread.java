package com.wat.melody.common.ssh.impl.downloader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.ChannelSftp;
import com.wat.melody.common.ssh.Messages;
import com.wat.melody.common.ssh.TemplatingHandler;
import com.wat.melody.common.ssh.exception.SshSessionException;
import com.wat.melody.common.ssh.filesfinder.EnhancedFileAttributes;
import com.wat.melody.common.ssh.filesfinder.Resource;
import com.wat.melody.common.ssh.impl.FSHelper;
import com.wat.melody.common.ssh.impl.SftpHelper;
import com.wat.melody.common.ssh.impl.transfer.TransferNoThread;
import com.wat.melody.common.ssh.types.GroupID;
import com.wat.melody.common.ssh.types.Modifiers;
import com.wat.melody.common.ssh.types.TransferBehavior;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
class DownloaderNoThread extends TransferNoThread {

	private static Logger log = LoggerFactory
			.getLogger(DownloaderNoThread.class);

	protected DownloaderNoThread(ChannelSftp channel, Resource r,
			TemplatingHandler th) {
		super(channel, r, th);
	}

	@Override
	public void createParentDirectory() throws IOException {
		Path dest = getResource().getDestination().normalize();
		if (dest.getNameCount() > 1) {
			Files.createDirectories(dest.getParent());
			// neither chmod nor chgrp.
		}
	}

	@Override
	public void deleteDestination() throws IOException {
		Path path = getResource().getDestination();
		if (Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
			if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
				FileUtils.deleteDirectory(path.toFile());
			} else {
				// This method doesn't follow links
				Files.deleteIfExists(path);
			}
		}
	}

	@Override
	public void createDirectory() throws IOException, SshSessionException {
		Path dir = getResource().getDestination();
		if (FSHelper.ensureDir(dir)) {
			log.info(Messages.DownloadMsg_DONT_DOWNLOAD_CAUSE_DIR_ALREADY_EXISTS);
			return;
		}
		FSHelper.mkdir(dir);
	}

	@Override
	public void transferFile(Path source,
			EnhancedFileAttributes remoteFileAttrs, Path dest,
			TransferBehavior tb) throws IOException, SshSessionException {
		String unixFile = SftpHelper.convertToUnixPath(source);
		if (FSHelper.ensureFile(remoteFileAttrs, dest, tb)) {
			log.info(Messages.DownloadMsg_DONT_DOWNLOAD_CAUSE_FILE_ALREADY_EXISTS);
			return;
		}
		SftpHelper.scp_get(getChannel(), unixFile, dest.toString());
	}

	@Override
	public void createSymlink() throws IOException, SshSessionException {
		Resource r = getResource();
		Path link = r.getDestination();
		Path target = r.getSymbolicLinkTarget();
		if (FSHelper.ensureLink(target, link)) {
			log.info(Messages.DownloadMsg_DONT_DOWNLOAD_CAUSE_LINK_ALREADY_EXISTS);
			return;
		}
		FSHelper.ln(link, target);
	}

	@Override
	public void chmodFile() throws IOException, SshSessionException {
		// Path path = getResource().getDestination();
		Modifiers modifiers = getResource().getFileModifiers();
		if (modifiers == null) {
			return;
		}
		// TODO : local chmod
	}

	@Override
	public void chmodDir() throws IOException, SshSessionException {
		// Path path = getResource().getDestination();
		Modifiers modifiers = getResource().getDirModifiers();
		if (modifiers == null) {
			return;
		}
		// TODO : local chmod
	}

	@Override
	public void chgrp() throws IOException, SshSessionException {
		// Path path = getResource().getDestination();
		GroupID group = getResource().getGroup();
		if (group == null) {
			return;
		}
		// TODO : local chgrp
	}

}