package com.wat.cloud.libvirt.exception;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class LibVirtKeyPairRepositoryException extends MelodyException {

	private static final long serialVersionUID = -2453656458767974644L;

	public LibVirtKeyPairRepositoryException(String msg) {
		super(msg);
	}

	public LibVirtKeyPairRepositoryException(Throwable cause) {
		super(cause);
	}

	public LibVirtKeyPairRepositoryException(String msg, Throwable cause) {
		super(msg, cause);
	}

}