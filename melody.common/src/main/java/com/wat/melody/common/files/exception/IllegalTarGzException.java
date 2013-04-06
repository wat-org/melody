package com.wat.melody.common.files.exception;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class IllegalTarGzException extends IllegalFileException {

	private static final long serialVersionUID = -1986465413587462515L;

	public IllegalTarGzException() {
		super();
	}

	public IllegalTarGzException(String msg) {
		super(msg);
	}

	public IllegalTarGzException(Throwable cause) {
		super(cause);
	}

	public IllegalTarGzException(String msg, Throwable cause) {
		super(msg, cause);
	}

}