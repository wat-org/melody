package com.wat.melody.api;

import org.w3c.dom.Element;

import com.wat.melody.api.exception.TaskException;

/**
 * <p>
 * An {@link ITask} which implements {@link ITaskContainer} can contains inner-
 * {@link ITask}.
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public interface ITaskContainer {

	/**
	 * <p>
	 * Register the given inner-task, in its native {@link Element} format.
	 * </p>
	 * 
	 * @param n
	 *            is an inner-task (in its native {@link Element} format) to
	 *            register.
	 * 
	 * @throws TaskException
	 *             if an error occurred while registering the {@link Element}.
	 */
	public void registerInnerTask(Element n) throws TaskException;

}