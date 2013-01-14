package com.wat.melody.common.ssh.impl;

import com.wat.melody.common.network.Host;
import com.wat.melody.common.network.Port;
import com.wat.melody.common.ssh.CompressionLevel;
import com.wat.melody.common.ssh.CompressionType;
import com.wat.melody.common.ssh.ISshSessionConfiguration;
import com.wat.melody.common.ssh.KnownHostsFile;
import com.wat.melody.common.ssh.Messages;
import com.wat.melody.common.ssh.ProxyType;
import com.wat.melody.common.ssh.exception.IllegalSshSessionConfigurationException;
import com.wat.melody.common.utils.GenericTimeout;
import com.wat.melody.common.utils.Timeout;
import com.wat.melody.common.utils.exception.IllegalTimeoutException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class SshSessionConfiguration implements ISshSessionConfiguration {

	private KnownHostsFile moKnownHosts;
	private CompressionLevel miCompressionLevel;
	private CompressionType msCompressionType;
	private Timeout miConnectionTimeout;
	private Timeout miReadTimeout;
	private int miServerAliveCountMax;
	private Timeout miServerAliveInterval;
	private ProxyType moProxyType;
	private Host moProxyHost;
	private Port moProxyPort;

	public SshSessionConfiguration() {
		miCompressionLevel = CompressionLevel.NONE;
		msCompressionType = CompressionType.NONE;
		miServerAliveCountMax = 1;
		try {
			miConnectionTimeout = GenericTimeout.parseLong(15000);
			miReadTimeout = GenericTimeout.parseLong(60000);
			miServerAliveInterval = GenericTimeout.parseLong(10000);
		} catch (IllegalTimeoutException Ex) {
			throw new RuntimeException("Hard coded value is not valid. "
					+ "Source code have been modified and a bug have "
					+ "been introduced.", Ex);
		}
	}

	@Override
	public KnownHostsFile getKnownHosts() {
		return moKnownHosts;
	}

	@Override
	public KnownHostsFile setKnownHosts(KnownHostsFile knownHosts) {
		if (knownHosts == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid File (a KnownHosts File).");
		}
		KnownHostsFile previous = getKnownHosts();
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
					+ "Must be a valid String (a CompressionLevel).");
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
					+ "Must be a valid String (a CompressionType).");
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
	public Timeout setConnectionTimeout(GenericTimeout ival) {
		if (ival == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ GenericTimeout.class.getCanonicalName() + ".");
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
	public Timeout setReadTimeout(GenericTimeout ival) {
		if (ival == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ GenericTimeout.class.getCanonicalName() + ".");
		}
		Timeout previous = getReadTimeout();
		miReadTimeout = ival;
		return previous;
	}

	@Override
	public int getServerAliveCountMax() {
		return miServerAliveCountMax;
	}

	@Override
	public int setServerAliveCountMax(int ival)
			throws IllegalSshSessionConfigurationException {
		if (ival < 0) {
			throw new IllegalSshSessionConfigurationException(Messages.bind(
					Messages.ConfEx_INVALID_SERVER_ALIVE_MAX_COUNT, ival));
		}
		int previous = getServerAliveCountMax();
		miServerAliveCountMax = ival;
		return previous;
	}

	@Override
	public Timeout getServerAliveInterval() {
		return miServerAliveInterval;
	}

	@Override
	public Timeout setServerAliveInterval(GenericTimeout ival) {
		if (ival == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ GenericTimeout.class.getCanonicalName() + ".");
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
					+ "Must be a valid Host.");
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
					+ "Must be a valid Port.");
		}
		Port previous = getProxyPort();
		moProxyPort = port;
		return previous;
	}

}