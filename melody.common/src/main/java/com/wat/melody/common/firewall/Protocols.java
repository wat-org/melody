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
	 * Convert the given <code>String</code> to a {@link Protocols} object.
	 * </p>
	 * 
	 * <p>
	 * <i> * Input <code>String</code> must respect the following pattern :
	 * <code>Protocol(','Protocol)*</code>. <BR/>
	 * * Protocol must be a valid {@link Protocol} (see
	 * {@link Protocol#parseString(String)}). <BR/>
	 * </i>
	 * </p>
	 * 
	 * @param sProtocols
	 *            is the given <code>String</code> to convert.
	 * 
	 * @return a {@link Protocols} object, whose equal to the given input
	 *         <code>String</code>.
	 * 
	 * @throws IllegalProtocolsException
	 *             if the given input <code>String</code> is not a valid
	 *             {@link Protocols}.
	 * @throws IllegalArgumentException
	 *             if the given input <code>String</code> is <code>null</code>.
	 */
	public static Protocols parseString(String sProtocols)
			throws IllegalProtocolsException {
		return new Protocols(sProtocols);
	}

	public Protocols(String sProtocols) throws IllegalProtocolsException {
		super();
		setProtocols(sProtocols);
	}

	public Protocols(Protocol... protocols) throws IllegalProtocolsException {
		super();
		setProtocols(protocols);
	}

	public void setProtocols(Protocol... protocols)
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

	public void setProtocols(String sProtocols)
			throws IllegalProtocolsException {
		if (sProtocols == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a "
					+ Protocols.class.getCanonicalName() + ").");
		}
		clear();
		for (String protocol : sProtocols.split(PROTOCOLS_SEPARATOR)) {
			protocol = protocol.trim();
			if (protocol.length() == 0) {
				throw new IllegalProtocolsException(Msg.bind(
						Messages.ProtocolsEx_EMPTY_PROTOCOL, sProtocols));
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
						Messages.ProtocolsEx_INVALID_PROTOCOL, sProtocols), Ex);
			}
		}
		if (size() == 0) {
			throw new IllegalProtocolsException(Msg.bind(
					Messages.ProtocolsEx_EMPTY, sProtocols));
		}
	}

}