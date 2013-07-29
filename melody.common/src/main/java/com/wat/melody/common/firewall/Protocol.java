package com.wat.melody.common.firewall;

import java.util.Arrays;

import com.wat.melody.common.firewall.exception.IllegalProtocolException;
import com.wat.melody.common.messages.Msg;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public enum Protocol {

	TCP("tcp"), UDP("udp"), ICMP("icmp");

	/**
	 * <p>
	 * Convert the given <tt>String</tt> to a {@link Protocol} object.
	 * </p>
	 * 
	 * @param protocol
	 *            is the given <tt>String</tt> to convert.
	 * 
	 * @return a {@link Protocol} object, which is equal to the given
	 *         <tt>String</tt>.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given <tt>String</tt> is <tt>null</tt>.
	 * @throws IllegalProtocolException
	 *             <ul>
	 *             <li>if the given <tt>String</tt> is not a valid
	 *             {@link Protocol} Enumeration Constant ;</li>
	 *             <li>if the given <tt>String</tt> is empty ;</li>
	 *             </ul>
	 */
	public static Protocol parseString(String protocol)
			throws IllegalProtocolException {
		if (protocol == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a "
					+ Protocol.class.getCanonicalName()
					+ " Enumeration Constant. Accepted values are "
					+ Arrays.asList(Protocol.values()) + ").");
		}
		if (protocol.trim().length() == 0) {
			throw new IllegalProtocolException(Msg.bind(
					Messages.ProtocolEx_EMPTY, protocol));
		}
		for (Protocol c : Protocol.class.getEnumConstants()) {
			if (c.getValue().equalsIgnoreCase(protocol)) {
				return c;
			}
		}
		throw new IllegalProtocolException(Msg.bind(
				Messages.ProtocolEx_INVALID, protocol,
				Arrays.asList(Protocol.values())));
	}

	private final String _value;

	private Protocol(String protocol) {
		this._value = protocol;
	}

	public String getValue() {
		return _value;
	}

}