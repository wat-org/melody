package com.wat.melody.plugin.xml.common.exception;

import com.wat.melody.api.exception.TaskException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class XmlPluginException extends TaskException {

	private static final long serialVersionUID = -8796456353245368758L;

	public XmlPluginException(String msg) {
		super(msg);
	}

	public XmlPluginException(Throwable cause) {
		super(cause);
	}

	public XmlPluginException(String msg, Throwable cause) {
		super(msg, cause);
	}

}