package com.wat.melody.common.ssh;

import com.wat.melody.common.network.Host;
import com.wat.melody.common.network.Port;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public interface ISshConnectionDatas {

	public Host getHost();

	public Host setHost(Host host);

	public Port getPort();

	public Port setPort(Port port);

	public boolean getTrust();

	public boolean setTrust(boolean b);

}