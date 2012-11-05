package com.wat.melody.plugin.ssh.common.exception;

/**
 * <p>
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public class KeyPairRepositoryException extends SshException {

	private static final long serialVersionUID = -2133590963141244905L;

	public KeyPairRepositoryException() {
		super();
	}

	public KeyPairRepositoryException(String msg) {
		super(msg);
	}

	public KeyPairRepositoryException(Throwable cause) {
		super(cause);
	}

	public KeyPairRepositoryException(String msg, Throwable cause) {
		super(msg, cause);
	}

}