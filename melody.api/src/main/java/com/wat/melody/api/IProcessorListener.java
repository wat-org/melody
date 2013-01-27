package com.wat.melody.api;

import com.wat.melody.api.event.ProcessingFinishedEvent;
import com.wat.melody.api.event.ProcessingStartedEvent;
import com.wat.melody.api.event.RequestProcessingToPauseEvent;
import com.wat.melody.api.event.RequestProcessingToResumeEvent;
import com.wat.melody.api.event.RequestProcessingToStartEvent;
import com.wat.melody.api.event.RequestProcessingToStopEvent;
import com.wat.melody.api.event.TaskCreatedEvent;
import com.wat.melody.api.event.TaskFinishedEvent;
import com.wat.melody.api.event.TaskStartedEvent;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public interface IProcessorListener {

	public void processingStartRequested(RequestProcessingToStartEvent evt);

	public void processingStopRequested(RequestProcessingToStopEvent evt);

	public void processingPauseRequested(RequestProcessingToPauseEvent evt);

	public void processingResumeRequested(RequestProcessingToResumeEvent evt);

	public void processingStarted(ProcessingStartedEvent evt);

	public void processingFinished(ProcessingFinishedEvent evt);

	public void taskCreated(TaskCreatedEvent evt);

	public void taskStarted(TaskStartedEvent evt);

	public void taskFinished(TaskFinishedEvent evt);

}