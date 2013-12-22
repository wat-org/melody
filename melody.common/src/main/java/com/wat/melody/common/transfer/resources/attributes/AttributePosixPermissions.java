package com.wat.melody.common.transfer.resources.attributes;

import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;

import com.wat.melody.common.ex.MelodyException;
import com.wat.melody.common.files.PosixPermissions;
import com.wat.melody.common.files.exception.IllegalPosixPermissionsException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class AttributePosixPermissions extends
		AttributeBase<Set<PosixFilePermission>> {

	public static final String NAME = "posix:permissions";

	private PosixPermissions _posixPermissions = null;

	public AttributePosixPermissions() {
		super();
	}

	@Override
	public String name() {
		return NAME;
	}

	@Override
	public String setStringValue(String value) throws MelodyException,
			IllegalPosixPermissionsException {
		String previous = super.setStringValue(value);
		// validate and convert input string to typed data
		_posixPermissions = PosixPermissions.fromString(value);
		return previous;
	}

	/**
	 * <p>
	 * Make this object compatible with
	 * {@link PosixFileAttributeView#setPermissions(Set)}.
	 * </p>
	 */
	@Override
	public Set<PosixFilePermission> value() {
		return _posixPermissions.toPosixFilePermissionSet();
	}

	/**
	 * <p>
	 * Access to the inner {@link PosixPermissions} data may be optimized is
	 * many circumstances.
	 * </p>
	 */
	public PosixPermissions getPosixPermissions() {
		return _posixPermissions;
	}

}