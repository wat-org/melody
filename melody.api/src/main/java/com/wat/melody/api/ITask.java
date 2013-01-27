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
	 * Associate the given {@link ITaskContext} to this object.
	 * </p>
	 * 
	 * @param tc
	 *            is the {@link ITaskContext} to associate to this object.
	 * 
	 * @throws TaskException
	 *             if an error occurred during this call.
	 */
	public void setContext(ITaskContext tc) throws TaskException;

	/**
	 * <p>
	 * Get the {@link ITaskContainer} associated to this object.
	 * </p>
	 * 
	 * @return the {@link ITaskContainer} associated to this object.
	 */
	public ITaskContext getContext();

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