package com.wat.melody.plugin.xml.common.exception;

import com.wat.melody.api.exception.PlugInConfigurationException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class XmlPlugInConfigurationException extends
		PlugInConfigurationException {

	private static final long serialVersionUID = -321432143534675764L;

	public XmlPlugInConfigurationException(String msg) {
		super(msg);
	}

	public XmlPlugInConfigurationException(Throwable cause) {
		super(cause);
	}

	public XmlPlugInConfigurationException(String msg, Throwable cause) {
		super(msg, cause);
	}

}