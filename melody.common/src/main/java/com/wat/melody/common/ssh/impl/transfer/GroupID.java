package com.wat.melody.common.ssh.impl.transfer;

import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.ssh.impl.Messages;
import com.wat.melody.common.ssh.impl.transfer.exception.IllegalGroupIDException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class GroupID {

	/**
	 * <p>
	 * Convert the given <tt>String</tt> to a {@link GroupID} object.
	 * </p>
	 * 
	 * @param groupID
	 *            is the given <tt>String</tt> to convert.
	 * 
	 * @return a {@link GroupID} object, which is equal to the given
	 *         <tt>String</tt>.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given <tt>String</tt> is <tt>null</tt>.
	 * @throws IllegalGroupIDException
	 *             <ul>
	 *             <li>if the given <tt>String</tt> is empty ;</li>
	 *             <li>if the given <tt>String</tt> is < 0 ;</li>
	 *             </ul>
	 */
	public static GroupID parseString(String groupID)
			throws IllegalGroupIDException {
		return new GroupID(groupID);
	}

	private String _value;

	public GroupID(String sGroupID) throws IllegalGroupIDException {
		setValue(sGroupID);
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
		if (anObject instanceof GroupID) {
			GroupID inter = (GroupID) anObject;
			return getValue().equals(inter.getValue());
		}
		return false;
	}

	public int toInt() {
		return Integer.parseInt(getValue());
	}

	public String getValue() {
		return _value;
	}

	private String setValue(String groupID) throws IllegalGroupIDException {
		String previous = toString();
		if (groupID == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a "
					+ GroupID.class.getCanonicalName() + ").");
		}
		if (groupID.trim().length() == 0) {
			throw new IllegalGroupIDException(Msg.bind(
					Messages.GroupIDEx_EMPTY, groupID));
		}
		int iGID = 0;
		try {
			iGID = Integer.parseInt(groupID);
		} catch (NumberFormatException Ex) {
			throw new IllegalGroupIDException(Msg.bind(
					Messages.GroupIDEx_INVALID, groupID));
		}
		if (iGID < 0) {
			throw new IllegalGroupIDException(Msg.bind(
					Messages.GroupIDEx_INVALID, groupID));
		}
		_value = groupID;
		return previous;
	}

}