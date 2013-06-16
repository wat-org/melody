package com.wat.melody.api.event;

import com.wat.melody.api.IProcessorManager;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class ProcessingFinishedEvent extends ProcessorEvent {

	private State _state;
	private Throwable _details;

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