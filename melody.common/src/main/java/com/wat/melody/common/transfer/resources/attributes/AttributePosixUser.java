package com.wat.melody.common.transfer.resources.attributes;

import java.nio.file.attribute.FileOwnerAttributeView;
import java.nio.file.attribute.UserPrincipal;

import com.wat.melody.common.ex.MelodyException;
import com.wat.melody.common.files.PosixUser;
import com.wat.melody.common.files.exception.IllegalPosixUserException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class AttributePosixUser extends AttributeBase<UserPrincipal> {

	public static final String NAME = "owner:owner";

	private PosixUser _posixUser = null;

	public AttributePosixUser() {
	}

	@Override
	public String name() {
		return NAME;
	}

	@Override
	public String setStringValue(final String value) throws MelodyException,
			IllegalPosixUserException {
		String previous = super.setStringValue(value);
		// validate and convert input string to typed data
		_posixUser = PosixUser.fromString(value);
		return previous;
	}

	/**
	 * <p>
	 * Make this object compatible with
	 * {@link FileOwnerAttributeView#setOwner(UserPrincipal)}.
	 * </p>
	 */
	@Override
	public UserPrincipal value() {
		return _posixUser.toUserPrincipal();
	}

	/**
	 * <p>
	 * Access to the inner {@link PosixUser} data may be optimized is many
	 * circumstances.
	 * </p>
	 */
	public PosixUser getPosixUser() {
		return _posixUser;
	}

}