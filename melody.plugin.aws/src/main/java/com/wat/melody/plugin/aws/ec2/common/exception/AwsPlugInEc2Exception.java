package com.wat.melody.plugin.aws.ec2.common.exception;

import com.wat.melody.api.exception.TaskException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class AwsPlugInEc2Exception extends TaskException {

	private static final long serialVersionUID = 7682456925787679884L;

	public AwsPlugInEc2Exception(String msg) {
		super(msg);
	}

	public AwsPlugInEc2Exception(Throwable cause) {
		super(cause);
	}

	public AwsPlugInEc2Exception(String msg, Throwable cause) {
		super(msg, cause);
	}

}