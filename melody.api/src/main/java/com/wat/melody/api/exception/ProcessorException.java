package com.wat.melody.api.exception;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class ProcessorException extends MelodyException {

	private static final long serialVersionUID = -1489516211069804151L;

	private String _sourceFile;

	public ProcessorException(String file, String msg) {
		super(msg);
		setSourceFile(file);
	}

	public ProcessorException(String file, Throwable cause) {
		super(cause.getMessage(), cause != null ? cause.getCause() : null);
		setSourceFile(file);
	}

	public ProcessorException(String file, String msg, Throwable cause) {
		super(msg, cause);
		setSourceFile(file);
	}

	private String getSourceFile() {
		return _sourceFile;
	}

	private void setSourceFile(String file) {
		if (file == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ " (the sequence descriptor file path).");
		}
		_sourceFile = file;
	}

	@Override
	public String getMessage() {
		return "[file:" + getSourceFile() + "] " + super.getMessage();
	}

}