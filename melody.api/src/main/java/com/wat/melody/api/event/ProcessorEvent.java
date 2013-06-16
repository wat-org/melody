package com.wat.melody.api.event;

import com.wat.melody.api.IProcessorManager;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class ProcessorEvent extends AbstractEvent {

	private IProcessorManager _processorManager;

	public ProcessorEvent(IProcessorManager engine) {
		super();
		setProcessorManager(engine);
	}

	public IProcessorManager getProcessorManager() {
		return _processorManager;
	}

	private IProcessorManager setProcessorManager(IProcessorManager e) {
		if (e == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ IProcessorManager.class.getCanonicalName() + ".");
		}
		return _processorManager = e;
	}

}