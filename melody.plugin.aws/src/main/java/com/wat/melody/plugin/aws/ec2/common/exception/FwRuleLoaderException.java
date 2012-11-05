package com.wat.melody.plugin.aws.ec2.common.exception;

public class FwRuleLoaderException extends AwsException {

	private static final long serialVersionUID = 7682456925787679884L;

	public FwRuleLoaderException() {
		super();
	}

	public FwRuleLoaderException(String msg) {
		super(msg);
	}

	public FwRuleLoaderException(Throwable cause) {
		super(cause);
	}

	public FwRuleLoaderException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
