package com.wat.melody.common.ssh.impl.downloader;

import java.util.List;

import com.jcraft.jsch.ChannelSftp;
import com.wat.melody.common.ssh.Messages;
import com.wat.melody.common.ssh.TemplatingHandler;
import com.wat.melody.common.ssh.exception.SshSessionException;
import com.wat.melody.common.ssh.filesfinder.RemoteResourcesFinder;
import com.wat.melody.common.ssh.filesfinder.Resource;
import com.wat.melody.common.ssh.filesfinder.ResourcesSpecification;
import com.wat.melody.common.ssh.impl.SshSession;
import com.wat.melody.common.ssh.impl.transfer.TransferException;
import com.wat.melody.common.ssh.impl.transfer.TransferMultiThread;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class DownloaderMultiThread extends TransferMultiThread {

	public DownloaderMultiThread(SshSession session,
			List<ResourcesSpecification> rss, int maxPar, TemplatingHandler th) {
		super(session, rss, maxPar, th);
	}

	@Override
	public String getThreadName() {
		return "downloader";
	}

	@Override
	public String getSourceSystem() {
		return getSession().getConnectionDatas().toString();
	}

	@Override
	public String getDestinationSystem() {
		return "local";
	}

	@Override
	public String getTransferProtocol() {
		return "sftp";
	}

	@Override
	public void computeResources() throws TransferException {
		ChannelSftp chan = null;
		try {
			chan = getSession().openSftpChannel();
			for (ResourcesSpecification rspec : getResourcesSpecifications()) {
				try {
					List<Resource> rs;
					rs = RemoteResourcesFinder.findResources(chan, rspec);
					getResources().removeAll(rs); // remove duplicated
					getResources().addAll(rs);
				} catch (SshSessionException Ex) {
					throw new TransferException(
							Messages.DownloadEx_IO_ERROR_WHILE_FINDING, Ex);
				}
			}
		} finally {
			if (chan != null) {
				chan.disconnect();
			}
		}
		for (Resource r : getResources()) {
			System.out.println(r);
		}
	}

	@Override
	public void transfer(ChannelSftp channel, Resource rr)
			throws TransferException {
		try {
			new DownloaderNoThread(channel, rr, getTemplatingHandler())
					.download();
		} catch (DownloaderException Ex) {
			throw new TransferException(Ex);
		}
	}

}