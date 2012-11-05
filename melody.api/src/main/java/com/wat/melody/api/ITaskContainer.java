package com.wat.melody.api;

import org.w3c.dom.Node;

import com.wat.melody.api.exception.TaskException;

/**
 * <p>
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public interface ITaskContainer {

	public void addNode(Node n) throws TaskException;

}