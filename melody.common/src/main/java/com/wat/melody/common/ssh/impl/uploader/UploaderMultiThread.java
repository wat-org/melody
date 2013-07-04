package com.wat.melody.common.ssh.impl.uploader;

import java.io.IOException;
import java.util.List;

import com.jcraft.jsch.ChannelSftp;
import com.wat.melody.common.ssh.Messages;
import com.wat.melody.common.ssh.TemplatingHandler;
import com.wat.melody.common.ssh.filesfinder.LocalResource;
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

	private TemplatingHandler _templatingHandler;

	public UploaderMultiThread(SshSession session,
			List<ResourcesSpecification> rss, int maxPar, TemplatingHandler th) {
		super(session, rss, maxPar);
		setTemplatingHandler(th);
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
		for (ResourcesSpecification rs : getResourcesSpecifications()) {
			try { // Add all found LocalResource to the global list
				List<LocalResource> lrs;
				lrs = LocalResourcesFinder.findResources(rs);
				getResources().removeAll(lrs); // remove duplicated
				getResources().addAll(lrs);
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
		try {
			new UploaderNoThread(channel, r, getTemplatingHandler()).upload();
		} catch (UploaderException Ex) {
			throw new TransferException(Ex);
		}
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