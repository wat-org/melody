package com.wat.melody.common.order;

import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.order.exception.IllegalOrderNameException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class OrderName {

	/**
	 * <p>
	 * Convert the given <tt>String</tt> to an {@link OrderName} object.
	 * </p>
	 * 
	 * @param sOrderName
	 *            is the given <tt>String</tt> to convert.
	 * 
	 * @return an {@link OrderName} object, which is equal to the given
	 *         <tt>String</tt>.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given <tt>String</tt> is <tt>null</tt>.
	 * @throws IllegalOrderNameException
	 *             <ul>
	 *             <li>if the given <tt>String</tt> is empty ;</li>
	 *             <li>if the given <tt>String</tt> doesn't match the pattern
	 *             {@link #PATTERN} ;</li>
	 *             </ul>
	 */
	public static OrderName parseString(String sOrderName)
			throws IllegalOrderNameException {
		return new OrderName(sOrderName);
	}

	/**
	 * The pattern an OrderName must satisfy.
	 */
	public static final String PATTERN = "\\w+([.]\\w+)*";

	private String _value;

	public OrderName(String sOrderName) throws IllegalOrderNameException {
		setValue(sOrderName);
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
		if (anObject instanceof OrderName) {
			OrderName on = (OrderName) anObject;
			return getValue().equals(on.getValue());
		}
		return false;
	}

	public String getValue() {
		return _value;
	}

	private String setValue(String sOrderName) throws IllegalOrderNameException {
		if (sOrderName == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (an "
					+ OrderName.class.getCanonicalName() + ").");
		}
		if (sOrderName.trim().length() == 0) {
			throw new IllegalOrderNameException(Msg.bind(
					Messages.OrderNameEx_EMPTY, sOrderName));
		} else if (!sOrderName.matches("^" + PATTERN + "$")) {
			throw new IllegalOrderNameException(Msg.bind(
					Messages.OrderNameEx_INVALID, sOrderName, PATTERN));
		}
		String previous = toString();
		_value = sOrderName;
		return previous;
	}

}