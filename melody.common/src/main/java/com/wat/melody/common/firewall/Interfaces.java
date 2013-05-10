package com.wat.melody.common.firewall;

import java.util.LinkedHashSet;

import com.wat.melody.common.firewall.exception.IllegalInterfaceException;
import com.wat.melody.common.firewall.exception.IllegalInterfacesException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class Interfaces extends LinkedHashSet<Interface> {

	private static final long serialVersionUID = -534567888987653292L;

	public static final String INTERFACES_SEPARATOR = ",";

	public static final Interfaces ALL = createInterfaces(Interface.ALL);

	private static Interfaces createInterfaces(Interface... interfaces) {
		try {
			return new Interfaces(interfaces);
		} catch (IllegalInterfacesException Ex) {
			throw new RuntimeException("Unexpected error while initializing "
					+ "an Interface with value '" + interfaces + "'. "
					+ "Because this default value initialization is "
					+ "hardcoded, such error cannot happened. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.", Ex);
		}
	}

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
	 * @param sInterfaces
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
	public static Interfaces parseString(String sInterfaces)
			throws IllegalInterfacesException {
		return new Interfaces(sInterfaces);
	}

	public Interfaces(String sInterfaces) throws IllegalInterfacesException {
		super();
		setInterfaces(sInterfaces);
	}

	public Interfaces(Interface... interfaces)
			throws IllegalInterfacesException {
		super();
		setInterfaces(interfaces);
	}

	public void setInterfaces(Interface... interfaces)
			throws IllegalInterfacesException {
		clear();
		if (interfaces == null) {
			return;
		}
		for (Interface inter : interfaces) {
			if (inter == null) {
				continue;
			} else {
				add(inter);
			}
		}
		if (size() == 0) {
			throw new IllegalInterfacesException(Messages.bind(
					Messages.InterfacesEx_EMPTY, interfaces));
		}
	}

	public void setInterfaces(String sInterfaces)
			throws IllegalInterfacesException {
		if (sInterfaces == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a "
					+ Interfaces.class.getCanonicalName() + ").");
		}
		clear();
		for (String inter : sInterfaces.split(INTERFACES_SEPARATOR)) {
			inter = inter.trim();
			if (inter.length() == 0) {
				throw new IllegalInterfacesException(Messages.bind(
						Messages.InterfacesEx_EMPTY_INTERFACE, sInterfaces));
			}
			try {
				add(Interface.parseString(inter));
			} catch (IllegalInterfaceException Ex) {
				throw new IllegalInterfacesException(Messages.bind(
						Messages.InterfacesEx_INVALID_INTERFACE, sInterfaces),
						Ex);
			}
		}
		if (size() == 0) {
			throw new IllegalInterfacesException(Messages.bind(
					Messages.InterfacesEx_EMPTY, sInterfaces));
		}
	}

}