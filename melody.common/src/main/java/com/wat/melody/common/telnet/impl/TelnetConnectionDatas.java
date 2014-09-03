package com.wat.melody.common.telnet.impl;

import com.wat.melody.common.network.Host;
import com.wat.melody.common.network.Port;
import com.wat.melody.common.telnet.ITelnetConnectionDatas;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class TelnetConnectionDatas implements ITelnetConnectionDatas {

	private Host _host = null;
	private Port _port = Port.TELNET;

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder("{ ");
		str.append("host:");
		str.append(getHost());
		str.append(", port:");
		str.append(getPort());
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

}