package com.wat.melody.cloud.protectedarea;

import com.wat.melody.cloud.protectedarea.exception.ProtectedAreaException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public interface ProtectedAreaControllerListener {

	public void onProtectedAreaCreated() throws ProtectedAreaException,
			InterruptedException;

	public void onProtectedAreaDestroyed() throws ProtectedAreaException,
			InterruptedException;

}