package com.wat.melody.common.firewall;

import java.util.LinkedHashSet;

import com.wat.melody.common.firewall.exception.IllegalProtocolException;
import com.wat.melody.common.firewall.exception.IllegalProtocolsException;
import com.wat.melody.common.messages.Msg;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class Protocols extends LinkedHashSet<Protocol> {

	private static final long serialVersionUID = -499090888987653292L;

	public static final String PROTOCOLS_SEPARATOR = ",";

	private static final String _ALL = "all";

	public static Protocols ALL = createProtocols(Protocol.TCP, Protocol.UDP);

	private static Protocols createProtocols(Protocol... protocols) {
		try {
			return new Protocols(protocols);
		} catch (IllegalProtocolsException Ex) {
			throw new RuntimeException("Unexpected error while initializing "
					+ "the Protocols with its default value. "
					+ "Because this default value initialization is "
					+ "hardcoded, such error cannot happened. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.", Ex);
		}
	}

	/**
	 * <p>
	 * Convert the given <tt>String</tt> to a {@link Protocols} object.
	 * </p>
	 * 
	 * Input <tt>String</tt> must respect the following pattern :
	 * <tt>protocol(','protocol)*</tt>
	 * <ul>
	 * <li>Each <tt>protocol</tt> must be a valid {@link Protocol} (see
	 * {@link Protocol#parseString(String)}) ;</li>
	 * <li>The given <tt>String</tt> can be equals to 'all', which is equivalent
	 * to {@link Protocol#TCP} and {@link Protocol#UDP} ;</li>
	 * </ul>
	 * 
	 * @param protocols
	 *            is the given <tt>String</tt> to convert.
	 * 
	 * @return a {@link Protocols} object, which is equal to the given
	 *         <tt>String</tt>.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given <tt>String</tt> is <tt>null</tt>.
	 * @throws IllegalProtocolsException
	 *             <ul>
	 *             <li>if the given <tt>String</tt> is empty ;</li>
	 *             <li>if a <tt>protocol</tt> is neither a valid
	 *             {@link Protocol} nor equals to 'all' ;</li>
	 *             </ul>
	 */
	public static Protocols parseString(String protocols)
			throws IllegalProtocolsException {
		return new Protocols(protocols);
	}

	public Protocols(String protocols) throws IllegalProtocolsException {
		super();
		setProtocols(protocols);
	}

	public Protocols(Protocol... protocols) throws IllegalProtocolsException {
		super();
		setProtocols(protocols);
	}

	private void setProtocols(Protocol... protocols)
			throws IllegalProtocolsException {
		clear();
		if (protocols == null) {
			return;
		}
		for (Protocol protocol : protocols) {
			if (protocol == null) {
				continue;
			} else {
				add(protocol);
			}
		}
		if (size() == 0) {
			throw new IllegalProtocolsException(Msg.bind(
					Messages.ProtocolsEx_EMPTY, (Object[]) protocols));
		}
	}

	private void setProtocols(String protocols)
			throws IllegalProtocolsException {
		if (protocols == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a "
					+ Protocols.class.getCanonicalName() + ").");
		}
		clear();
		for (String protocol : protocols.split(PROTOCOLS_SEPARATOR)) {
			protocol = protocol.trim();
			if (protocol.length() == 0) {
				throw new IllegalProtocolsException(Msg.bind(
						Messages.ProtocolsEx_EMPTY_PROTOCOL, protocols));
			}
			if (protocol.equalsIgnoreCase(_ALL)) {
				add(Protocol.TCP);
				add(Protocol.UDP);
				continue;
			}
			try {
				add(Protocol.parseString(protocol));
			} catch (IllegalProtocolException Ex) {
				throw new IllegalProtocolsException(Msg.bind(
						Messages.ProtocolsEx_INVALID_PROTOCOL, protocols), Ex);
			}
		}
		if (size() == 0) {
			throw new IllegalProtocolsException(Msg.bind(
					Messages.ProtocolsEx_EMPTY, protocols));
		}
	}

}