package com.wat.melody.cloud.network;

import com.wat.melody.common.ssh.ISshSessionConfiguration;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public interface NetworkManagerFactoryConfigurationCallback {

	public ISshSessionConfiguration getSshConfiguration();

	/*
	 * TODO : add a method to retrieve a WinRM configuration
	 */

}
