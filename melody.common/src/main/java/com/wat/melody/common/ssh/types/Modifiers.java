package com.wat.melody.common.ssh.types;

import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.ssh.types.exception.IllegalModifiersException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class Modifiers {

	public static final String PATTERN = "[0-7]{3}";

	/**
	 * <p>
	 * Convert the given <tt>String</tt> to a {@link Modifiers} object.
	 * </p>
	 * 
	 * <p>
	 * Input <tt>String</tt> must respect the following pattern :
	 * <tt>[0-7]{3}</tt>
	 * </p>
	 * 
	 * @param modifiers
	 *            is the given <tt>String</tt> to convert.
	 * 
	 * @return a {@link Modifiers} object, which is equal to the given
	 *         <tt>String</tt>.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given <tt>String</tt> is <tt>null</tt>.
	 * @throws IllegalModifiersException
	 *             <ul>
	 *             <li>if the given <tt>String</tt> is empty ;</li>
	 *             <li>if the given <tt>String</tt> is doesn't respect the
	 *             pattern {@link #PATTERN} ;</li>
	 *             </ul>
	 */
	public static Modifiers parseString(String modifiers)
			throws IllegalModifiersException {
		return new Modifiers(modifiers);
	}

	private String _value;

	public Modifiers(String modifiers) throws IllegalModifiersException {
		setValue(modifiers);
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
		if (anObject instanceof Modifiers) {
			Modifiers inter = (Modifiers) anObject;
			return getValue().equals(inter.getValue());
		}
		return false;
	}

	public int toInt() {
		int foo = 0;
		for (byte k : getValue().getBytes()) {
			foo <<= 3;
			foo |= (k - '0');
		}
		return foo;
	}

	public String getValue() {
		return _value;
	}

	private String setValue(String modifiers) throws IllegalModifiersException {
		String previous = toString();
		if (modifiers == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a "
					+ Modifiers.class.getCanonicalName() + ").");
		}
		if (modifiers.trim().length() == 0) {
			throw new IllegalModifiersException(Msg.bind(
					Messages.ModifiersEx_EMPTY, modifiers));
		} else if (!modifiers.matches("^" + PATTERN + "$")) {
			throw new IllegalModifiersException(Msg.bind(
					Messages.ModifiersEx_INVALID, modifiers, PATTERN));
		}
		_value = modifiers;
		return previous;
	}

}