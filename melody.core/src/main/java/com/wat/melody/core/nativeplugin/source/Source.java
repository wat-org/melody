package com.wat.melody.core.nativeplugin.source;

import com.wat.melody.api.IDeferedTask;
import com.wat.melody.api.ITask;
import com.wat.melody.api.exception.TaskException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class Source implements ITask, IDeferedTask {

	/**
	 * The 'source' XML element used in the Sequence Descriptor
	 */
	public static final String SOURCE = "source";

	@Override
	public void validate() throws TaskException {
		// can be validated
		throw new RuntimeException("this task cannot be validated!");
	}

	@Override
	public void doProcessing() throws TaskException, InterruptedException {
		// can be process
		throw new RuntimeException("this task cannot be processed!");
	}

}