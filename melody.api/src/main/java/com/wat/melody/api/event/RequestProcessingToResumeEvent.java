package com.wat.melody.api.event;

import com.wat.melody.api.IProcessorManager;

/**
 * <p>
 * </p>
 * 
 * @author Guillaume Cornet
 */
public class RequestProcessingToResumeEvent extends ProcessorEvent {

	public RequestProcessingToResumeEvent(IProcessorManager engine) {
		super(engine);
	}

}