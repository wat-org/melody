package com.wat.melody.plugin.ssh.common.exception;

/**
 * <p>
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public class IllegalCompressionTypeException extends SshException {

	private static final long serialVersionUID = -5432298782665529965L;

	public IllegalCompressionTypeException() {
		super();
	}

	public IllegalCompressionTypeException(String msg) {
		super(msg);
	}

	public IllegalCompressionTypeException(Throwable cause) {
		super(cause);
	}

	public IllegalCompressionTypeException(String msg, Throwable cause) {
		super(msg, cause);
	}

}