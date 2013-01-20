package com.wat.melody.common.ssh;

import com.wat.melody.common.network.Host;
import com.wat.melody.common.ssh.types.HostKeyType;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public interface IHostKey {

	public byte[] getBytes();

	public HostKeyType getType();

	public Host getHost();

}