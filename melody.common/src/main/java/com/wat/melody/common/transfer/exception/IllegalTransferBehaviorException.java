package com.wat.melody.common.transfer.exception;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class IllegalTransferBehaviorException extends MelodyException {

	private static final long serialVersionUID = -3243378809809809735L;

	public IllegalTransferBehaviorException(String msg) {
		super(msg);
	}

	public IllegalTransferBehaviorException(Throwable cause) {
		super(cause);
	}

	public IllegalTransferBehaviorException(String msg, Throwable cause) {
		super(msg, cause);
	}

}