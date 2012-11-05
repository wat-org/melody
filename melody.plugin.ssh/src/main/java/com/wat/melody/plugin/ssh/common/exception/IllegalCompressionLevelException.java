package com.wat.melody.plugin.ssh.common.exception;

/**
 * <p>
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public class IllegalCompressionLevelException extends SshException {

	private static final long serialVersionUID = -5322698659365904245L;

	public IllegalCompressionLevelException() {
		super();
	}

	public IllegalCompressionLevelException(String msg) {
		super(msg);
	}

	public IllegalCompressionLevelException(Throwable cause) {
		super(cause);
	}

	public IllegalCompressionLevelException(String msg, Throwable cause) {
		super(msg, cause);
	}

}