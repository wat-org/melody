package com.wat.melody.plugin.aws.s3.common.exception;

import com.wat.melody.api.exception.TaskException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class AwsPlugInS3Exception extends TaskException {

	private static final long serialVersionUID = 8756353256458979884L;

	public AwsPlugInS3Exception(String msg) {
		super(msg);
	}

	public AwsPlugInS3Exception(Throwable cause) {
		super(cause);
	}

	public AwsPlugInS3Exception(String msg, Throwable cause) {
		super(msg, cause);
	}

}