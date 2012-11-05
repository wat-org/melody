package com.wat.melody.core.nativeplugin.order.exception;

import com.wat.melody.api.exception.TaskException;

/**
 * <p>
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public class OrderException extends TaskException {

	private static final long serialVersionUID = -5014916546516159138L;

	public OrderException() {
		super();
	}

	public OrderException(String msg) {
		super(msg);
	}

	public OrderException(Throwable cause) {
		super(cause);
	}

	public OrderException(String msg, Throwable cause) {
		super(msg, cause);
	}

}