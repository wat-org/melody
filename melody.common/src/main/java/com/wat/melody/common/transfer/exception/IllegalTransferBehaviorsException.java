package com.wat.melody.common.transfer.exception;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class IllegalTransferBehaviorsException extends MelodyException {

	private static final long serialVersionUID = -3365586967654354535L;

	public IllegalTransferBehaviorsException(String msg) {
		super(msg);
	}

	public IllegalTransferBehaviorsException(Throwable cause) {
		super(cause);
	}

	public IllegalTransferBehaviorsException(String msg, Throwable cause) {
		super(msg, cause);
	}

}