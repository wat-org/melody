package com.wat.melody.common.ssh.impl.transfer;

import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.ssh.impl.Messages;
import com.wat.melody.common.ssh.impl.transfer.exception.IllegalPermissionsException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class Permissions {

	public static final String PATTERN = "[0-7]{3}";

	/**
	 * <p>
	 * Convert the given <tt>String</tt> to a {@link Permissions} object.
	 * </p>
	 * 
	 * <p>
	 * Input <tt>String</tt> must respect the following pattern :
	 * <tt>[0-7]{3}</tt>
	 * </p>
	 * 
	 * @param permissions
	 *            is the given <tt>String</tt> to convert.
	 * 
	 * @return a {@link Permissions} object, which is equal to the given
	 *         <tt>String</tt>.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given <tt>String</tt> is <tt>null</tt>.
	 * @throws IllegalPermissionsException
	 *             <ul>
	 *             <li>if the given <tt>String</tt> is empty ;</li>
	 *             <li>if the given <tt>String</tt> is doesn't respect the
	 *             pattern {@link #PATTERN} ;</li>
	 *             </ul>
	 */
	public static Permissions parseString(String permissions)
			throws IllegalPermissionsException {
		return new Permissions(permissions);
	}

	private String _value;

	public Permissions(String permissions) throws IllegalPermissionsException {
		setValue(permissions);
	}

	@Override
	public int hashCode() {
		return _value.hashCode();
	}

	@Override
	public String toString() {
		return _value;
	}

	@Override
	public boolean equals(Object anObject) {
		if (this == anObject) {
			return true;
		}
		if (anObject instanceof Permissions) {
			Permissions inter = (Permissions) anObject;
			return getValue().equals(inter.getValue());
		}
		return false;
	}

	public int toInt() {
		int foo = 0;
		for (byte k : getValue().getBytes()) {
			foo <<= 3;
			foo |= (k - '0');
		}
		return foo;
	}

	public String getValue() {
		return _value;
	}

	private String setValue(String permissions)
			throws IllegalPermissionsException {
		String previous = toString();
		if (permissions == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a "
					+ Permissions.class.getCanonicalName() + ").");
		}
		if (permissions.trim().length() == 0) {
			throw new IllegalPermissionsException(Msg.bind(
					Messages.PermissionsEx_EMPTY, permissions));
		} else if (!permissions.matches("^" + PATTERN + "$")) {
			throw new IllegalPermissionsException(Msg.bind(
					Messages.PermissionsEx_INVALID, permissions, PATTERN));
		}
		_value = permissions;
		return previous;
	}

}