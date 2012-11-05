package com.wat.melody.common.utils;

import com.wat.melody.common.utils.exception.IllegalFilterException;

/**
 * <p>
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public class Filter {

	/**
	 * <p>
	 * Convert the given <code>String</code> to an {@link Filter} object.
	 * </p>
	 * 
	 * @param filter
	 *            is the given <code>String</code> to convert.
	 * 
	 * @return an <code>Filter</code> object, whose equal to the given input
	 *         <code>String</code>.
	 * 
	 * 
	 * @throws IllegalFilterException
	 *             if the given input <code>String</code> is not a valid
	 *             <code>Filter</code>.
	 * @throws IllegalArgumentException
	 *             if the given input <code>String</code> is <code>null</code>.
	 */
	public static Filter parseFilter(String filter)
			throws IllegalFilterException {
		return new Filter(filter);
	}

	private String msValue;

	public Filter(String filter) throws IllegalFilterException {
		setValue(filter);
	}

	public String getValue() {
		return msValue;
	}

	public String setValue(String filter) throws IllegalFilterException {
		if (filter == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a Filter).");
		}
		if (filter.trim().length() == 0) {
			throw new IllegalFilterException(Messages.bind(
					Messages.FilterEx_EMPTY, filter));
		}
		String previous = getValue();
		msValue = filter;
		return previous;
	}

}