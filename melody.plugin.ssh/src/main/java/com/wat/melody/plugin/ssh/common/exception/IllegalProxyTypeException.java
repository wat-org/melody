package com.wat.melody.plugin.ssh.common.exception;

/**
 * <p>
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public class IllegalProxyTypeException extends SshException {

	private static final long serialVersionUID = -2133590963741240905L;

	public IllegalProxyTypeException() {
		super();
	}

	public IllegalProxyTypeException(String msg) {
		super(msg);
	}

	public IllegalProxyTypeException(Throwable cause) {
		super(cause);
	}

	public IllegalProxyTypeException(String msg, Throwable cause) {
		super(msg, cause);
	}

}