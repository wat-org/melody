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

	private Host _host = null;
	private Port _port = Port.SSH;
	private Boolean _trust = false;

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder("{ ");
		str.append("host:");
		str.append(getHost());
		str.append(", port:");
		str.append(getPort());
		str.append(", trusted:");
		str.append(getTrust());
		str.append(" }");
		return str.toString();
	}

	@Override
	public Host getHost() {
		return _host;
	}

	@Override
	public Host setHost(Host host) {
		if (host == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Cannot be null.");
		}
		Host previous = getHost();
		_host = host;
		return previous;
	}

	@Override
	public Port getPort() {
		return _port;
	}

	@Override
	public Port setPort(Port port) {
		if (port == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Cannot be null.");
		}
		Port previous = getPort();
		_port = port;
		return previous;
	}

	@Override
	public boolean getTrust() {
		return _trust;
	}

	@Override
	public boolean setTrust(boolean b) {
		boolean previous = getTrust();
		_trust = b;
		return previous;
	}

}
