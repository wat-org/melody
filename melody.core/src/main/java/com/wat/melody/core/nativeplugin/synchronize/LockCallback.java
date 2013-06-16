package com.wat.melody.core.nativeplugin.synchronize;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public interface LockCallback {

	public void doRun() throws MelodyException, InterruptedException;

}