package com.wat.melody.common.endpoint;

import com.wat.melody.common.endpoint.exception.IllegalContextRootException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class ContextRoot {

	/**
	 * <p>
	 * Convert the given <tt>String</tt> to an {@link ContextRoot} object.
	 * </p>
	 * 
	 * @param contextRoot
	 *            is the given <tt>String</tt> to convert.
	 * 
	 * @return a {@link ContextRoot} object, whose equal to the given input
	 *         <tt>String</tt>.
	 * 
	 * @throws IllegalContextRootException
	 *             if the given input <tt>String</tt> is not a valid
	 *             {@link ContextRoot}.
	 * @throws IllegalArgumentException
	 *             if the given input <tt>String</tt> is <tt>null</tt>.
	 */
	public static ContextRoot parseString(String contextRoot)
			throws IllegalContextRootException {
		return new ContextRoot(contextRoot);
	}

	/**
	 * The pattern which the 'name' must satisfied
	 */
	public static final String PATTERN = "[\\w-_]+";

	private String msValue;

	public ContextRoot(String contextRoot) throws IllegalContextRootException {
		setValue(contextRoot);
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
		if (anObject instanceof ContextRoot) {
			ContextRoot cr = (ContextRoot) anObject;
			return getValue().equals(cr.getValue());
		}
		return false;
	}

	public String getValue() {
		return msValue;
	}

	private String setValue(String contextRoot)
			throws IllegalContextRootException {
		if (contextRoot == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a contextRoot).");
		}
		if (contextRoot.trim().length() == 0) {
			throw new IllegalContextRootException(Messages.bind(
					Messages.ContextRootEx_EMPTY, contextRoot));
		} else if (!contextRoot.matches("^" + PATTERN + "$")) {
			throw new IllegalContextRootException(Messages.bind(
					Messages.ContextRootEx_INVALID, contextRoot, PATTERN));
		}
		String previous = getValue();
		msValue = contextRoot;
		return previous;
	}
}
