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
	 * <p>
	 * Should call
	 * {@link ITaskContext#reportActivity(com.wat.melody.api.report.ITaskReport)}
	 * in order to report what was done. Get the current context with
	 * {@link Melody#getContext()}.
	 * </p>
	 * <p>
	 * Should call {@link ITaskContext#handleProcessorStateUpdates()} in order
	 * to detect the state of the processing. Get the current context with
	 * {@link Melody#getContext()}.
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