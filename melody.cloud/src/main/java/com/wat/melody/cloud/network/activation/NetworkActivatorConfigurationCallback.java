package com.wat.melody.cloud.network.activation;

import com.wat.melody.common.ssh.ISshSessionConfiguration;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public interface NetworkActivatorConfigurationCallback {

	public ISshSessionConfiguration getSshConfiguration();

	/*
	 * TODO : add a method to retrieve a WinRM configuration
	 */

}