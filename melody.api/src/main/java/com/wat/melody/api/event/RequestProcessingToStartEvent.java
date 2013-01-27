package com.wat.melody.api.event;

import com.wat.melody.api.IProcessorManager;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class RequestProcessingToStartEvent extends ProcessorEvent {

	public RequestProcessingToStartEvent(IProcessorManager engine) {
		super(engine);
	}

}