package com.wat.melody.api;

import org.w3c.dom.Element;

import com.wat.melody.api.exception.TaskFactoryException;
import com.wat.melody.common.properties.PropertySet;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public interface ITaskBuilder {

	public Class<? extends ITask> getTaskClass();

	public String getTaskName();

	public boolean isEligible(Element elmt, PropertySet ps);

	public ITask build() throws TaskFactoryException;

}