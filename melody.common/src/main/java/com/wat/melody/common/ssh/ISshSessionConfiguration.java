package com.wat.melody.common.ssh;

import com.wat.melody.common.network.Host;
import com.wat.melody.common.network.Port;
import com.wat.melody.common.ssh.exception.IllegalSshSessionConfigurationException;
import com.wat.melody.common.ssh.types.CompressionLevel;
import com.wat.melody.common.ssh.types.CompressionType;
import com.wat.melody.common.utils.GenericTimeout;
import com.wat.melody.common.utils.Timeout;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public interface ISshSessionConfiguration {

	public KnownHostsFile getKnownHosts();

	public KnownHostsFile setKnownHosts(KnownHostsFile knownHosts);

	public CompressionLevel getCompressionLevel();

	public CompressionLevel setCompressionLevel(
			CompressionLevel compressionLevel);

	public CompressionType getCompressionType();

	public CompressionType setCompressionType(CompressionType compressionType);

	public Timeout getConnectionTimeout();

	public Timeout setConnectionTimeout(GenericTimeout ival);

	public Timeout getReadTimeout();

	public Timeout setReadTimeout(GenericTimeout ival);

	public int getServerAliveCountMax();

	public int setServerAliveCountMax(int ival)
			throws IllegalSshSessionConfigurationException;

	public Timeout getServerAliveInterval();

	public Timeout setServerAliveInterval(GenericTimeout ival);

	public ProxyType getProxyType();

	public ProxyType setProxyType(ProxyType val);

	public Host getProxyHost();

	public Host setProxyHost(Host val);

	public Port getProxyPort();

	public Port setProxyPort(Port port);

}