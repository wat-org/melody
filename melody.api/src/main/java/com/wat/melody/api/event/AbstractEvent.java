package com.wat.melody.api.event;

import java.util.Date;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class AbstractEvent {

	private Date _date;

	protected AbstractEvent() {
		// Initialize members
		initDate();
	}

	private void initDate() {
		_date = new Date();
	}

	public Date getDate() {
		return _date;
	}

}