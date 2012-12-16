package com.wat.melody.cloud.management;

import com.wat.melody.api.ITaskContext;
import com.wat.melody.cloud.management.exception.ManagementException;

public interface ManagementHelper {

	public ITaskContext getContext();

	public void setContext(ITaskContext context);

	public ManagementInfos getManagementInfos();

	public void setManagementInfos(ManagementInfos mi);

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
