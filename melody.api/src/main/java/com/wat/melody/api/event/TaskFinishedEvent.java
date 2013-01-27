package com.wat.melody.api.event;

import com.wat.melody.api.ITask;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class TaskFinishedEvent extends TaskEvent {

	private State miState;
	private Throwable moDetails;

	public TaskFinishedEvent(ITask task, State state) {
		this(task, state, null);
	}

	public TaskFinishedEvent(ITask task, State state, Throwable details) {
		super(task);
		setState(state);
		setDetails(details);
	}

	public State getState() {
		return miState;
	}

	private void setState(State s) {
		if (s == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + State.class.getCanonicalName() + ".");
		}
		miState = s;
	}

	public Throwable getDetails() {
		return moDetails;
	}

	private void setDetails(Throwable s) {
		if (s == null && miState != State.SUCCESS) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Throwable.class.getCanonicalName()
					+ ".");
		}
		moDetails = s;
	}

}