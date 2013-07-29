package com.wat.melody.common.transfer.resources.attributes;

import java.nio.file.attribute.PosixFilePermission;
import java.util.EnumSet;
import java.util.Set;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class PosixPermissions extends
		ResourceAttribute<Set<PosixFilePermission>> {

	public static final String VIEW_NAME = "posix:permissions";

	private Set<PosixFilePermission> _permissions = null;

	public PosixPermissions() {
	}

	@Override
	public String name() {
		return VIEW_NAME;
	}

	@Override
	public String setStringValue(String value) {
		String previous = super.setStringValue(value);
		/*
		 * TODO : validate input string
		 */
		// TODO : convert input String to Set<PosixFilePermission>
		_permissions = EnumSet
				.<PosixFilePermission> of(PosixFilePermission.OWNER_READ,
						PosixFilePermission.OWNER_WRITE,
						PosixFilePermission.OWNER_EXECUTE,
						PosixFilePermission.GROUP_READ,
						PosixFilePermission.GROUP_WRITE);
		return previous;
	}

	@Override
	public Set<PosixFilePermission> value() {
		return _permissions;
	}
}