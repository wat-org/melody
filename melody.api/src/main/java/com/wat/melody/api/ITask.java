package com.wat.melody.api;

import com.wat.melody.api.exception.TaskException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public interface ITask {

	/**
	 * <p>
	 * Place holder where this object's validation is done.
	 * </p>
	 * 
	 * @throws TaskException
	 *             if an error is detected by this object's validation.
	 * @throws Throwable
	 *             if an unmanaged error occurred during this object's
	 *             validation.
	 */
	public void validate() throws TaskException;

	/**
	 * <p>
	 * Place holder where this object's processing is done.
	 * </p>
	 * 
	 * @throws TaskException
	 *             if an error occurred during this object's processing.
	 * @throws InterruptedException
	 *             if the Task was interrupted during this object's processing.
	 * @throws Throwable
	 *             if an unmanaged error occurred during this object's
	 *             processing.
	 */
	public void doProcessing() throws TaskException, InterruptedException;

}