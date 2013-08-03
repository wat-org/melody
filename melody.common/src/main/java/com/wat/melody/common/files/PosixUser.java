package com.wat.melody.common.files;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.attribute.UserPrincipal;

import com.wat.melody.common.files.exception.IllegalPosixUserException;
import com.wat.melody.common.messages.Msg;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class PosixUser {

	/**
	 * <p>
	 * Convert the given <tt>Integer</tt> to a {@link PosixUser} object.
	 * </p>
	 * 
	 * @param userId
	 *            is the given <tt>Integer</tt> to convert.
	 * 
	 * @return a {@link PosixUser} object, which is equal to the given
	 *         <tt>Integer</tt>.
	 * 
	 * @throws IllegalPosixOwnerException
	 *             if the given <tt>Integer</tt> is < 0.
	 */
	public static PosixUser fromInt(int userId)
			throws IllegalPosixUserException {
		return new PosixUser(userId);
	}

	/**
	 * <p>
	 * Convert the given <tt>String</tt> to a {@link PosixUser} object.
	 * </p>
	 * 
	 * @param userId
	 *            is the given <tt>String</tt> to convert.
	 * 
	 * @return a {@link PosixUser} object, which is equal to the given
	 *         <tt>String</tt>.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given <tt>String</tt> is <tt>null</tt>.
	 * @throws IllegalPosixUserException
	 *             <ul>
	 *             <li>if the given <tt>String</tt> is empty ;</li>
	 *             <li>if the given <tt>String</tt> is not parse-able Integer ;</li>
	 *             <li>if the given <tt>String</tt> is an Integer < 0 ;</li>
	 *             </ul>
	 */
	public static PosixUser fromString(String userId)
			throws IllegalPosixUserException {
		return new PosixUser(userId);
	}

	/**
	 * <p>
	 * Convert the given {@link UserPrincipal} to a {@link PosixUser} object.
	 * </p>
	 * 
	 * @param userId
	 *            is the given {@link UserPrincipal} to convert.
	 * 
	 * @return a {@link PosixUser} object, which is equal to the given
	 *         <tt>String</tt>.
	 * 
	 * @throws IllegalArgumentException
	 *             <ul>
	 *             <li>if the given {@link UserPrincipal} is <tt>null</tt>.</li>
	 *             <li>if the given {@link UserPrincipal}'s name is
	 *             <tt>null</tt> ;</li>
	 *             </ul>
	 * @throws IllegalPosixUserException
	 *             <ul>
	 *             <li>if the given {@link UserPrincipal}'s name is an empty
	 *             <tt>String</tt> ;</li>
	 *             <li>if the given {@link UserPrincipal}'s name is not
	 *             parse-able Integer ;</li>
	 *             <li>if the given {@link UserPrincipal}'s name is an Integer <
	 *             0 ;</li>
	 *             </ul>
	 */
	public static PosixUser fromUserPrincipal(UserPrincipal userId)
			throws IllegalPosixUserException {
		return new PosixUser(userId);
	}

	private int _uid;

	public PosixUser(int userId) throws IllegalPosixUserException {
		setValue(userId);
	}

	public PosixUser(String userId) throws IllegalPosixUserException {
		setValue(userId);
	}

	public PosixUser(UserPrincipal userId) throws IllegalPosixUserException {
		setValue(userId);
	}

	@Override
	public int hashCode() {
		return _uid;
	}

	@Override
	public String toString() {
		return String.valueOf(_uid);
	}

	@Override
	public boolean equals(Object anObject) {
		if (this == anObject) {
			return true;
		}
		if (anObject instanceof PosixUser) {
			PosixUser inter = (PosixUser) anObject;
			return toInt() == inter.toInt();
		}
		return false;
	}

	public int toInt() {
		return _uid;
	}

	/**
	 * @return a {@link UserPrincipal} equal to this object.
	 */
	public UserPrincipal toUserPrincipal() {
		try {
			return FileSystems.getDefault().getUserPrincipalLookupService()
					.lookupPrincipalByName(toString());
		} catch (IOException Ex) {
			// will never throw anything since we pass it an uid
			throw new IllegalArgumentException(Ex);
		}
	}

	private int setValue(int userId) throws IllegalPosixUserException {
		int previous = toInt();
		if (userId < 0) {
			throw new IllegalPosixUserException(Msg.bind(
					Messages.PoxixUserEx_INVALID, userId));
		}
		_uid = userId;
		return previous;
	}

	private int setValue(String userId) throws IllegalPosixUserException {
		if (userId == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a "
					+ PosixUser.class.getCanonicalName() + ").");
		}
		if (userId.trim().length() == 0) {
			throw new IllegalPosixUserException(Msg.bind(
					Messages.PoxixUserEx_EMPTY, userId));
		}
		try {
			return setValue(Integer.parseInt(userId));
		} catch (NumberFormatException Ex) {
			throw new IllegalPosixUserException(Msg.bind(
					Messages.PoxixUserEx_INVALID, userId));
		}
	}

	private int setValue(UserPrincipal userId) throws IllegalPosixUserException {
		if (userId == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ UserPrincipal.class.getCanonicalName() + ".");
		}
		return setValue(userId.getName());
	}

}