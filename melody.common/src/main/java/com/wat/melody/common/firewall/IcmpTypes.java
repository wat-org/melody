package com.wat.melody.common.firewall;

import java.util.LinkedHashSet;

import com.wat.melody.common.firewall.exception.IllegalIcmpTypeException;
import com.wat.melody.common.firewall.exception.IllegalIcmpTypesException;
import com.wat.melody.common.messages.Msg;

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
	 * Convert the given <tt>String</tt> to an {@link IcmpTypes} object.
	 * </p>
	 * 
	 * Input <tt>String</tt> must respect the following pattern :
	 * <tt>icmpType(','icmpType)*</tt>
	 * <ul>
	 * <li>Each <tt>icmpType</tt> must be a valid {@link IcmpType} (see
	 * {@link IcmpType#parseString(String)}) ;</li>
	 * <li>The given <tt>String</tt> can also be equal to 'all', which is
	 * equivalent to an {@link IcmpType} equal to 0 ;</li>
	 * </ul>
	 * 
	 * @param icmpTypes
	 *            is the given <tt>String</tt> to convert.
	 * 
	 * @return an {@link IcmpTypes} object, which is equal to the given
	 *         <tt>String</tt>.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given <tt>String</tt> is <tt>null</tt>.
	 * @throws IllegalIcmpTypesException
	 *             <ul>
	 *             <li>if the given <tt>String</tt> is empty ;</li>
	 *             <li>if an <tt>icmpType</tt> is neither a valid
	 *             {@link IcmpType} nor equals to 'all' ;</li>
	 *             </ul>
	 */
	public static IcmpTypes parseString(String icmpTypes)
			throws IllegalIcmpTypesException {
		return new IcmpTypes(icmpTypes);
	}

	public IcmpTypes(String icmpTypes) throws IllegalIcmpTypesException {
		super();
		setIcmpTypes(icmpTypes);
	}

	public IcmpTypes(IcmpType... icmpTypes) throws IllegalIcmpTypesException {
		super();
		setIcmpTypes(icmpTypes);
	}

	private void setIcmpTypes(IcmpType... icmpTypes)
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
			throw new IllegalIcmpTypesException(Msg.bind(
					Messages.IcmpTypesEx_EMPTY, (Object[]) icmpTypes));
		}
	}

	private void setIcmpTypes(String icmpTypes)
			throws IllegalIcmpTypesException {
		if (icmpTypes == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (an "
					+ IcmpTypes.class.getCanonicalName() + ").");
		}
		clear();
		for (String icmpType : icmpTypes.split(TYPES_SEPARATOR)) {
			icmpType = icmpType.trim();
			if (icmpType.length() == 0) {
				throw new IllegalIcmpTypesException(Msg.bind(
						Messages.IcmpTypesEx_EMPTY_TYPE, icmpTypes));
			}
			if (icmpType.equalsIgnoreCase(_ALL)) {
				add(IcmpType.ALL);
				continue;
			}
			try {
				add(IcmpType.parseString(icmpType));
			} catch (IllegalIcmpTypeException Ex) {
				throw new IllegalIcmpTypesException(Msg.bind(
						Messages.IcmpTypesEx_INVALID_TYPE, icmpTypes), Ex);
			}
		}
		if (size() == 0) {
			throw new IllegalIcmpTypesException(Msg.bind(
					Messages.IcmpTypesEx_EMPTY, icmpTypes));
		}
	}

}