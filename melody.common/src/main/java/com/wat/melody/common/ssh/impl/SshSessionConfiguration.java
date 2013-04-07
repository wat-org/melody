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

	private IKnownHostsRepository moKnownHosts;
	private CompressionLevel miCompressionLevel;
	private CompressionType msCompressionType;
	private ConnectionTimeout miConnectionTimeout;
	private ReadTimeout miReadTimeout;
	private ServerAliveMaxCount miServerAliveCountMax;
	private ServerAliveInterval miServerAliveInterval;
	private ProxyType moProxyType;
	private Host moProxyHost;
	private Port moProxyPort;

	public SshSessionConfiguration() {
		miCompressionLevel = CompressionLevel.NONE;
		msCompressionType = CompressionType.NONE;
		try {
			miServerAliveCountMax = ServerAliveMaxCount.parseInt(1);
			miConnectionTimeout = ConnectionTimeout.parseLong(15000);
			miReadTimeout = ReadTimeout.parseLong(60000);
			miServerAliveInterval = ServerAliveInterval.parseLong(10000);
		} catch (IllegalServerAliveMaxCountException | IllegalTimeoutException Ex) {
			throw new RuntimeException("Hard coded value is not valid. "
					+ "Source code have been modified and a bug have "
					+ "been introduced.", Ex);
		}
	}

	@Override
	public IKnownHostsRepository getKnownHosts() {
		return moKnownHosts;
	}

	@Override
	public IKnownHostsRepository setKnownHosts(IKnownHostsRepository knownHosts) {
		if (knownHosts == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ KnownHostsRepository.class.getCanonicalName() + ".");
		}
		IKnownHostsRepository previous = getKnownHosts();
		moKnownHosts = knownHosts;
		return previous;
	}

	@Override
	public CompressionLevel getCompressionLevel() {
		return miCompressionLevel;
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
		miCompressionLevel = compressionLevel;
		return previous;
	}

	@Override
	public CompressionType getCompressionType() {
		return msCompressionType;
	}

	@Override
	public CompressionType setCompressionType(CompressionType compressionType) {
		if (compressionType == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ CompressionType.class.getCanonicalName() + ".");
		}
		CompressionType previous = getCompressionType();
		msCompressionType = compressionType;
		return previous;
	}

	@Override
	public Timeout getConnectionTimeout() {
		return miConnectionTimeout;
	}

	@Override
	public Timeout setConnectionTimeout(ConnectionTimeout ival) {
		if (ival == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ ConnectionTimeout.class.getCanonicalName() + ".");
		}
		Timeout previous = getConnectionTimeout();
		miConnectionTimeout = ival;
		return previous;
	}

	@Override
	public Timeout getReadTimeout() {
		return miReadTimeout;
	}

	@Override
	public Timeout setReadTimeout(ReadTimeout ival) {
		if (ival == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + ReadTimeout.class.getCanonicalName()
					+ ".");
		}
		Timeout previous = getReadTimeout();
		miReadTimeout = ival;
		return previous;
	}

	@Override
	public ServerAliveMaxCount getServerAliveCountMax() {
		return miServerAliveCountMax;
	}

	@Override
	public ServerAliveMaxCount setServerAliveCountMax(ServerAliveMaxCount ival) {
		if (ival == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ ServerAliveMaxCount.class.getCanonicalName() + ".");
		}
		ServerAliveMaxCount previous = getServerAliveCountMax();
		miServerAliveCountMax = ival;
		return previous;
	}

	@Override
	public Timeout getServerAliveInterval() {
		return miServerAliveInterval;
	}

	@Override
	public Timeout setServerAliveInterval(ServerAliveInterval ival) {
		if (ival == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ ServerAliveInterval.class.getCanonicalName() + ".");
		}
		Timeout previous = getServerAliveInterval();
		miServerAliveInterval = ival;
		return previous;
	}

	@Override
	public ProxyType getProxyType() {
		return moProxyType;
	}

	@Override
	public ProxyType setProxyType(ProxyType val) {
		if (val == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + ProxyType.class.getCanonicalName()
					+ ".");
		}
		ProxyType previous = getProxyType();
		moProxyType = val;
		return previous;
	}

	@Override
	public Host getProxyHost() {
		return moProxyHost;
	}

	@Override
	public Host setProxyHost(Host val) {
		if (val == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Host.class.getCanonicalName() + ".");
		}
		Host previous = getProxyHost();
		moProxyHost = val;
		return previous;
	}

	@Override
	public Port getProxyPort() {
		return moProxyPort;
	}

	@Override
	public Port setProxyPort(Port port) {
		if (port == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Port.class.getCanonicalName() + ".");
		}
		Port previous = getProxyPort();
		moProxyPort = port;
		return previous;
	}

}