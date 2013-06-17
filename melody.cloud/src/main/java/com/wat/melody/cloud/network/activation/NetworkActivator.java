package com.wat.melody.cloud.network.activation;

import com.wat.melody.cloud.network.activation.exception.NetworkActivationException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public interface NetworkActivator {

	/**
	 * @return the {@link NetworkActivationDatas}.
	 * 
	 * @throws NetworkActivationException
	 *             if it fails to retrieve the Network Activation Datas.
	 */
	public NetworkActivationDatas getNetworkActivationDatas()
			throws NetworkActivationException;

	/**
	 * @throws NetworkActivationException
	 *             if an error occurred during Network Activation enablement.
	 * @throws InterruptedException
	 *             if the operation have been interrupted.
	 */
	public void enableNetworkActivation() throws NetworkActivationException,
			InterruptedException;

	/**
	 * @throws NetworkActivationException
	 *             if an error occurred during Network Activation disablement.
	 * @throws InterruptedException
	 *             if the operation have been interrupted.
	 */
	public void disableNetworkActivation() throws NetworkActivationException,
			InterruptedException;

}