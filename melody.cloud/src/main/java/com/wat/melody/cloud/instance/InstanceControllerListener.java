package com.wat.melody.cloud.instance;

import com.wat.melody.cloud.instance.exception.OperationException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public interface InstanceControllerListener {

	public void onInstanceCreated() throws OperationException,
			InterruptedException;

	public void onInstanceDestroyed() throws OperationException,
			InterruptedException;

	public void onInstanceStopped() throws OperationException,
			InterruptedException;

	public void onInstanceStarted() throws OperationException,
			InterruptedException;

}
