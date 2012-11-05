package com.wat.melody.common.typedef;

import com.wat.melody.common.typedef.exception.IllegalModifiersException;

public class Modifiers {

	public static final String PATTERN = "[0-7]{3}";

	/**
	 * <p>
	 * Convert the given <code>String</code> to an {@link Modifiers} object.
	 * </p>
	 * 
	 * <p>
	 * <i> * Input <code>String</code> must respect the following pattern :
	 * "[0-7]{3}" <BR/>
	 * </i>
	 * </p>
	 * 
	 * @param sModifiers
	 *            is the given <code>String</code> to convert.
	 * 
	 * @return a <code>Modifiers</code> object, whose equal to the given input
	 *         <code>String</code>.
	 * 
	 * 
	 * @throws IllegalModifiersException
	 *             if the given input <code>String</code> is not a valid
	 *             <code>Modifiers</code>.
	 * @throws IllegalArgumentException
	 *             if the given input <code>String</code> is <code>null</code>.
	 */
	public static Modifiers parseString(String sModifiers)
			throws IllegalModifiersException {
		return new Modifiers(sModifiers);
	}

	private String msValue;

	public Modifiers(String sModifiers) throws IllegalModifiersException {
		setValue(sModifiers);
	}

	public int toInt() {
		int foo = 0;
		for (byte k : getValue().getBytes()) {
			foo <<= 3;
			foo |= (k - '0');
		}
		return foo;
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
		if (anObject instanceof Modifiers) {
			Modifiers inter = (Modifiers) anObject;
			return getValue().equals(inter.getValue());
		}
		return false;
	}

	public String getValue() {
		return msValue;
	}

	public String setValue(String sModifiers) throws IllegalModifiersException {
		String previous = toString();
		if (sModifiers == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (an "
					+ Modifiers.class.getCanonicalName() + ").");
		}
		if (sModifiers.trim().length() == 0) {
			throw new IllegalModifiersException(Messages.bind(
					Messages.ModifiersEx_EMPTY, sModifiers));
		} else if (!sModifiers.matches("^" + PATTERN + "$")) {
			throw new IllegalModifiersException(Messages.bind(
					Messages.ModifiersEx_INVALID, sModifiers, PATTERN));
		}
		msValue = sModifiers;
		return previous;
	}

}
