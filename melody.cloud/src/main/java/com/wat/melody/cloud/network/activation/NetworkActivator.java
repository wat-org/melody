package com.wat.melody.cloud.network.activation;

import com.wat.melody.cloud.network.activation.exception.NetworkActivationException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public interface NetworkActivator {

	public NetworkActivationDatas getDatas();

	/**
	 * @throws NetworkActivationException
	 * @throws InterruptedException
	 */
	public void enableNetworkActivation() throws NetworkActivationException,
			InterruptedException;

	/**
	 * @throws NetworkActivationException
	 * @throws InterruptedException
	 */
	public void disableNetworkActivation() throws NetworkActivationException,
			InterruptedException;

}