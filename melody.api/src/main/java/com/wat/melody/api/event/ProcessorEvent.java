package com.wat.melody.api.event;

import com.wat.melody.api.IProcessorManager;

/**
 * <p>
 * </p>
 * 
 * @author Guillaume Cornet
 */
public abstract class ProcessorEvent extends AbstractEvent {

	private IProcessorManager msProcessorManager;

	public ProcessorEvent(IProcessorManager engine) {
		super();
		setProcessorManager(engine);
	}

	public IProcessorManager getProcessorManager() {
		return msProcessorManager;
	}

	private IProcessorManager setProcessorManager(IProcessorManager e) {
		if (e == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ IProcessorManager.class.getCanonicalName() + ".");
		}
		return msProcessorManager = e;
	}

}