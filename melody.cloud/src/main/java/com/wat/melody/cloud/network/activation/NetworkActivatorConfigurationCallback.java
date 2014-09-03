package com.wat.melody.cloud.network.activation;

import com.wat.melody.common.ssh.ISshSessionConfiguration;
import com.wat.melody.common.telnet.ITelnetSessionConfiguration;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public interface NetworkActivatorConfigurationCallback {

	public ISshSessionConfiguration getSshConfiguration();

	public ITelnetSessionConfiguration getTelnetConfiguration();

	/*
	 * TODO : add a method to retrieve a WinRM configuration
	 */

}