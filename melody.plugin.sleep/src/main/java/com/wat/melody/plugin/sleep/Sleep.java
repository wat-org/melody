package com.wat.melody.plugin.sleep;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.wat.melody.api.ITask;
import com.wat.melody.api.Melody;
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

	private SleepTimeout moTimeout;

	public Sleep() {
		setTimeout(SleepTimeout.DEFAULT_VALUE);
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
		Melody.getContext().handleProcessorStateUpdates();
		log.debug(Messages.bind(Messages.SleepMsg_INFO, getTimeout()
				.getTimeoutInMillis()));
		Thread.sleep(getTimeout().getTimeoutInMillis());
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