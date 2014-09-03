package com.wat.melody.common.ssh;

import com.wat.melody.common.network.Host;
import com.wat.melody.common.network.Port;
import com.wat.melody.common.ssh.types.CompressionLevel;
import com.wat.melody.common.ssh.types.CompressionType;
import com.wat.melody.common.ssh.types.ConnectionRetry;
import com.wat.melody.common.ssh.types.ConnectionTimeout;
import com.wat.melody.common.ssh.types.ProxyType;
import com.wat.melody.common.ssh.types.ReadTimeout;
import com.wat.melody.common.ssh.types.ServerAliveInterval;
import com.wat.melody.common.ssh.types.ServerAliveMaxCount;

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

	public ConnectionTimeout getConnectionTimeout();

	public ConnectionTimeout setConnectionTimeout(ConnectionTimeout val);

	public ConnectionRetry getConnectionRetry();

	public ConnectionRetry setConnectionRetry(ConnectionRetry val);

	public ReadTimeout getReadTimeout();

	public ReadTimeout setReadTimeout(ReadTimeout val);

	public ServerAliveMaxCount getServerAliveMaxCount();

	public ServerAliveMaxCount setServerAliveMaxCount(ServerAliveMaxCount val);

	public ServerAliveInterval getServerAliveInterval();

	public ServerAliveInterval setServerAliveInterval(ServerAliveInterval val);

	public ProxyType getProxyType();

	public ProxyType setProxyType(ProxyType val);

	public Host getProxyHost();

	public Host setProxyHost(Host val);

	public Port getProxyPort();

	public Port setProxyPort(Port port);

}