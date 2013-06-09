package com.wat.melody.cloud.network.activation;

import com.wat.melody.cloud.network.activation.exception.NetworkActivationException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public interface NetworkActivator {

	public NetworkActivationDatas getManagementDatas();

	/**
	 * @throws NetworkActivationException
	 * @throws InterruptedException
	 */
	public void enableNetworkManagement() throws NetworkActivationException,
			InterruptedException;

	/**
	 * 
	 * @throws NetworkActivationException
	 */
	public void disableNetworkManagement() throws NetworkActivationException;

}
