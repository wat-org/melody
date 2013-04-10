package com.wat.melody.api;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class Melody {

	/**
	 * <p>
	 * Get the {@link ITaskContainer} associated to the current Thread.
	 * </p>
	 * 
	 * @return the {@link ITaskContainer} associated the current Thread.
	 */
	public static ITaskContext getContext() {
		if (Thread.currentThread() instanceof MelodyThread) {
			return ((MelodyThread) Thread.currentThread()).getContext();
		}
		return null;
	}

}
