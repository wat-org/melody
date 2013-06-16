package com.wat.melody.cloud.network.activation;

import java.util.Arrays;

import com.wat.melody.cloud.network.Messages;
import com.wat.melody.cloud.network.activation.exception.IllegalNetworkActivationProtocolException;
import com.wat.melody.common.messages.Msg;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public enum NetworkActivationProtocol {

	SSH("ssh"), WINRM("winrm");

	/**
	 * <p>
	 * Convert the given <tt>String</tt> to a {@link NetworkActivationProtocol}
	 * object.
	 * </p>
	 * 
	 * @param protocol
	 *            is the given <tt>String</tt> to convert.
	 * 
	 * @return a {@link NetworkActivationProtocol} object, which is equal to the
	 *         given <tt>String</tt>.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given <tt>String</tt> is <tt>null</tt>.
	 * @throws IllegalNetworkActivationProtocolException
	 *             <ul>
	 *             <li>if the given <tt>String</tt> is empty ;</li>
	 *             <li>if the given <tt>String</tt> is not one of the
	 *             {@link NetworkActivationProtocol} Enumeration Constant ;</li>
	 *             </ul>
	 */
	public static NetworkActivationProtocol parseString(String protocol)
			throws IllegalNetworkActivationProtocolException {
		if (protocol == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a "
					+ NetworkActivationProtocol.class.getCanonicalName()
					+ " Enumeration Constant. Accepted values are "
					+ Arrays.asList(NetworkActivationProtocol.values()) + ").");
		}
		if (protocol.trim().length() == 0) {
			throw new IllegalNetworkActivationProtocolException(Msg.bind(
					Messages.NetworkActivationProtocolEx_EMPTY, protocol));
		}
		for (NetworkActivationProtocol c : NetworkActivationProtocol.class
				.getEnumConstants()) {
			if (protocol.equalsIgnoreCase(c.getValue())) {
				return c;
			}
		}
		throw new IllegalNetworkActivationProtocolException(Msg.bind(
				Messages.NetworkActivationProtocolEx_INVALID, protocol,
				Arrays.asList(NetworkActivationProtocol.values())));
	}

	private final String _value;

	private NetworkActivationProtocol(String v) {
		this._value = v;
	}

	@Override
	public String toString() {
		return _value;
	}

	private String getValue() {
		return _value;
	}

}