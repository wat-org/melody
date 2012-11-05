package com.wat.melody.plugin.sleep;

import com.wat.melody.api.ITask;
import com.wat.melody.api.ITaskContext;
import com.wat.melody.api.annotation.Attribute;
import com.wat.melody.plugin.sleep.exception.SleepException;

/**
 * <p>
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public class Sleep implements ITask {

	/**
	 * The 'sleep' XML element used in the Sequence Descriptor
	 */
	public static final String SLEEP = "sleep";

	/**
	 * The 'millis' XML attribute of the 'sleep' XML element
	 */
	public static final String MILLIS_ATTR = "millis";

	private ITaskContext moContext;
	private long miMillis;

	public Sleep() {
		initContext();
		initMillis();
	}

	private void initContext() {
		moContext = null;
	}

	private void initMillis() {
		miMillis = 1000;
	}

	@Override
	public void validate() {
	}

	/**
	 * <p>
	 * Sleep during the specified amount of time.
	 * </p>
	 * 
	 * @throws InterruptedException
	 */
	@Override
	public void doProcessing() throws InterruptedException {
		getContext().handleProcessorStateUpdates();
		Thread.sleep(getMillis());
	}

	@Override
	public ITaskContext getContext() {
		return moContext;
	}

	@Override
	public void setContext(ITaskContext p) {
		if (p == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid ITaskContext.");
		}
		moContext = p;
	}

	private long getMillis() {
		return miMillis;
	}

	@Attribute(name = MILLIS_ATTR)
	public long setMillis(long v) throws SleepException {
		if (v <= 0) {
			throw new SleepException(Messages.bind(
					Messages.SleepEx_INVALID_MILLIS_ATTR, v));
		}
		return miMillis = v;
	}

}