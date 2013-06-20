package com.wat.melody.common.order;

import java.util.ArrayList;

import com.wat.melody.common.messages.Msg;
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
	 * Convert the given <tt>String</tt> to an {@link OrderNameSet} object.
	 * </p>
	 * 
	 * Input <tt>String</tt> must respect the following pattern :
	 * <tt>ordername(','ordername)*</tt>
	 * <ul>
	 * <li>Each <tt>ordername</tt> must be a valid {@link OrderName} (see
	 * {@link OrderName#parseString(String)}) ;</li>
	 * </ul>
	 * 
	 * @param orders
	 *            is the given <tt>String</tt> to convert.
	 * 
	 * @return an {@link OrderNameSet} object, which is equal to the given
	 *         <tt>String</tt>.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given <tt>String</tt> is <tt>null</tt>.
	 * @throws IllegalOrderNameSetException
	 *             <ul>
	 *             <li>if the given <tt>String</tt> is empty ;</li>
	 *             <li>if an <tt>ordername</tt> is not a valid {@link Order} ;</li>
	 *             </ul>
	 */
	public static OrderNameSet parseOrderNameSet(String orders)
			throws IllegalOrderNameSetException {
		return new OrderNameSet(orders);
	}

	/**
	 * <p>
	 * Create an empty {@link OrderNameSet}.
	 * </p>
	 */
	public OrderNameSet() {
		super();
	}

	public OrderNameSet(String orders) throws IllegalOrderNameSetException {
		super();
		setOrdersSet(orders);
	}

	private void setOrdersSet(String orders)
			throws IllegalOrderNameSetException {
		if (orders == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ OrderNameSet.class.getCanonicalName() + ".");
		}
		clear();
		for (String order : orders.split(",")) {
			order = order.trim();
			if (order.length() == 0) {
				throw new IllegalOrderNameSetException(Msg.bind(
						Messages.OrderNameSetEx_EMPTY_ORDER_NAME, orders));
			}
			try {
				add(OrderName.parseString(order));
			} catch (IllegalOrderNameException Ex) {
				throw new IllegalOrderNameSetException(Msg.bind(
						Messages.OrderNameSetEx_INVALID_ORDER_NAME, orders), Ex);
			}
		}
		if (size() == 0) {
			throw new IllegalOrderNameSetException(Msg.bind(
					Messages.OrderNameSetEx_EMPTY, orders));
		}
	}

}