package com.wat.melody.common.ssh.impl;

import com.wat.melody.common.network.Host;
import com.wat.melody.common.network.Port;
import com.wat.melody.common.ssh.IKnownHostsRepository;
import com.wat.melody.common.ssh.ISshSessionConfiguration;
import com.wat.melody.common.ssh.types.CompressionLevel;
import com.wat.melody.common.ssh.types.CompressionType;
import com.wat.melody.common.ssh.types.ConnectionTimeout;
import com.wat.melody.common.ssh.types.ProxyType;
import com.wat.melody.common.ssh.types.ReadTimeout;
import com.wat.melody.common.ssh.types.ServerAliveInterval;
import com.wat.melody.common.ssh.types.ServerAliveMaxCount;
import com.wat.melody.common.ssh.types.exception.IllegalServerAliveMaxCountException;
import com.wat.melody.common.timeout.Timeout;
import com.wat.melody.common.timeout.exception.IllegalTimeoutException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class SshSessionConfiguration implements ISshSessionConfiguration {

	private static ConnectionTimeout createConnectionTimeout(long timeout) {
		try {
			return ConnectionTimeout.parseLong(timeout);
		} catch (IllegalTimeoutException Ex) {
			throw new RuntimeException("Unexpected error while initializing "
					+ "a ConnectionTimeout with value '" + timeout + "'. "
					+ "Because this default value initialization is "
					+ "hardcoded, such error cannot happened. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.", Ex);
		}
	}

	private static ReadTimeout createReadTimeout(long timeout) {
		try {
			return ReadTimeout.parseLong(timeout);
		} catch (IllegalTimeoutException Ex) {
			throw new RuntimeException("Unexpected error while initializing "
					+ "a ReadTimeout with value '" + timeout + "'. "
					+ "Because this default value initialization is "
					+ "hardcoded, such error cannot happened. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.", Ex);
		}
	}

	private static ServerAliveMaxCount createServerAliveMaxCount(int maxcount) {
		try {
			return ServerAliveMaxCount.parseInt(maxcount);
		} catch (IllegalServerAliveMaxCountException Ex) {
			throw new RuntimeException("Unexpected error while initializing "
					+ "a ServerAliveMaxCount with value '" + maxcount + "'. "
					+ "Because this default value initialization is "
					+ "hardcoded, such error cannot happened. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.", Ex);
		}
	}

	private static ServerAliveInterval createServerAliveInterval(long interval) {
		try {
			return ServerAliveInterval.parseLong(interval);
		} catch (IllegalTimeoutException Ex) {
			throw new RuntimeException("Unexpected error while initializing "
					+ "a ServerAliveInterval with value '" + interval + "'. "
					+ "Because this default value initialization is "
					+ "hardcoded, such error cannot happened. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.", Ex);
		}
	}

	private static CompressionLevel DEFAULT_COMPRESSION_LEVEL = CompressionLevel.NONE;
	private static CompressionType DEFAULT_COMPRESSION_TYPE = CompressionType.NONE;
	private static ConnectionTimeout DEFAULT_CONNECTION_TIMEOUT = createConnectionTimeout(15000);;
	private static ReadTimeout DEFAULT_READ_TIMEOUT = createReadTimeout(60000);;
	private static ServerAliveMaxCount DEFAULT_SERVER_ALIVE_MAX_COUNT = createServerAliveMaxCount(1);;
	private static ServerAliveInterval DEFAULT_SERVER_ALIVE_INTERVAL = createServerAliveInterval(10000);;

	private IKnownHostsRepository _knownHosts = null;
	private CompressionLevel _compressionLevel = DEFAULT_COMPRESSION_LEVEL;
	private CompressionType _compressionType = DEFAULT_COMPRESSION_TYPE;
	private ConnectionTimeout _connectionTimeout = DEFAULT_CONNECTION_TIMEOUT;
	private ReadTimeout _readTimeout = DEFAULT_READ_TIMEOUT;
	private ServerAliveMaxCount _serverAliveMaxCount = DEFAULT_SERVER_ALIVE_MAX_COUNT;
	private ServerAliveInterval _serverAliveInterval = DEFAULT_SERVER_ALIVE_INTERVAL;
	private ProxyType _proxyType = null;
	private Host _proxyHost = null;
	private Port _proxyPort = null;

	public SshSessionConfiguration() {
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder("{ ");
		str.append("compression-level:");
		str.append(getCompressionLevel());
		str.append(", compression-type:");
		str.append(getCompressionType());
		str.append(", connection-timeout:");
		str.append(getConnectionTimeout());
		str.append(", read-timeout:");
		str.append(getReadTimeout());
		str.append(", server-alive-max-count:");
		str.append(getServerAliveMaxCount());
		str.append(", server-alive-interval:");
		str.append(getServerAliveInterval());
		if (getKnownHosts() != null) {
			str.append(", knowhosts:");
			str.append(getKnownHosts());
		}
		if (getProxyType() != null) {
			str.append(", proxy-type:");
			str.append(getProxyType());
			str.append(", proxy-host:");
			str.append(getProxyHost());
			str.append(", proxy-port:");
			str.append(getProxyPort());
		}
		str.append(" }");
		return str.toString();
	}

	@Override
	public IKnownHostsRepository getKnownHosts() {
		return _knownHosts;
	}

	@Override
	public IKnownHostsRepository setKnownHosts(IKnownHostsRepository knownHosts) {
		// can be null
		IKnownHostsRepository previous = getKnownHosts();
		_knownHosts = knownHosts;
		return previous;
	}

	@Override
	public CompressionLevel getCompressionLevel() {
		return _compressionLevel;
	}

	@Override
	public CompressionLevel setCompressionLevel(
			CompressionLevel compressionLevel) {
		if (compressionLevel == null) {
			compressionLevel = DEFAULT_COMPRESSION_LEVEL;
		}
		CompressionLevel previous = getCompressionLevel();
		_compressionLevel = compressionLevel;
		return previous;
	}

	@Override
	public CompressionType getCompressionType() {
		return _compressionType;
	}

	@Override
	public CompressionType setCompressionType(CompressionType compressionType) {
		if (compressionType == null) {
			compressionType = DEFAULT_COMPRESSION_TYPE;
		}
		CompressionType previous = getCompressionType();
		_compressionType = compressionType;
		return previous;
	}

	@Override
	public Timeout getConnectionTimeout() {
		return _connectionTimeout;
	}

	@Override
	public Timeout setConnectionTimeout(ConnectionTimeout timeout) {
		if (timeout == null) {
			timeout = DEFAULT_CONNECTION_TIMEOUT;
		}
		Timeout previous = getConnectionTimeout();
		_connectionTimeout = timeout;
		return previous;
	}

	@Override
	public Timeout getReadTimeout() {
		return _readTimeout;
	}

	@Override
	public Timeout setReadTimeout(ReadTimeout timeout) {
		if (timeout == null) {
			timeout = DEFAULT_READ_TIMEOUT;
		}
		Timeout previous = getReadTimeout();
		_readTimeout = timeout;
		return previous;
	}

	@Override
	public ServerAliveMaxCount getServerAliveMaxCount() {
		return _serverAliveMaxCount;
	}

	@Override
	public ServerAliveMaxCount setServerAliveMaxCount(
			ServerAliveMaxCount maxcount) {
		if (maxcount == null) {
			maxcount = DEFAULT_SERVER_ALIVE_MAX_COUNT;
		}
		ServerAliveMaxCount previous = getServerAliveMaxCount();
		_serverAliveMaxCount = maxcount;
		return previous;
	}

	@Override
	public Timeout getServerAliveInterval() {
		return _serverAliveInterval;
	}

	@Override
	public Timeout setServerAliveInterval(ServerAliveInterval interval) {
		if (interval == null) {
			interval = DEFAULT_SERVER_ALIVE_INTERVAL;
		}
		Timeout previous = getServerAliveInterval();
		_serverAliveInterval = interval;
		return previous;
	}

	@Override
	public ProxyType getProxyType() {
		return _proxyType;
	}

	@Override
	public ProxyType setProxyType(ProxyType val) {
		// can be null, when no proxy is used
		ProxyType previous = getProxyType();
		_proxyType = val;
		return previous;
	}

	@Override
	public Host getProxyHost() {
		return _proxyHost;
	}

	@Override
	public Host setProxyHost(Host val) {
		if (val == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Host.class.getCanonicalName() + ".");
		}
		Host previous = getProxyHost();
		_proxyHost = val;
		return previous;
	}

	@Override
	public Port getProxyPort() {
		return _proxyPort;
	}

	@Override
	public Port setProxyPort(Port port) {
		if (port == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Port.class.getCanonicalName() + ".");
		}
		Port previous = getProxyPort();
		_proxyPort = port;
		return previous;
	}

}