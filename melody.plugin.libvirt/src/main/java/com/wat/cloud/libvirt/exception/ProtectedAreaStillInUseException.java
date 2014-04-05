package com.wat.cloud.libvirt.exception;

import com.wat.melody.cloud.protectedarea.ProtectedAreaId;
import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class ProtectedAreaStillInUseException extends MelodyException {

	private static final long serialVersionUID = -8767325345365744644L;

	public ProtectedAreaStillInUseException(ProtectedAreaId id) {
		super(id.getValue());
	}

}