package com.wat.melody.common.transfer.resources.attributes;

import java.util.Arrays;

import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.transfer.Messages;
import com.wat.melody.common.transfer.exception.IllegalScopeException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public enum Scope {

	FILES("files"), DIRECTORIES("directories"), LINKS("links");

	/**
	 * <p>
	 * Convert the given <tt>String</tt> to a {@link Scope} object.
	 * </p>
	 * 
	 * @param scope
	 *            is the given <tt>String</tt> to convert.
	 * 
	 * @return a {@link Scope} object, which is equal to the given
	 *         <tt>String</tt>.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given <tt>String</tt> is <tt>null</tt>.
	 * @throws IllegalScopeException
	 *             <ul>
	 *             <li>if the given <tt>String</tt> is not a valid {@link Scope}
	 *             Enumeration Constant ;</li>
	 *             <li>if the given <tt>String</tt> is empty ;</li>
	 *             </ul>
	 */
	public static Scope parseString(String scope) throws IllegalScopeException {
		if (scope == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a "
					+ Scope.class.getCanonicalName()
					+ " Enumeration Constant. Accepted values are "
					+ Arrays.asList(Scope.values()) + ").");
		}
		if (scope.trim().length() == 0) {
			throw new IllegalScopeException(Msg.bind(Messages.ScopeEx_EMPTY,
					scope));
		}
		for (Scope c : Scope.class.getEnumConstants()) {
			if (c.getValue().equalsIgnoreCase(scope.trim())) {
				return c;
			}
		}
		throw new IllegalScopeException(Msg.bind(Messages.ScopeEx_INVALID,
				scope, Arrays.asList(Scope.values())));
	}

	private final String _value;

	private Scope(String scope) {
		this._value = scope;
	}

	public String getValue() {
		return _value;
	}

}