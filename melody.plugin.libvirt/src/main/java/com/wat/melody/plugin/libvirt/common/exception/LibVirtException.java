package com.wat.melody.plugin.libvirt.common.exception;

import com.wat.melody.api.exception.TaskException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class LibVirtException extends TaskException {

	private static final long serialVersionUID = 2682456725717679884L;

	public LibVirtException() {
		super();
	}

	public LibVirtException(String msg) {
		super(msg);
	}

	public LibVirtException(Throwable cause) {
		super(cause);
	}

	public LibVirtException(String msg, Throwable cause) {
		super(msg, cause);
	}

}