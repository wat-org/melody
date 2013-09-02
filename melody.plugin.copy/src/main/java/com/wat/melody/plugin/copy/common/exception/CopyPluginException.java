package com.wat.melody.plugin.copy.common.exception;

import com.wat.melody.api.exception.TaskException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class CopyPluginException extends TaskException {

	private static final long serialVersionUID = -2867645543245368758L;

	public CopyPluginException(String msg) {
		super(msg);
	}

	public CopyPluginException(Throwable cause) {
		super(cause);
	}

	public CopyPluginException(String msg, Throwable cause) {
		super(msg, cause);
	}

}