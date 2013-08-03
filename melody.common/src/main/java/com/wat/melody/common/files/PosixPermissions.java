package com.wat.melody.common.files;

import static java.nio.file.attribute.PosixFilePermission.GROUP_EXECUTE;
import static java.nio.file.attribute.PosixFilePermission.GROUP_READ;
import static java.nio.file.attribute.PosixFilePermission.GROUP_WRITE;
import static java.nio.file.attribute.PosixFilePermission.OTHERS_EXECUTE;
import static java.nio.file.attribute.PosixFilePermission.OTHERS_READ;
import static java.nio.file.attribute.PosixFilePermission.OTHERS_WRITE;
import static java.nio.file.attribute.PosixFilePermission.OWNER_EXECUTE;
import static java.nio.file.attribute.PosixFilePermission.OWNER_READ;
import static java.nio.file.attribute.PosixFilePermission.OWNER_WRITE;

import java.nio.file.attribute.PosixFilePermission;
import java.util.EnumSet;
import java.util.Set;

import com.wat.melody.common.files.exception.IllegalPosixPermissionsException;
import com.wat.melody.common.messages.Msg;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class PosixPermissions {

	public static final String NUMERIC_PATTERN = "[0-7]{3,4}";
	public static final String LITERAL_PATTERN = "[r-][w-][Ssx-][r-][w-][Ssx-][r-][w-][Ttx-]";
	public static final String PATTERN = NUMERIC_PATTERN + "|"
			+ LITERAL_PATTERN;

	private static final int SUID = 0x0800; // set user ID on execution
	private static final int SGID = 0x0400; // set group ID on execution
	private static final int SVTX = 0x0200; // sticky bit

	private static final int RUSR = 0x0100; // read by owner
	private static final int WUSR = 0x0080; // write by owner
	private static final int XUSR = 0x0040; // execute/search by owner

	private static final int RGRP = 0x0020; // read by group
	private static final int WGRP = 0x0010; // write by group
	private static final int XGRP = 0x0008; // execute/search by group

	private static final int ROTH = 0x0004; // read by others
	private static final int WOTH = 0x0002; // write by others
	private static final int XOTH = 0x0001; // execute/search by others

	/**
	 * <p>
	 * Convert the given <tt>String</tt> to a {@link PosixPermissions} object.
	 * </p>
	 * 
	 * <p>
	 * Input <tt>String</tt> must respect the following pattern :
	 * <ul>
	 * <li>numeric : <tt>[0-7]{3,4}</tt> ;</li>
	 * <li>literal : <tt>[r-][w-][Ssx-][r-][w-][Ssx-][r-][w-][Ttx-]</tt> ;</li>
	 * </ul>
	 * </p>
	 * 
	 * @param permissions
	 *            is the given <tt>String</tt> to convert.
	 * 
	 * @return a {@link PosixPermissions} object, which is equal to the given
	 *         <tt>String</tt>.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given <tt>String</tt> is <tt>null</tt>.
	 * @throws IllegalPosixPermissionsException
	 *             <ul>
	 *             <li>if the given <tt>String</tt> is empty ;</li>
	 *             <li>if the given <tt>String</tt> is doesn't respect the
	 *             pattern {@link #PATTERN} ;</li>
	 *             </ul>
	 */
	public static PosixPermissions fromString(String permissions)
			throws IllegalPosixPermissionsException {
		return new PosixPermissions(permissions);
	}

	/**
	 * <p>
	 * Convert the given <tt>Integer</tt> to a {@link PosixPermissions} object.
	 * </p>
	 * 
	 * <p>
	 * Input <tt>Integer</tt> must respect the following pattern :
	 * <tt>[0-7]{3,4}</tt>
	 * </p>
	 * 
	 * @param permissions
	 *            is the given <tt>Integer</tt> to convert.
	 * 
	 * @return a {@link PosixPermissions} object, which is equal to the given
	 *         <tt>Integer</tt>.
	 * 
	 * @throws IllegalPosixPermissionsException
	 *             if the given <tt>Integer</tt> is doesn't respect the pattern
	 *             {@link #NUMERIC_PATTERN}.
	 */
	public static PosixPermissions fromInt(int permissions)
			throws IllegalPosixPermissionsException {
		return new PosixPermissions(String.valueOf(permissions));
	}

	/**
	 * <p>
	 * Convert the given <tt>Integer</tt> to a {@link PosixPermissions} object.
	 * </p>
	 * 
	 * <p>
	 * In their native raw format, posix permissions is an int32 where :
	 * 
	 * <pre>
	 *    0000 0000 0000 0000
	 * =>      SSTr wxrw xrwx
	 * </pre>
	 * 
	 * </p>
	 * 
	 * @param permissions
	 *            is the given <tt>Integer</tt> to convert.
	 * 
	 * @return a {@link PosixPermissions} object, which is equal to the given
	 *         <tt>Integer</tt>.
	 * 
	 * @throws IllegalPosixPermissionsException
	 *             if the given <tt>Integer</tt> is negative or > 0xFFF.
	 */
	public static PosixPermissions fromRawInt(int permissions)
			throws IllegalPosixPermissionsException {
		return new PosixPermissions(permissions);
	}

	/**
	 * <p>
	 * Convert the given {@link Set} of {@link PosixFilePermission} to a
	 * {@link PosixPermissions} object.
	 * </p>
	 * 
	 * <p>
	 * {@link PosixFilePermission} doesn't support SUID, SGID, and SVTX Bits.
	 * Using {@link Set} of {@link PosixFilePermission} may result in data loss.
	 * </p>
	 * 
	 * @param permissions
	 *            is the given {@link Set} of {@link PosixFilePermission} to
	 *            convert.
	 * 
	 * @return a {@link PosixPermissions} object, which is equal to the given
	 *         {@link Set} of {@link PosixFilePermission}.
	 */
	public static PosixPermissions fromPosixFilePermissionSet(
			Set<PosixFilePermission> permissions) {
		return new PosixPermissions(permissions);
	}

	private int _rawValue;

	public PosixPermissions(String permissions)
			throws IllegalPosixPermissionsException {
		setValue(permissions);
	}

	public PosixPermissions(int permissions)
			throws IllegalPosixPermissionsException {
		setValue(permissions);
	}

	public PosixPermissions(Set<PosixFilePermission> permissions) {
		setValue(permissions);
	}

	@Override
	public int hashCode() {
		return _rawValue;
	}

	@Override
	public String toString() {
		return toLiteralString();
	}

	@Override
	public boolean equals(Object anObject) {
		if (this == anObject) {
			return true;
		}
		if (anObject instanceof PosixPermissions) {
			PosixPermissions inter = (PosixPermissions) anObject;
			return toInt() == inter.toInt();
		}
		return false;
	}

	public int toInt() {
		return _rawValue;
	}

	public String toNumericString() {
		StringBuilder buf = new StringBuilder("");

		int foo = _rawValue;
		for (int i = 0; i < 4; i++) {
			buf.insert(0, (char) ((foo & 0x0007) + '0'));
			foo >>= 3;
		}

		return buf.toString();
	}

	public String toLiteralString() {
		StringBuilder buf = new StringBuilder("---------");

		if ((_rawValue & RUSR) != 0) {
			buf.replace(0, 1, "r");
		}
		if ((_rawValue & WUSR) != 0) {
			buf.replace(1, 2, "w");
		}
		if ((_rawValue & XUSR) != 0) {
			if ((_rawValue & SUID) != 0) {
				buf.replace(2, 3, "s");
			} else {
				buf.replace(2, 3, "x");
			}
		} else if ((_rawValue & SUID) != 0) {
			buf.replace(2, 3, "S");
		}

		if ((_rawValue & RGRP) != 0) {
			buf.replace(3, 4, "r");
		}
		if ((_rawValue & WGRP) != 0) {
			buf.replace(4, 5, "w");
		}
		if ((_rawValue & XGRP) != 0) {
			if ((_rawValue & SGID) != 0) {
				buf.replace(5, 6, "s");
			} else {
				buf.replace(5, 6, "x");
			}
		} else if ((_rawValue & SGID) != 0) {
			buf.replace(5, 6, "S");
		}

		if ((_rawValue & ROTH) != 0) {
			buf.replace(6, 7, "r");
		}
		if ((_rawValue & WOTH) != 0) {
			buf.replace(7, 8, "w");
		}
		if ((_rawValue & XOTH) != 0) {
			if ((_rawValue & SVTX) != 0) {
				buf.replace(8, 9, "t");
			} else {
				buf.replace(8, 9, "x");
			}
		} else if ((_rawValue & SVTX) != 0) {
			buf.replace(8, 9, "T");
		}

		return buf.toString();
	}

	public Set<PosixFilePermission> toPosixFilePermissionSet() {
		// PosixFilePermission doesn't support S_ISUID, S_ISGID nor S_ISVTX
		Set<PosixFilePermission> res = EnumSet
				.noneOf(PosixFilePermission.class);

		if ((_rawValue & RUSR) != 0) {
			res.add(OWNER_READ);
		}
		if ((_rawValue & WUSR) != 0) {
			res.add(OWNER_WRITE);
		}
		if ((_rawValue & XUSR) != 0) {
			res.add(OWNER_EXECUTE);
		}
		if ((_rawValue & RGRP) != 0) {
			res.add(GROUP_READ);
		}
		if ((_rawValue & WGRP) != 0) {
			res.add(GROUP_WRITE);
		}
		if ((_rawValue & XGRP) != 0) {
			res.add(GROUP_EXECUTE);
		}
		if ((_rawValue & ROTH) != 0) {
			res.add(OTHERS_READ);
		}
		if ((_rawValue & WOTH) != 0) {
			res.add(OTHERS_WRITE);
		}
		if ((_rawValue & XOTH) != 0) {
			res.add(OTHERS_EXECUTE);
		}

		return res;
	}

	private int setValue(int permissions)
			throws IllegalPosixPermissionsException {
		if (permissions < 0 && permissions > 0xFFF) {
			throw new IllegalPosixPermissionsException(Msg.bind(
					Messages.PosixPermissionsEx_INVALID, permissions,
					NUMERIC_PATTERN));
		}
		int previous = toInt();
		_rawValue = permissions;
		return previous;
	}

	private int setValue(String permissions)
			throws IllegalPosixPermissionsException {
		if (permissions == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a "
					+ PosixPermissions.class.getCanonicalName() + ").");
		}
		if (permissions.trim().length() == 0) {
			throw new IllegalPosixPermissionsException(Msg.bind(
					Messages.PosixPermissionsEx_EMPTY, permissions));
		} else if (permissions.matches("^" + NUMERIC_PATTERN + "$")) {
			// convert to raw int
			int foo = 0;
			for (byte k : permissions.getBytes()) {
				foo <<= 3;
				foo |= (k - '0');
			}
			try {
				return setValue(foo);
			} catch (IllegalPosixPermissionsException Ex) {
				throw new RuntimeException("Shouldn't get here.");
			}
		} else if (permissions.matches("^" + LITERAL_PATTERN + "$")) {
			// convert to raw int
			int value = 0;
			if (permissions.charAt(0) == 'r') {
				value |= RUSR;
			}
			if (permissions.charAt(1) == 'w') {
				value |= WUSR;
			}
			if (permissions.charAt(2) == 'x') {
				value |= XUSR;
			}
			if (permissions.charAt(2) == 's') {
				value |= XUSR | SUID;
			}
			if (permissions.charAt(2) == 'S') {
				value |= SUID;
			}

			if (permissions.charAt(3) == 'r') {
				value |= RGRP;
			}
			if (permissions.charAt(4) == 'w') {
				value |= WGRP;
			}
			if (permissions.charAt(5) == 'x') {
				value |= XGRP;
			}
			if (permissions.charAt(5) == 's') {
				value |= XGRP | SGID;
			}
			if (permissions.charAt(5) == 'S') {
				value |= SGID;
			}

			if (permissions.charAt(6) == 'r') {
				value |= ROTH;
			}
			if (permissions.charAt(7) == 'w') {
				value |= WOTH;
			}
			if (permissions.charAt(8) == 'x') {
				value |= XOTH;
			}
			if (permissions.charAt(8) == 't') {
				value |= XOTH | SVTX;
			}
			if (permissions.charAt(8) == 'T') {
				value |= SVTX;
			}
			try {
				return setValue(value);
			} catch (IllegalPosixPermissionsException Ex) {
				throw new RuntimeException("Shouldn't get here.");
			}
		}
		throw new IllegalPosixPermissionsException(Msg.bind(
				Messages.PosixPermissionsEx_INVALID, permissions, PATTERN));
	}

	private int setValue(Set<PosixFilePermission> permissions) {
		if (permissions == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Set.class.getCanonicalName() + "<"
					+ PosixFilePermission.class.getCanonicalName() + ">" + ".");
		}
		// PosixFilePermission doesn't support S_ISUID, S_ISGID nor S_ISVTX
		int value = 0;
		if (permissions.contains(OWNER_READ)) {
			value |= RUSR;
		}
		if (permissions.contains(OWNER_WRITE)) {
			value |= WUSR;
		}
		if (permissions.contains(OWNER_EXECUTE)) {
			value |= XUSR;
		}

		if (permissions.contains(GROUP_READ)) {
			value |= RGRP;
		}
		if (permissions.contains(GROUP_WRITE)) {
			value |= WGRP;
		}
		if (permissions.contains(GROUP_EXECUTE)) {
			value |= XGRP;
		}

		if (permissions.contains(OTHERS_READ)) {
			value |= ROTH;
		}
		if (permissions.contains(OTHERS_WRITE)) {
			value |= WOTH;
		}
		if (permissions.contains(OTHERS_EXECUTE)) {
			value |= XOTH;
		}
		try {
			return setValue(value);
		} catch (IllegalPosixPermissionsException Ex) {
			throw new RuntimeException("Shouldn't get here.");
		}
	}

}