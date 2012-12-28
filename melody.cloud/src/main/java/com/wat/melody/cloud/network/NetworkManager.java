package com.wat.melody.cloud.network;

import com.wat.melody.api.ITaskContext;
import com.wat.melody.cloud.network.exception.ManagementException;

public interface NetworkManager {

	public ITaskContext getContext();

	public NetworkManagementDatas getManagementDatas();

	/**
	 * @param timeout
	 * 
	 * @throws ManagementException
	 * @throws InterruptedException
	 */
	public void enableNetworkManagement(long timeout) throws ManagementException,
			InterruptedException;

	/**
	 * 
	 * @throws ManagementException
	 */
	public void disableNetworkManagement() throws ManagementException;

}
