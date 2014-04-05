package com.wat.cloud.libvirt.exception;

import com.wat.melody.cloud.protectedarea.ProtectedAreaId;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class ProtectedAreaNotFoundException extends RuntimeException {

	private static final long serialVersionUID = -8767325345365744644L;

	public ProtectedAreaNotFoundException(ProtectedAreaId id) {
		super(id.getValue());
	}

}