package com.wat.melody.common.order;

import java.util.ArrayList;

import com.wat.melody.common.order.exception.IllegalOrderNameException;
import com.wat.melody.common.order.exception.IllegalOrderNameSetException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class OrderNameSet extends ArrayList<OrderName> {

	private static final long serialVersionUID = -5425800801917819044L;

	/**
	 * <p>
	 * Convert the given <code>String</code> to an {@link OrderNameSet} object.
	 * </p>
	 * 
	 * <p>
	 * <i> * Input <code>String</code> must respect the following pattern :
	 * <code>OrderName(','OrderName)*</code>. <BR/>
	 * * Each OrderName must be a valid {@link OrderNameSet} (see
	 * {@link OrderName#parseString(String)}). <BR/>
	 * </i>
	 * </p>
	 * 
	 * @param sOrderNames
	 *            is the given <code>String</code> to convert.
	 * 
	 * @return an <code>OrderNameSet</code> object, whose equal to the given
	 *         input <code>String</code>.
	 * 
	 * 
	 * @throws IllegalOrderNameSetException
	 *             if the given input <code>String</code> is not a valid
	 *             <code>OrderNameSet</code>.
	 * @throws IllegalArgumentException
	 *             if the given input <code>String</code> is <code>null</code>.
	 */
	public static OrderNameSet parseOrdersSet(String sOrderNames)
			throws IllegalOrderNameSetException {
		return new OrderNameSet(sOrderNames);
	}

	/**
	 * <p>
	 * Create an empty <code>OrdersSet</code>.
	 * </p>
	 */
	public OrderNameSet() {
		super();
	}

	public OrderNameSet(String sOrderName) throws IllegalOrderNameSetException {
		super();
		setOrdersSet(sOrderName);
	}

	public void setOrdersSet(String sOrderNames)
			throws IllegalOrderNameSetException {
		if (sOrderNames == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (an "
					+ OrderNameSet.class.getCanonicalName() + ").");
		}
		clear();
		for (String order : sOrderNames.split(",")) {
			order = order.trim();
			if (order.length() == 0) {
				throw new IllegalOrderNameSetException(Messages.bind(
						Messages.OrderNameSetEx_EMPTY_ORDER_NAME, sOrderNames));
			}
			try {
				add(OrderName.parseString(order));
			} catch (IllegalOrderNameException Ex) {
				throw new IllegalOrderNameSetException(
						Messages.bind(
								Messages.OrderNameSetEx_INVALID_ORDER_NAME,
								sOrderNames), Ex);
			}
		}
		if (size() == 0) {
			throw new IllegalOrderNameSetException(Messages.bind(
					Messages.OrderNameSetEx_EMPTY, sOrderNames));
		}
	}
}
