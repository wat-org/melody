package com.wat.melody.common.ssh.impl.transfer;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class TransferException extends MelodyException {

	private static final long serialVersionUID = -2141234458970708687L;

	public TransferException(String msg) {
		super(msg);
	}

	public TransferException(Throwable cause) {
		super(cause);
	}

	public TransferException(String msg, Throwable cause) {
		super(msg, cause);
	}

}