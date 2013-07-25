package com.wat.melody.common.ssh.impl.transfer;

import java.util.List;

import com.wat.melody.common.ssh.impl.SshSession;
import com.wat.melody.common.transfer.TemplatingHandler;
import com.wat.melody.common.transfer.TransferMultiThread;
import com.wat.melody.common.transfer.resources.ResourcesSpecification;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class SftpBaseTransferMultiThread extends TransferMultiThread {

	private SshSession _session;

	public SftpBaseTransferMultiThread(SshSession session,
			List<ResourcesSpecification> rss, int maxPar, TemplatingHandler th) {
		super(rss, maxPar, th);
		setSession(session);
	}

	@Override
	public String getTransferProtocolDescription() {
		return "sftp";
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