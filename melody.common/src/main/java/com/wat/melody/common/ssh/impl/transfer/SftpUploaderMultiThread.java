package com.wat.melody.common.ssh.impl.transfer;

import java.util.List;

import com.jcraft.jsch.ChannelSftp;
import com.wat.melody.common.files.FileSystem;
import com.wat.melody.common.files.LocalFileSystem;
import com.wat.melody.common.ssh.impl.SshSession;
import com.wat.melody.common.transfer.TemplatingHandler;
import com.wat.melody.common.transfer.TransferableFileSystem;
import com.wat.melody.common.transfer.resources.ResourcesSpecification;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class SftpUploaderMultiThread extends SftpBaseTransferMultiThread {

	public SftpUploaderMultiThread(SshSession session,
			List<ResourcesSpecification> rss, int maxPar, TemplatingHandler th) {
		super(session, rss, maxPar, th);
	}

	@Override
	public String getThreadName() {
		return "uploader";
	}

	@Override
	public String getSourceSystemDescription() {
		return "local file system";
	}

	@Override
	public String getDestinationSystemDescription() {
		return getSession().getConnectionDatas().toString();
	}

	@Override
	public FileSystem newSourceFileSystem() {
		return new LocalFileSystem();
	}

	@Override
	public TransferableFileSystem newDestinationFileSystem() {
		ChannelSftp channel = getSession().openSftpChannel();
		return new SftpFileSystem4Upload(channel, getTemplatingHandler());
	}

}