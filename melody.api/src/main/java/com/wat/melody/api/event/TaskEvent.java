package com.wat.melody.api.event;

import com.wat.melody.api.ITask;

/**
 * <p>
 * </p>
 * 
 * @author Guillaume Cornet
 */
public class TaskEvent extends AbstractEvent {

	private ITask msTask;

	public TaskEvent(ITask task) {
		super();
		setTask(task);
	}

	public ITask getTask() {
		return msTask;
	}

	private ITask setTask(ITask t) {
		if (t == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + ITask.class.getCanonicalName() + ".");
		}
		return msTask = t;
	}

}