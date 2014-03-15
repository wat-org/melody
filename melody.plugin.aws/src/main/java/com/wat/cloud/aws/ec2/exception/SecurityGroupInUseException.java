package com.wat.cloud.aws.ec2.exception;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class SecurityGroupInUseException extends MelodyException {

	private static final long serialVersionUID = 2786464253237689884L;

	public SecurityGroupInUseException(String msg) {
		super(msg);
	}

	public SecurityGroupInUseException(Throwable cause) {
		super(cause);
	}

	public SecurityGroupInUseException(String msg, Throwable cause) {
		super(msg, cause);
	}

}