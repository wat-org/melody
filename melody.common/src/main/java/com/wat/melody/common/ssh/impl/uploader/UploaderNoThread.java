package com.wat.melody.common.ssh.impl.uploader;

import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpATTRS;
import com.wat.melody.common.ssh.Messages;
import com.wat.melody.common.ssh.TemplatingHandler;
import com.wat.melody.common.ssh.exception.SshSessionException;
import com.wat.melody.common.ssh.filesfinder.EnhancedFileAttributes;
import com.wat.melody.common.ssh.filesfinder.Resource;
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
class UploaderNoThread extends TransferNoThread {

	private static Logger log = LoggerFactory.getLogger(UploaderNoThread.class);

	protected UploaderNoThread(ChannelSftp channel, Resource r,
			TemplatingHandler th) {
		super(channel, r, th);
	}

	@Override
	public void createParentDirectory() throws SshSessionException {
		Path dest = getResource().getDestination().normalize();
		if (dest.getNameCount() > 1) {
			SftpHelper.scp_mkdirs(getChannel(), dest.getParent());
			// neither chmod nor chgrp.
		}
	}

	@Override
	public void deleteDestination() throws SshSessionException {
		Path path = getResource().getDestination();
		String unixPath = SftpHelper.convertToUnixPath(path);
		SftpATTRS attrs = SftpHelper.scp_lstat(getChannel(), unixPath);
		if (attrs != null) {
			if (attrs.isDir()) {
				SftpHelper.scp_rmdirs(getChannel(), unixPath);
			} else {
				SftpHelper.scp_rmIfExists(getChannel(), unixPath);
			}
		}
	}

	@Override
	public void createDirectory() throws SshSessionException {
		Path dir = getResource().getDestination();
		String unixDir = SftpHelper.convertToUnixPath(dir);
		if (SftpHelper.scp_ensureDir(getChannel(), unixDir)) {
			log.info(Messages.UploadMsg_DONT_UPLOAD_CAUSE_DIR_ALREADY_EXISTS);
			return;
		}
		SftpHelper.scp_mkdir(getChannel(), unixDir);
	}

	@Override
	public void transferFile(Path source,
			EnhancedFileAttributes localFileAttrs, Path dest,
			TransferBehavior tb) throws SshSessionException {
		String unixFile = SftpHelper.convertToUnixPath(dest);
		if (SftpHelper.scp_ensureFile(localFileAttrs, getChannel(), unixFile,
				tb)) {
			log.info(Messages.UploadMsg_DONT_UPLOAD_CAUSE_FILE_ALREADY_EXISTS);
			return;
		}
		SftpHelper.scp_put(getChannel(), source.toString(), unixFile);
	}

	@Override
	public void createSymlink() throws SshSessionException {
		Resource r = getResource();
		String unixLink = SftpHelper.convertToUnixPath(r.getDestination());
		String unixTarget = SftpHelper.convertToUnixPath(r
				.getSymbolicLinkTarget());
		if (SftpHelper.scp_ensureLink(getChannel(), unixTarget, unixLink)) {
			log.info(Messages.UploadMsg_DONT_UPLOAD_CAUSE_LINK_ALREADY_EXISTS);
			return;
		}
		SftpHelper.scp_symlink(getChannel(), unixTarget, unixLink);
	}

	@Override
	public void chmodFile() throws SshSessionException {
		Modifiers modifiers = getResource().getFileModifiers();
		if (modifiers == null) {
			return;
		}
		Path path = getResource().getDestination();
		String unixPath = SftpHelper.convertToUnixPath(path);
		SftpHelper.scp_chmod(getChannel(), modifiers, unixPath);
	}

	@Override
	public void chmodDir() throws SshSessionException {
		Modifiers modifiers = getResource().getDirModifiers();
		if (modifiers == null) {
			return;
		}
		Path path = getResource().getDestination();
		String unixPath = SftpHelper.convertToUnixPath(path);
		SftpHelper.scp_chmod(getChannel(), modifiers, unixPath);
	}

	@Override
	public void chgrp() throws SshSessionException {
		Path path = getResource().getDestination();
		GroupID group = getResource().getGroup();
		if (group == null) {
			return;
		}
		String unixPath = SftpHelper.convertToUnixPath(path);
		SftpHelper.scp_chgrp(getChannel(), group, unixPath);
	}

}