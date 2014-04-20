package com.wat.melody.common.properties.exception;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class IllegalPropertiesSetException extends MelodyException {

	private static final long serialVersionUID = 7894651384798200660L;

	private String _file;
	private int _line;

	public IllegalPropertiesSetException(String file, int line, String msg) {
		super(msg);
		_file = file;
		_line = line;
	}

	public IllegalPropertiesSetException(String file, int line, Throwable cause) {
		super(cause);
		_file = file;
		_line = line;
	}

	public IllegalPropertiesSetException(String file, int line, String msg,
			Throwable cause) {
		super(msg, cause);
		_file = file;
		_line = line;
	}

	@Override
	public String getMessage() {
		String msg = super.getMessage();
		return "[file:" + _file + ", line:" + _line + "] "
				+ (msg != null ? msg : "");
	}

}