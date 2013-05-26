package com.wat.melody.common.ssh.exception;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class KnownHostsException extends MelodyException {

	private static final long serialVersionUID = -2998553237876325454L;

	public KnownHostsException(String msg) {
		super(msg);
	}

	public KnownHostsException(Throwable cause) {
		super(cause);
	}

	public KnownHostsException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
