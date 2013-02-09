package com.wat.melody.plugin.sleep;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.wat.melody.api.ITask;
import com.wat.melody.api.ITaskContext;
import com.wat.melody.api.annotation.Attribute;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class Sleep implements ITask {

	private static Log log = LogFactory.getLog(Sleep.class);

	/**
	 * The 'sleep' XML element used in the Sequence Descriptor
	 */
	public static final String SLEEP = "sleep";

	/**
	 * The 'millis' XML attribute of the 'sleep' XML element
	 */
	public static final String MILLIS_ATTR = "millis";

	private ITaskContext moContext;
	private SleepTimeout moTimeout;

	public Sleep() {
		initContext();
		setTimeout(SleepTimeout.DEFAULT_VALUE);
	}

	private void initContext() {
		moContext = null;
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
		log.debug(Messages.bind(Messages.SleepMsg_INFO, getTimeout()
				.getTimeoutInMillis()));
		Thread.sleep(getTimeout().getTimeoutInMillis());
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

	private SleepTimeout getTimeout() {
		return moTimeout;
	}

	@Attribute(name = MILLIS_ATTR)
	public SleepTimeout setTimeout(SleepTimeout v) {
		if (v == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ SleepTimeout.class.getCanonicalName() + ".");
		}
		return moTimeout = v;
	}

}