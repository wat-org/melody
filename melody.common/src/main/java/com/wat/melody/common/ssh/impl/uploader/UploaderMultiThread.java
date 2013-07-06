package com.wat.melody.common.ssh.impl.uploader;

import java.io.IOException;
import java.util.List;

import com.jcraft.jsch.ChannelSftp;
import com.wat.melody.common.ssh.Messages;
import com.wat.melody.common.ssh.TemplatingHandler;
import com.wat.melody.common.ssh.filesfinder.LocalResourcesFinder;
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
public class UploaderMultiThread extends TransferMultiThread {

	public UploaderMultiThread(SshSession session,
			List<ResourcesSpecification> rss, int maxPar, TemplatingHandler th) {
		super(session, rss, maxPar, th);
	}

	@Override
	public String getThreadName() {
		return "uploader";
	}

	@Override
	public String getSourceSystem() {
		return "local";
	}

	@Override
	public String getDestinationSystem() {
		return getSession().getConnectionDatas().toString();
	}

	@Override
	public String getTransferProtocol() {
		return "sftp";
	}

	@Override
	public void computeResources() throws TransferException {
		for (ResourcesSpecification rspec : getResourcesSpecifications()) {
			try { // Add all found LocalResource to the global list
				List<Resource> rs;
				rs = LocalResourcesFinder.findResources(rspec);
				getResources().removeAll(rs); // remove duplicated
				getResources().addAll(rs);
			} catch (IOException Ex) {
				throw new TransferException(
						Messages.UploadEx_IO_ERROR_WHILE_FINDING, Ex);
			}
		}
		for (Resource r : getResources()) {
			System.out.println(r);
		}
	}

	@Override
	public void transfer(ChannelSftp channel, Resource r)
			throws TransferException {
		new UploaderNoThread(channel, r, getTemplatingHandler()).transfer();
	}

}