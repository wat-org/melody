package com.wat.melody.common.order;

import com.wat.melody.common.order.exception.IllegalOrderNameException;

/**
 * <p>
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public class OrderName {

	/**
	 * <p>
	 * Convert the given <code>String</code> to an {@link OrderName} object.
	 * </p>
	 * 
	 * @param sOrderName
	 *            is the given <code>String</code> to convert.
	 * 
	 * @return an <code>OrderName</code> object, whose equal to the given input
	 *         <code>String</code>.
	 * 
	 * @throws IllegalOrderNameException
	 *             if the given input <code>String</code> is not a valid
	 *             <code>OrderName</code>.
	 * @throws IllegalArgumentException
	 *             if the given input <code>String</code> is <code>null</code>.
	 */
	public static OrderName parseString(String sOrderName)
			throws IllegalOrderNameException {
		return new OrderName(sOrderName);
	}

	/**
	 * The pattern an OrderName must satisfy.
	 */
	public static final String PATTERN = "\\w+([.]\\w+)*";

	private String msValue;

	public OrderName(String sOrderName) throws IllegalOrderNameException {
		setValue(sOrderName);
	}

	@Override
	public String toString() {
		return msValue;
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
		return msValue;
	}

	public String setValue(String sOrderName) throws IllegalOrderNameException {
		if (sOrderName == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (an "
					+ OrderName.class.getCanonicalName() + ").");
		}
		if (sOrderName.trim().length() == 0) {
			throw new IllegalOrderNameException(Messages.bind(
					Messages.OrderNameEx_EMPTY, sOrderName));
		} else if (!sOrderName.matches("^" + PATTERN + "$")) {
			throw new IllegalOrderNameException(Messages.bind(
					Messages.OrderNameEx_INVALID, sOrderName, PATTERN));
		}
		String previous = toString();
		msValue = sOrderName;
		return previous;
	}

}
