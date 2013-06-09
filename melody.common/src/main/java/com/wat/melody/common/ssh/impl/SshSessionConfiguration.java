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

	private IKnownHostsRepository _knownHosts;
	private CompressionLevel _compressionLevel;
	private CompressionType _compressionType;
	private ConnectionTimeout _connectionTimeout;
	private ReadTimeout _readTimeout;
	private ServerAliveMaxCount _serverAliveCountMax;
	private ServerAliveInterval _serverAliveInterval;
	private ProxyType _proxyType;
	private Host _proxyHost;
	private Port _proxyPort;

	public SshSessionConfiguration() {
		_compressionLevel = CompressionLevel.NONE;
		_compressionType = CompressionType.NONE;
		try {
			_serverAliveCountMax = ServerAliveMaxCount.parseInt(1);
			_connectionTimeout = ConnectionTimeout.parseLong(15000);
			_readTimeout = ReadTimeout.parseLong(60000);
			_serverAliveInterval = ServerAliveInterval.parseLong(10000);
		} catch (IllegalServerAliveMaxCountException | IllegalTimeoutException Ex) {
			throw new RuntimeException("Hard coded value is not valid. "
					+ "Source code have been modified and a bug have "
					+ "been introduced.", Ex);
		}
	}

	@Override
	public String toString() {
		// TODO : write the toString() method of SshSessionConfiguration
		return "TODO";
	}

	@Override
	public IKnownHostsRepository getKnownHosts() {
		return _knownHosts;
	}

	@Override
	public IKnownHostsRepository setKnownHosts(IKnownHostsRepository knownHosts) {
		if (knownHosts == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ KnownHostsRepository.class.getCanonicalName() + ".");
		}
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
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ CompressionLevel.class.getCanonicalName() + ".");
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
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ CompressionType.class.getCanonicalName() + ".");
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
	public Timeout setConnectionTimeout(ConnectionTimeout ival) {
		if (ival == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ ConnectionTimeout.class.getCanonicalName() + ".");
		}
		Timeout previous = getConnectionTimeout();
		_connectionTimeout = ival;
		return previous;
	}

	@Override
	public Timeout getReadTimeout() {
		return _readTimeout;
	}

	@Override
	public Timeout setReadTimeout(ReadTimeout ival) {
		if (ival == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + ReadTimeout.class.getCanonicalName()
					+ ".");
		}
		Timeout previous = getReadTimeout();
		_readTimeout = ival;
		return previous;
	}

	@Override
	public ServerAliveMaxCount getServerAliveCountMax() {
		return _serverAliveCountMax;
	}

	@Override
	public ServerAliveMaxCount setServerAliveCountMax(ServerAliveMaxCount ival) {
		if (ival == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ ServerAliveMaxCount.class.getCanonicalName() + ".");
		}
		ServerAliveMaxCount previous = getServerAliveCountMax();
		_serverAliveCountMax = ival;
		return previous;
	}

	@Override
	public Timeout getServerAliveInterval() {
		return _serverAliveInterval;
	}

	@Override
	public Timeout setServerAliveInterval(ServerAliveInterval ival) {
		if (ival == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ ServerAliveInterval.class.getCanonicalName() + ".");
		}
		Timeout previous = getServerAliveInterval();
		_serverAliveInterval = ival;
		return previous;
	}

	@Override
	public ProxyType getProxyType() {
		return _proxyType;
	}

	@Override
	public ProxyType setProxyType(ProxyType val) {
		if (val == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + ProxyType.class.getCanonicalName()
					+ ".");
		}
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