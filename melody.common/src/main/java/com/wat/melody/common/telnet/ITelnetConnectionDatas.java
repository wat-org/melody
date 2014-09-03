package com.wat.melody.common.telnet;

import com.wat.melody.common.network.Host;
import com.wat.melody.common.network.Port;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public interface ITelnetConnectionDatas {

	public Host getHost();

	public Host setHost(Host host);

	public Port getPort();

	public Port setPort(Port port);

}