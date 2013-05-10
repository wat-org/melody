package com.wat.melody.common.firewall;

import java.util.LinkedHashSet;

import com.wat.melody.common.firewall.exception.IllegalIcmpTypeException;
import com.wat.melody.common.firewall.exception.IllegalIcmpTypesException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class IcmpTypes extends LinkedHashSet<IcmpType> {

	private static final long serialVersionUID = -546548770857564345L;

	public static final String TYPES_SEPARATOR = ",";

	private static final String _ALL = "all";

	public static IcmpTypes ALL = createIcmpTypes(IcmpType.ALL);

	private static IcmpTypes createIcmpTypes(IcmpType... icmpTypes) {
		try {
			return new IcmpTypes(icmpTypes);
		} catch (IllegalIcmpTypesException Ex) {
			throw new RuntimeException("Unexpected error while initializing "
					+ "the Icmp Types with its default value. "
					+ "Because this default value initialization is "
					+ "hardTyped, such error cannot happened. "
					+ "Source Type has certainly been modified and "
					+ "a bug have been introduced.", Ex);
		}
	}

	/**
	 * <p>
	 * Convert the given <code>String</code> to a {@link IcmpTypes} object.
	 * </p>
	 * 
	 * <p>
	 * <i> * Input <code>String</code> must respect the following pattern :
	 * <code>IcmpType(','IcmpType)*</code>. <BR/>
	 * * IcmpType must be a valid {@link IcmpType} (see
	 * {@link IcmpType#parseString(String)}). <BR/>
	 * </i>
	 * </p>
	 * 
	 * @param sIcmpTypes
	 *            is the given <code>String</code> to convert.
	 * 
	 * @return a {@link IcmpTypes} object, whose equal to the given input
	 *         <code>String</code>.
	 * 
	 * @throws IllegalIcmpTypesException
	 *             if the given input <code>String</code> is not a valid
	 *             {@link IcmpTypes}.
	 * @throws IllegalArgumentException
	 *             if the given input <code>String</code> is <code>null</code>.
	 */
	public static IcmpTypes parseString(String sIcmpTypes)
			throws IllegalIcmpTypesException {
		return new IcmpTypes(sIcmpTypes);
	}

	public IcmpTypes(String icmpTypes) throws IllegalIcmpTypesException {
		super();
		setIcmpTypes(icmpTypes);
	}

	public IcmpTypes(IcmpType... icmpTypes) throws IllegalIcmpTypesException {
		super();
		setIcmpTypes(icmpTypes);
	}

	public void setIcmpTypes(IcmpType... icmpTypes)
			throws IllegalIcmpTypesException {
		clear();
		if (icmpTypes == null) {
			return;
		}
		for (IcmpType icmpType : icmpTypes) {
			if (icmpType == null) {
				continue;
			} else {
				add(icmpType);
			}
		}
		if (size() == 0) {
			throw new IllegalIcmpTypesException(Messages.bind(
					Messages.IcmpTypesEx_EMPTY, icmpTypes));
		}
	}

	public void setIcmpTypes(String sIcmpTypes)
			throws IllegalIcmpTypesException {
		if (sIcmpTypes == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a "
					+ IcmpTypes.class.getCanonicalName() + ").");
		}
		clear();
		for (String icmpType : sIcmpTypes.split(TYPES_SEPARATOR)) {
			icmpType = icmpType.trim();
			if (icmpType.length() == 0) {
				throw new IllegalIcmpTypesException(Messages.bind(
						Messages.IcmpTypesEx_EMPTY_TYPE, sIcmpTypes));
			}
			if (icmpType.equalsIgnoreCase(_ALL)) {
				add(IcmpType.ALL);
				continue;
			}
			try {
				add(IcmpType.parseString(icmpType));
			} catch (IllegalIcmpTypeException Ex) {
				throw new IllegalIcmpTypesException(Messages.bind(
						Messages.IcmpTypesEx_INVALID_TYPE, sIcmpTypes), Ex);
			}
		}
		if (size() == 0) {
			throw new IllegalIcmpTypesException(Messages.bind(
					Messages.IcmpTypesEx_EMPTY, sIcmpTypes));
		}
	}

}