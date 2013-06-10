package com.wat.melody.common.ssh;

import com.wat.melody.common.network.Host;
import com.wat.melody.common.network.Port;
import com.wat.melody.common.ssh.types.CompressionLevel;
import com.wat.melody.common.ssh.types.CompressionType;
import com.wat.melody.common.ssh.types.ConnectionTimeout;
import com.wat.melody.common.ssh.types.ProxyType;
import com.wat.melody.common.ssh.types.ReadTimeout;
import com.wat.melody.common.ssh.types.ServerAliveInterval;
import com.wat.melody.common.ssh.types.ServerAliveMaxCount;
import com.wat.melody.common.timeout.Timeout;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public interface ISshSessionConfiguration {

	public IKnownHostsRepository getKnownHosts();

	public IKnownHostsRepository setKnownHosts(IKnownHostsRepository knownHosts);

	public CompressionLevel getCompressionLevel();

	public CompressionLevel setCompressionLevel(
			CompressionLevel compressionLevel);

	public CompressionType getCompressionType();

	public CompressionType setCompressionType(CompressionType compressionType);

	public Timeout getConnectionTimeout();

	public Timeout setConnectionTimeout(ConnectionTimeout ival);

	public Timeout getReadTimeout();

	public Timeout setReadTimeout(ReadTimeout ival);

	public ServerAliveMaxCount getServerAliveMaxCount();

	public ServerAliveMaxCount setServerAliveMaxCount(ServerAliveMaxCount ival);

	public Timeout getServerAliveInterval();

	public Timeout setServerAliveInterval(ServerAliveInterval ival);

	public ProxyType getProxyType();

	public ProxyType setProxyType(ProxyType val);

	public Host getProxyHost();

	public Host setProxyHost(Host val);

	public Port getProxyPort();

	public Port setProxyPort(Port port);

}