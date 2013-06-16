package com.wat.melody.common.filter;

import com.wat.melody.common.filter.exception.IllegalFilterException;
import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.order.OrderName;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class Filter {

	/**
	 * <p>
	 * Convert the given <tt>String</tt> to a {@link Filter} object.
	 * </p>
	 * 
	 * @param filter
	 *            is the given <tt>String</tt> to convert.
	 * 
	 * @return a {@link Filter} object, whose equal to the given input
	 *         <tt>String</tt>.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given input <tt>String</tt> is <tt>null</tt>.
	 * @throws IllegalFilterException
	 *             if the given input <tt>String</tt> is empty.
	 */
	public static Filter parseFilter(String filter)
			throws IllegalFilterException {
		return new Filter(filter);
	}

	private String _value;

	public Filter(String filter) throws IllegalFilterException {
		setValue(filter);
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
			Filter on = (Filter) anObject;
			return getValue().equals(on.getValue());
		}
		return false;
	}

	public String getValue() {
		return _value;
	}

	private String setValue(String filter) throws IllegalFilterException {
		if (filter == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a Filter).");
		}
		if (filter.trim().length() == 0) {
			throw new IllegalFilterException(Msg.bind(Messages.FilterEx_EMPTY,
					filter));
		}
		String previous = getValue();
		_value = filter;
		return previous;
	}

}