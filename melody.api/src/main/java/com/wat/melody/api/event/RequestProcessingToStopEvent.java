package com.wat.melody.api.event;

import com.wat.melody.api.IProcessorManager;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class RequestProcessingToStopEvent extends ProcessorEvent {

	public RequestProcessingToStopEvent(IProcessorManager engine) {
		super(engine);
	}

}