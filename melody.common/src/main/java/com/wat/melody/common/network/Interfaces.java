package com.wat.melody.common.network;

import java.util.ArrayList;

import com.wat.melody.common.network.exception.IllegalInterfaceException;
import com.wat.melody.common.network.exception.IllegalInterfacesException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class Interfaces extends ArrayList<Interface> {

	private static final long serialVersionUID = -534567888987653292L;

	public static final String INTERFACES_SEPARATOR = ",";

	/**
	 * <p>
	 * Convert the given <code>String</code> to an {@link Interfaces} object.
	 * </p>
	 * 
	 * <p>
	 * <i> * Input <code>String</code> must respect the following pattern :
	 * <code>Interface(','Interface)*</code>. <BR/>
	 * * Interface must be a valid {@link Interface} (see
	 * {@link Interface#parseString(String)}). <BR/>
	 * </i>
	 * </p>
	 * 
	 * @param sProtocols
	 *            is the given <code>String</code> to convert.
	 * 
	 * @return an {@link Interfaces} object, whose equal to the given input
	 *         <code>String</code>.
	 * 
	 * @throws IllegalInterfacesException
	 *             if the given input <code>String</code> is not a valid
	 *             {@link Interfaces}.
	 * @throws IllegalArgumentException
	 *             if the given input <code>String</code> is <code>null</code>.
	 */
	public static Interfaces parseString(String sProtocols)
			throws IllegalInterfacesException {
		return new Interfaces(sProtocols);
	}

	public Interfaces(String sProtocols) throws IllegalInterfacesException {
		super();
		setInterfaces(sProtocols);
	}

	public Interfaces(Interface... protocols) throws IllegalInterfacesException {
		super();
		setInterfaces(protocols);
	}

	public void setInterfaces(Interface... protocols)
			throws IllegalInterfacesException {
		clear();
		if (protocols == null) {
			return;
		}
		for (Interface protocol : protocols) {
			if (protocol == null) {
				continue;
			} else {
				add(protocol);
			}
		}
		if (size() == 0) {
			throw new IllegalInterfacesException(Messages.bind(
					Messages.InterfacesEx_EMPTY, protocols));
		}
	}

	public void setInterfaces(String sProtocols)
			throws IllegalInterfacesException {
		if (sProtocols == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a "
					+ Interfaces.class.getCanonicalName() + ").");
		}
		clear();
		for (String protocol : sProtocols.split(INTERFACES_SEPARATOR)) {
			protocol = protocol.trim();
			if (protocol.length() == 0) {
				throw new IllegalInterfacesException(Messages.bind(
						Messages.InterfacesEx_EMPTY_INTERFACE, sProtocols));
			}
			try {
				add(Interface.parseString(protocol));
			} catch (IllegalInterfaceException Ex) {
				throw new IllegalInterfacesException(Messages.bind(
						Messages.InterfacesEx_INVALID_INTERFACE, sProtocols),
						Ex);
			}
		}
		if (size() == 0) {
			throw new IllegalInterfacesException(Messages.bind(
					Messages.InterfacesEx_EMPTY, sProtocols));
		}
	}

}