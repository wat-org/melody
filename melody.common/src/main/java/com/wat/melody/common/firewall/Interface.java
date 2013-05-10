package com.wat.melody.common.firewall;

import com.wat.melody.common.firewall.exception.IllegalInterfaceException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class Interface {

	public static final String PATTERN = ".*";

	private static final String _ALL = "all";

	public static final Interface ALL = createInterface(_ALL);

	private static Interface createInterface(String sInterface) {
		try {
			return new Interface(sInterface);
		} catch (IllegalInterfaceException Ex) {
			throw new RuntimeException("Unexpected error while initializing "
					+ "an Interface with value '" + sInterface + "'. "
					+ "Because this default value initialization is "
					+ "hardcoded, such error cannot happened. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.", Ex);
		}
	}

	/**
	 * <p>
	 * Convert the given <code>String</code> to an {@link Interface} object.
	 * </p>
	 * 
	 * @param sInterface
	 *            is the given <code>String</code> to convert.
	 * 
	 * @return an {@link Interface} object, whose equal to the given input
	 *         <code>String</code>.
	 * 
	 * 
	 * @throws IllegalInterfaceException
	 *             if the given input <code>String</code> is not a valid
	 *             {@link Interface}.
	 * @throws IllegalArgumentException
	 *             if the given input <code>String</code> is <code>null</code>.
	 */
	public static Interface parseString(String sInterface)
			throws IllegalInterfaceException {
		return new Interface(sInterface);
	}

	private String msValue;

	public Interface(String sInterface) throws IllegalInterfaceException {
		setValue(sInterface);
	}

	@Override
	public int hashCode() {
		return getValue().hashCode();
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
		if (anObject instanceof Interface) {
			Interface inter = (Interface) anObject;
			return getValue().equals(inter.getValue());
		}
		return false;
	}

	public String getValue() {
		return msValue;
	}

	public String setValue(String sInterface) throws IllegalInterfaceException {
		String previous = toString();
		if (sInterface == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (an "
					+ Interface.class.getCanonicalName() + ").");
		}
		if (sInterface.trim().length() == 0) {
			throw new IllegalInterfaceException(Messages.bind(
					Messages.InterfaceEx_EMPTY, sInterface));
		} else if (sInterface.equalsIgnoreCase(_ALL)) {
			msValue = _ALL;
			return previous;
		} else if (!sInterface.matches("^" + PATTERN + "$")) {
			throw new IllegalInterfaceException(Messages.bind(
					Messages.InterfaceEx_INVALID, sInterface, PATTERN));
		}
		msValue = sInterface;
		return previous;
	}

}
