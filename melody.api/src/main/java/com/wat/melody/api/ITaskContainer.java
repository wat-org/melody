package com.wat.melody.api;

import org.w3c.dom.Node;

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
	 * Place holder where this object's inner-tasks are registered in their
	 * native {@link Node} format.
	 * </p>
	 * 
	 * @param n
	 *            is an inner-task (in its native {@link Node} format) to
	 *            register to this object.
	 * 
	 * @throws TaskException
	 *             if an error occurred while registering the {@link Node}.
	 */
	public void registerInnerTask(Node n) throws TaskException;

}