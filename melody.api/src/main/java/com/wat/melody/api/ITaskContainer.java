package com.wat.melody.api;

import org.w3c.dom.Node;

import com.wat.melody.api.exception.TaskException;

/**
 * <p>
 * An {@link ITask} which implements {@link ITaskContainer} can contains
 * {@link ITask}.
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public interface ITaskContainer {

	/**
	 * <p>
	 * Place holder where this object's sub-tasks are registered in their native
	 * {@link Node} format.
	 * </p>
	 * 
	 * @param n
	 *            is a Task (in its native {@link Node} format) to add to this
	 *            object.
	 * 
	 * @throws TaskException
	 *             if an error occurred while add the {@link Node}.
	 */
	public void addNode(Node n) throws TaskException;

}