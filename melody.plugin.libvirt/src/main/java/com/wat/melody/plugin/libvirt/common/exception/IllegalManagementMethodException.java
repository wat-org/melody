package com.wat.melody.plugin.libvirt.common.exception;

public class IllegalManagementMethodException extends LibVirtException {

	private static final long serialVersionUID = -497357953090876949L;

	public IllegalManagementMethodException() {
		super();
	}

	public IllegalManagementMethodException(String msg) {
		super(msg);
	}

	public IllegalManagementMethodException(Throwable cause) {
		super(cause);
	}

	public IllegalManagementMethodException(String msg, Throwable cause) {
		super(msg, cause);
	}

}