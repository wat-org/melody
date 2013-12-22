package com.wat.melody.common.transfer.resources.attributes;

import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFileAttributeView;

import com.wat.melody.common.ex.MelodyException;
import com.wat.melody.common.files.PosixGroup;
import com.wat.melody.common.files.exception.IllegalPosixGroupException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class AttributePosixGroup extends AttributeBase<GroupPrincipal> {

	public static final String NAME = "posix:group";

	private PosixGroup _posixGroup = null;

	public AttributePosixGroup() {
		super();
	}

	@Override
	public String name() {
		return NAME;
	}

	@Override
	public String setStringValue(final String value) throws MelodyException,
			IllegalPosixGroupException {
		String previous = super.setStringValue(value);
		// validate and convert input string to typed data
		_posixGroup = PosixGroup.fromString(value);
		return previous;
	}

	/**
	 * <p>
	 * Make this object compatible with
	 * {@link PosixFileAttributeView#setGroup(GroupPrincipal)}.
	 * </p>
	 */
	@Override
	public GroupPrincipal value() {
		return _posixGroup.toGroupPrincipal();
	}

	/**
	 * <p>
	 * Access to the inner {@link PosixGroup} data may be optimized is many
	 * circumstances.
	 * </p>
	 */
	public PosixGroup getPosixGroup() {
		return _posixGroup;
	}

}