package com.wat.melody.common.ssh.types;

import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.ssh.types.exception.IllegalGroupIDException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class GroupID {

	/**
	 * <p>
	 * Convert the given <code>String</code> to an {@link GroupID} object.
	 * </p>
	 * 
	 * <p>
	 * <i> * Input <code>String</code> must respect the following pattern : a
	 * String which represents a positive Integer or Zero.<BR/>
	 * </i>
	 * </p>
	 * 
	 * @param sGroupID
	 *            is the given <code>String</code> to convert.
	 * 
	 * @return a <code>GroupID</code> object, whose equal to the given input
	 *         <code>String</code>.
	 * 
	 * 
	 * @throws IllegalGroupIDException
	 *             if the given input <code>String</code> is not a valid
	 *             <code>GroupID</code>.
	 * @throws IllegalArgumentException
	 *             if the given input <code>String</code> is <code>null</code>.
	 */
	public static GroupID parseString(String sGroupID)
			throws IllegalGroupIDException {
		return new GroupID(sGroupID);
	}

	private String _value;

	public GroupID(String sGroupID) throws IllegalGroupIDException {
		setValue(sGroupID);
	}

	public int toInt() {
		return Integer.parseInt(getValue());
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
		if (anObject instanceof GroupID) {
			GroupID inter = (GroupID) anObject;
			return getValue().equals(inter.getValue());
		}
		return false;
	}

	public String getValue() {
		return _value;
	}

	public String setValue(String sGroupID) throws IllegalGroupIDException {
		String previous = toString();
		if (sGroupID == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (an "
					+ Modifiers.class.getCanonicalName() + ").");
		}
		if (sGroupID.trim().length() == 0) {
			throw new IllegalGroupIDException(Msg.bind(
					Messages.GroupIDEx_EMPTY, sGroupID));
		}
		int iGID = 0;
		try {
			iGID = Integer.parseInt(sGroupID);
		} catch (NumberFormatException Ex) {
			throw new IllegalGroupIDException(Msg.bind(
					Messages.GroupIDEx_INVALID, sGroupID));
		}
		if (iGID < 0) {
			throw new IllegalGroupIDException(Msg.bind(
					Messages.GroupIDEx_INVALID, sGroupID));
		}
		_value = sGroupID;
		return previous;
	}

}