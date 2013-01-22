package com.wat.melody.common.ssh.exception;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class KnownHostsFileException extends MelodyException {

	private static final long serialVersionUID = -2998553237876325454L;

	public KnownHostsFileException() {
		super();
	}

	public KnownHostsFileException(String msg) {
		super(msg);
	}

	public KnownHostsFileException(Throwable cause) {
		super(cause);
	}

	public KnownHostsFileException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
