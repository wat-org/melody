package com.wat.melody.api.event;

import java.util.Date;

/**
 * <p>
 * </p>
 * 
 * @author Guillaume Cornet
 */
public class AbstractEvent {
	private Date moDate;

	protected AbstractEvent() {
		// Initialize members
		initDate();
	}

	private void initDate() {
		moDate = new Date();
	}

	public Date getDate() {
		return moDate;
	}

}