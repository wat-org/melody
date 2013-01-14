package com.wat.melody.common.ssh.impl;

import com.wat.melody.common.network.Host;
import com.wat.melody.common.network.Port;
import com.wat.melody.common.ssh.ISshConnectionDatas;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class SshConnectionDatas implements ISshConnectionDatas {

	private Host moHost = null;
	private Port moPort = Port.SSH;
	private Boolean mbTrust = false;

	@Override
	public Host getHost() {
		return moHost;
	}

	@Override
	public Host setHost(Host host) {
		if (host == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Cannot be null.");
		}
		Host previous = getHost();
		moHost = host;
		return previous;
	}

	@Override
	public Port getPort() {
		return moPort;
	}

	@Override
	public Port setPort(Port port) {
		if (port == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Cannot be null.");
		}
		Port previous = getPort();
		moPort = port;
		return previous;
	}

	@Override
	public boolean getTrust() {
		return mbTrust;
	}

	@Override
	public boolean setTrust(boolean b) {
		boolean previous = getTrust();
		mbTrust = b;
		return previous;
	}

}
