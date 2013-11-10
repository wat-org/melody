package com.wat.melody.common.cifs.transfer.exception;

import jcifs.smb.SmbException;

import com.wat.melody.common.ex.MelodyException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class WrapperSmbException extends MelodyException {

	private static final long serialVersionUID = 98654256897654261L;

	private SmbException _cause;

	public WrapperSmbException(SmbException cause) {
		super((Throwable) null);
		if (cause == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ SmbException.class.getCanonicalName() + ".");
		}
		_cause = cause;
	}

	@Override
	public String getMessage() {
		String msg = _cause.getMessage();
		if (msg == null) {
			msg = "";
		}
		return String.format("[CIFS_ERR:Ox%X] %s", _cause.getNtStatus(), msg);
	}

}