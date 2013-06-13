package com.wat.melody.cloud.network.activation;

import com.wat.melody.cloud.network.activation.exception.NetworkActivationException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public interface NetworkActivator {

	/**
	 * @return the {@link NetworkActivationDatas}, or <tt>null</tt>. If the
	 *         <tt>null</tt>, calls to {@link #enableNetworkActivation()} and
	 *         {@link #disableNetworkActivation()} will no do anything.
	 */
	public NetworkActivationDatas getNetworkActivationDatas();

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