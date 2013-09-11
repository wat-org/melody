package com.wat.melody.plugin.aws.common.exception;

import com.wat.melody.api.exception.PlugInConfigurationException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class AwsPlugInConfigurationException extends
		PlugInConfigurationException {

	private static final long serialVersionUID = 983212356657786543L;

	public AwsPlugInConfigurationException(String msg) {
		super(msg);
	}

	public AwsPlugInConfigurationException(Throwable cause) {
		super(cause);
	}

	public AwsPlugInConfigurationException(String msg, Throwable cause) {
		super(msg, cause);
	}

}