package com.wat.melody.common.endpoint;

import com.wat.melody.common.endpoint.exception.IllegalContextRootException;
import com.wat.melody.common.messages.Msg;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class ContextRoot {

	/**
	 * <p>
	 * Convert the given <tt>String</tt> to a {@link ContextRoot} object.
	 * </p>
	 * 
	 * @param contextRoot
	 *            is the given <tt>String</tt> to convert.
	 * 
	 * @return a {@link ContextRoot} object, which is equal to the given
	 *         <tt>String</tt>.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given <tt>String</tt> is <tt>null</tt>.
	 * @throws IllegalContextRootException
	 *             <ul>
	 *             <li>if the given <tt>String</tt> is empty ;</li>
	 *             <li>if the given <tt>String</tt> doesn't match the pattern
	 *             {@link #PATTERN} ;</li>
	 *             </ul>
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
			throw new IllegalContextRootException(Msg.bind(
					Messages.ContextRootEx_EMPTY, contextRoot));
		} else if (!contextRoot.matches("^" + PATTERN + "$")) {
			throw new IllegalContextRootException(Msg.bind(
					Messages.ContextRootEx_INVALID, contextRoot, PATTERN));
		}
		String previous = getValue();
		msValue = contextRoot;
		return previous;
	}
}