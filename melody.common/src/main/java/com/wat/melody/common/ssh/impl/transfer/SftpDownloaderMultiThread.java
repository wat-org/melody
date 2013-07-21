package com.wat.melody.common.ssh.impl.transfer;

import java.util.List;

import com.jcraft.jsch.ChannelSftp;
import com.wat.melody.common.files.FileSystem;
import com.wat.melody.common.files.LocalFileSystem;
import com.wat.melody.common.ssh.impl.SshSession;
import com.wat.melody.common.transfer.TemplatingHandler;
import com.wat.melody.common.transfer.TransferMultiThread;
import com.wat.melody.common.transfer.TransferNoThread;
import com.wat.melody.common.transfer.Transferable;
import com.wat.melody.common.transfer.resources.ResourcesSpecification;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class SftpDownloaderMultiThread extends TransferMultiThread {

	private SshSession _session;

	public SftpDownloaderMultiThread(SshSession session,
			List<ResourcesSpecification> rss, int maxPar, TemplatingHandler th) {
		super(rss, maxPar, th);
		setSession(session);
	}

	@Override
	public String getThreadName() {
		return "downloader";
	}

	@Override
	public String getSourceSystemDescription() {
		return getSession().getConnectionDatas().toString();
	}

	@Override
	public String getDestinationSystemDescription() {
		return "local";
	}

	@Override
	public String getTransferProtocolDescription() {
		return "sftp";
	}

	@Override
	public FileSystem newSourceFileSystem() {
		ChannelSftp channel = getSession().openSftpChannel();
		return new SftpFileSystem(channel);
	}

	@Override
	public FileSystem newDestinationFileSystem() {
		return new LocalFileSystem();
	}

	@Override
	public TransferNoThread newTransferNoThread(FileSystem sourceFileSystem,
			FileSystem destinationFileSystem, Transferable r) {
		return new DownloaderNoThread((SftpFileSystem) sourceFileSystem,
				destinationFileSystem, r, getTemplatingHandler());
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

}