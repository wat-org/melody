package com.wat.melody.api.event;

import com.wat.melody.api.IProcessorManager;

/**
 * <p>
 * </p>
 * 
 * @author Guillaume Cornet
 */
public class RequestProcessingToPauseEvent extends ProcessorEvent {

	public RequestProcessingToPauseEvent(IProcessorManager engine) {
		super(engine);
	}

}