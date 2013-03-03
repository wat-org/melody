package com.wat.melody.cloud.network;

import com.wat.melody.cloud.network.exception.NetworkManagementException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public interface NetworkManager {

	public ManagementNetworkDatas getManagementDatas();

	/**
	 * @throws NetworkManagementException
	 * @throws InterruptedException
	 */
	public void enableNetworkManagement()
			throws NetworkManagementException, InterruptedException;

	/**
	 * 
	 * @throws NetworkManagementException
	 */
	public void disableNetworkManagement() throws NetworkManagementException;

}
