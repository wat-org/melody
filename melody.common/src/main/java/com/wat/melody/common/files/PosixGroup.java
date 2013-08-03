package com.wat.melody.common.files;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.attribute.GroupPrincipal;

import com.wat.melody.common.files.exception.IllegalPosixGroupException;
import com.wat.melody.common.messages.Msg;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class PosixGroup {

	/**
	 * <p>
	 * Convert the given <tt>Integer</tt> to a {@link PosixGroup} object.
	 * </p>
	 * 
	 * @param groupId
	 *            is the given <tt>Integer</tt> to convert.
	 * 
	 * @return a {@link PosixGroup} object, which is equal to the given
	 *         <tt>Integer</tt>.
	 * 
	 * @throws IllegalPosixGroupException
	 *             if the given <tt>Integer</tt> is < 0.
	 */
	public static PosixGroup fromInt(int groupId)
			throws IllegalPosixGroupException {
		return new PosixGroup(groupId);
	}

	/**
	 * <p>
	 * Convert the given <tt>String</tt> to a {@link PosixGroup} object.
	 * </p>
	 * 
	 * @param groupId
	 *            is the given <tt>String</tt> to convert.
	 * 
	 * @return a {@link PosixGroup} object, which is equal to the given
	 *         <tt>String</tt>.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given <tt>String</tt> is <tt>null</tt>.
	 * @throws IllegalPosixGroupException
	 *             <ul>
	 *             <li>if the given <tt>String</tt> is empty ;</li>
	 *             <li>if the given <tt>String</tt> is not parse-able Integer ;</li>
	 *             <li>if the given <tt>String</tt> is an Integer < 0 ;</li>
	 *             </ul>
	 */
	public static PosixGroup fromString(String groupId)
			throws IllegalPosixGroupException {
		return new PosixGroup(groupId);
	}

	/**
	 * <p>
	 * Convert the given {@link GroupPrincipal} to a {@link PosixGroup} object.
	 * </p>
	 * 
	 * @param groupId
	 *            is the given {@link GroupPrincipal} to convert.
	 * 
	 * @return a {@link PosixGroup} object, which is equal to the given
	 *         <tt>String</tt>.
	 * 
	 * @throws IllegalArgumentException
	 *             <ul>
	 *             <li>if the given {@link GroupPrincipal} is <tt>null</tt>.</li>
	 *             <li>if the given {@link GroupPrincipal}'s name is
	 *             <tt>null</tt> ;</li>
	 *             </ul>
	 * @throws IllegalPosixGroupException
	 *             <ul>
	 *             <li>if the given {@link GroupPrincipal}'s name is an empty
	 *             <tt>String</tt> ;</li>
	 *             <li>if the given {@link GroupPrincipal}'s name is not
	 *             parse-able Integer ;</li>
	 *             <li>if the given {@link GroupPrincipal}'s name is an Integer
	 *             < 0 ;</li>
	 *             </ul>
	 */
	public static PosixGroup fromGroupPrincipal(GroupPrincipal groupId)
			throws IllegalPosixGroupException {
		return new PosixGroup(groupId);
	}

	private int _gid;

	public PosixGroup(int groupId) throws IllegalPosixGroupException {
		setValue(groupId);
	}

	public PosixGroup(String groupId) throws IllegalPosixGroupException {
		setValue(groupId);
	}

	public PosixGroup(GroupPrincipal groupId) throws IllegalPosixGroupException {
		setValue(groupId);
	}

	@Override
	public int hashCode() {
		return _gid;
	}

	@Override
	public String toString() {
		return String.valueOf(_gid);
	}

	@Override
	public boolean equals(Object anObject) {
		if (this == anObject) {
			return true;
		}
		if (anObject instanceof PosixGroup) {
			PosixGroup inter = (PosixGroup) anObject;
			return toInt() == inter.toInt();
		}
		return false;
	}

	public int toInt() {
		return _gid;
	}

	/**
	 * @return a {@link GroupPrincipal} equal to this object.
	 */
	public GroupPrincipal toGroupPrincipal() {
		try {
			return FileSystems.getDefault().getUserPrincipalLookupService()
					.lookupPrincipalByGroupName(toString());
		} catch (IOException Ex) {
			// will never throw anything since we pass it an gid
			throw new IllegalArgumentException(Ex);
		}
	}

	private int setValue(int groupId) throws IllegalPosixGroupException {
		int previous = toInt();
		if (groupId < 0) {
			throw new IllegalPosixGroupException(Msg.bind(
					Messages.PoxixGroupEx_INVALID, groupId));
		}
		_gid = groupId;
		return previous;
	}

	private int setValue(String groupId) throws IllegalPosixGroupException {
		if (groupId == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a "
					+ PosixGroup.class.getCanonicalName() + ").");
		}
		if (groupId.trim().length() == 0) {
			throw new IllegalPosixGroupException(Msg.bind(
					Messages.PoxixGroupEx_EMPTY, groupId));
		}
		try {
			return setValue(Integer.parseInt(groupId));
		} catch (NumberFormatException Ex) {
			throw new IllegalPosixGroupException(Msg.bind(
					Messages.PoxixGroupEx_INVALID, groupId));
		}
	}

	private int setValue(GroupPrincipal groupId)
			throws IllegalPosixGroupException {
		if (groupId == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ GroupPrincipal.class.getCanonicalName() + ".");
		}
		return setValue(groupId.getName());
	}

}