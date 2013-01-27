package com.wat.melody.api.event;

import java.util.Date;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class AbstractEvent {
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