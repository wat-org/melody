package com.wat.melody.core.nativeplugin.setEDAttrValue.exception;

import com.wat.melody.api.exception.TaskException;

/**
 * <p>
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public class SetEDAttrValueException extends TaskException {

	private static final long serialVersionUID = -189644684016806644L;

	public SetEDAttrValueException() {
		super();
	}

	public SetEDAttrValueException(String msg) {
		super(msg);
	}

	public SetEDAttrValueException(Throwable cause) {
		super(cause);
	}

	public SetEDAttrValueException(String msg, Throwable cause) {
		super(msg, cause);
	}

}