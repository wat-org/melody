package com.wat.melody.common.ssh.exception;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class KnownHostsRepositoryPathException extends MelodyException {

	private static final long serialVersionUID = -6546544324324455305L;

	public KnownHostsRepositoryPathException(String msg) {
		super(msg);
	}

	public KnownHostsRepositoryPathException(Throwable cause) {
		super(cause);
	}

	public KnownHostsRepositoryPathException(String msg, Throwable cause) {
		super(msg, cause);
	}

}