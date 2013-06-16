package com.wat.melody.plugin.sleep;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.wat.melody.api.ITask;
import com.wat.melody.api.Melody;
import com.wat.melody.api.annotation.Attribute;
import com.wat.melody.common.messages.Msg;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class Sleep implements ITask {

	private static Log log = LogFactory.getLog(Sleep.class);

	/**
	 * Task's name
	 */
	public static final String SLEEP = "sleep";

	/**
	 * Task's attribute, which specifies the amount of time, in millis, to
	 * sleep.
	 */
	public static final String MILLIS_ATTR = "millis";

	private SleepTimeout _timeout;

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
		log.debug(Msg.bind(Messages.SleepMsg_INFO, getTimeout()
				.getTimeoutInMillis()));
		Thread.sleep(getTimeout().getTimeoutInMillis());
	}

	private SleepTimeout getTimeout() {
		return _timeout;
	}

	@Attribute(name = MILLIS_ATTR)
	public SleepTimeout setTimeout(SleepTimeout v) {
		if (v == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ SleepTimeout.class.getCanonicalName() + ".");
		}
		return _timeout = v;
	}

}