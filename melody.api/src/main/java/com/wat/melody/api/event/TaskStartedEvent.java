package com.wat.melody.api.event;

import com.wat.melody.api.ITask;

/**
 * <p>
 * </p>
 * 
 * @author Guillaume Cornet
 */
public class TaskStartedEvent extends TaskEvent {

	public TaskStartedEvent(ITask task) {
		super(task);
	}

}