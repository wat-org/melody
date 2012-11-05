package com.wat.melody.api.event;

import com.wat.melody.api.IProcessorManager;

/**
 * <p>
 * </p>
 * 
 * @author Guillaume Cornet
 */
public class ProcessingFinishedEvent extends ProcessorEvent {

	private State miState;
	private Throwable moDetails;

	public ProcessingFinishedEvent(IProcessorManager engine, State state) {
		this(engine, state, null);
	}

	public ProcessingFinishedEvent(IProcessorManager engine, State state,
			Throwable details) {
		super(engine);
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