package com.wat.melody.api.event;

import com.wat.melody.api.ITask;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class TaskCreatedEvent extends AbstractEvent {

	private String _taskName;
	private State _state;
	private Throwable _details;

	/**
	 * We can't pass the {@link ITask} object, because when the creation failed,
	 * there is no {link ITask} object !
	 * 
	 * @param taskName
	 * @param state
	 */
	public TaskCreatedEvent(String taskName, State state) {
		this(taskName, state, null);
	}

	public TaskCreatedEvent(String taskName, State state, Throwable details) {
		super();
		setTaskName(taskName);
		setState(state);
		setDetails(details);
	}

	public String getTaskName() {
		return _taskName;
	}

	private String setTaskName(String v) {
		if (v == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ ".");
		}
		return _taskName = v;
	}

	public State getState() {
		return _state;
	}

	private void setState(State s) {
		if (s == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + State.class.getCanonicalName() + ".");
		}
		_state = s;
	}

	public Throwable getDetails() {
		return _details;
	}

	private void setDetails(Throwable s) {
		if (s == null && _state != State.SUCCESS) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Throwable.class.getCanonicalName()
					+ ".");
		}
		_details = s;
	}

}