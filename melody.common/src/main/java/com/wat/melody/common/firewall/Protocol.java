package com.wat.melody.common.firewall;

import java.util.Arrays;

import com.wat.melody.common.firewall.exception.IllegalProtocolException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public enum Protocol {

	TCP("tcp"), UDP("udp"), ICMP("icmp");

	/**
	 * <p>
	 * Convert the given <code>String</code> to a {@link Protocol} object.
	 * </p>
	 * 
	 * @param sProtocol
	 *            is the given <code>String</code> to convert.
	 * 
	 * @return an <code>Protocol</code> object, whose equal to the given input
	 *         <code>String</code>.
	 * 
	 * @throws IllegalProtocolException
	 *             if the given input <code>String</code> is not a valid
	 *             <code>Protocol</code> Enumeration Constant.
	 * @throws IllegalArgumentException
	 *             if the given input <code>String</code> is <code>null</code>.
	 */
	public static Protocol parseString(String sProtocol)
			throws IllegalProtocolException {
		if (sProtocol == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a "
					+ Protocol.class.getCanonicalName()
					+ " Enumeration Constant. Accepted values are "
					+ Arrays.asList(Protocol.values()) + ").");
		}
		if (sProtocol.trim().length() == 0) {
			throw new IllegalProtocolException(Messages.bind(
					Messages.ProtocolEx_EMPTY, sProtocol));
		}
		for (Protocol c : Protocol.class.getEnumConstants()) {
			if (c.getValue().equalsIgnoreCase(sProtocol)) {
				return c;
			}
		}
		throw new IllegalProtocolException(Messages.bind(
				Messages.ProtocolEx_INVALID, sProtocol,
				Arrays.asList(Protocol.values())));
	}

	private final String _value;

	private Protocol(String sProtocol) {
		this._value = sProtocol;
	}

	public String getValue() {
		return _value;
	}

}