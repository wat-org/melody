package com.wat.melody.api;

import com.wat.melody.api.exception.TaskException;

/**
 * <p>
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public interface ITask {

	public void setContext(ITaskContext tc) throws TaskException;

	public ITaskContext getContext();

	public void validate() throws TaskException;

	/**
	 * 
	 * @throws TaskException
	 *             if an error occurred during the Task Processing. Can be a
	 *             sub-class of {@link TaskException}.
	 * @throws InterruptedException
	 *             if the Task was interrupted during its processing.
	 * @throws Throwable
	 *             if an unmanaged error occurred during the Task Processing.
	 *             Can be a sub-class of {@link Throwable}.
	 */
	public void doProcessing() throws TaskException, InterruptedException;

}