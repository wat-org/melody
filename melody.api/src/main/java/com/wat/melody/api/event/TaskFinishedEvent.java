package com.wat.melody.api.event;

import com.wat.melody.api.ITask;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class TaskFinishedEvent extends TaskEvent {

	private State _state;
	private Throwable _details;

	public TaskFinishedEvent(ITask task, State state) {
		this(task, state, null);
	}

	public TaskFinishedEvent(ITask task, State state, Throwable details) {
		super(task);
		setState(state);
		setDetails(details);
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