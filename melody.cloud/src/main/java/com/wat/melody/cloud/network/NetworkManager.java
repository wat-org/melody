package com.wat.melody.cloud.network;

import com.wat.melody.api.ITaskContext;
import com.wat.melody.cloud.network.exception.ManagementException;

public interface NetworkManager {

	public ITaskContext getContext();

	public void setContext(ITaskContext context);

	public NetworkManagerInfos getManagementInfos();

	public void setManagementInfos(NetworkManagerInfos mi);

	/**
	 * @param timeout
	 * 
	 * @throws ManagementException
	 * @throws InterruptedException
	 */
	public void enableManagement(long timeout) throws ManagementException,
			InterruptedException;

	/**
	 * 
	 * @throws ManagementException
	 */
	public void disableManagement() throws ManagementException;

}
