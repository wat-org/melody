package com.wat.melody.cloud.instance;

import com.wat.melody.cloud.instance.exception.OperationException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public interface InstanceControllerListener {

	public void onInstanceIdChanged() throws OperationException;

	public void onInvalidateInstanceNetworkDevicesDatas()
			throws OperationException, InterruptedException;

	public void onAssignInstanceNetworkDevicesDatas()
			throws OperationException, InterruptedException;

}
