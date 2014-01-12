package com.wat.melody.common.cifs.transfer.exception;

import jcifs.smb.SmbException;

import com.wat.melody.common.ex.MelodyException;

/**
 * <p>
 * {@link WrapperSmbException} have no message, only a cause.
 * </p>
 * <p>
 * {@link WrapperSmbException#getMessage()} prints its cause
 * {@link SmbException}'s CIFS ERROR CODE + message.
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public class WrapperSmbException extends MelodyException {

	private static final long serialVersionUID = 98654256897654261L;

	public WrapperSmbException(SmbException cause) {
		super(cause);
		if (cause == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ SmbException.class.getCanonicalName() + ".");
		}
	}

	@Override
	public SmbException getCause() {
		return (SmbException) super.getCause();
	}

	@Override
	public String getMessage() {
		// getCause() can not return null
		String msg = getCause().getMessage();
		if (msg == null) {
			msg = "";
		}
		return String.format("[CIFS_ERR:Ox%X] %s", getCause().getNtStatus(),
				msg);
	}

}