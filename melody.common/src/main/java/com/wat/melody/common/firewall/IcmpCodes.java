package com.wat.melody.common.firewall;

import java.util.LinkedHashSet;

import com.wat.melody.common.firewall.exception.IllegalIcmpCodeException;
import com.wat.melody.common.firewall.exception.IllegalIcmpCodesException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class IcmpCodes extends LinkedHashSet<IcmpCode> {

	private static final long serialVersionUID = -546548770857564345L;

	public static final String CODES_SEPARATOR = ",";

	private static final String _ALL = "all";

	public static IcmpCodes ALL = createIcmpCodes(IcmpCode.ALL);

	private static IcmpCodes createIcmpCodes(IcmpCode... icmpCodes) {
		try {
			return new IcmpCodes(icmpCodes);
		} catch (IllegalIcmpCodesException Ex) {
			throw new RuntimeException("Unexpected error while initializing "
					+ "the Icmp Codes with its default value. "
					+ "Because this default value initialization is "
					+ "hardcoded, such error cannot happened. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.", Ex);
		}
	}

	/**
	 * <p>
	 * Convert the given <code>String</code> to a {@link IcmpCodes} object.
	 * </p>
	 * 
	 * <p>
	 * <i> * Input <code>String</code> must respect the following pattern :
	 * <code>IcmpCode(','IcmpCode)*</code>. <BR/>
	 * * IcmpCode must be a valid {@link IcmpCode} (see
	 * {@link IcmpCode#parseString(String)}). <BR/>
	 * </i>
	 * </p>
	 * 
	 * @param sIcmpCodes
	 *            is the given <code>String</code> to convert.
	 * 
	 * @return a {@link IcmpCodes} object, whose equal to the given input
	 *         <code>String</code>.
	 * 
	 * @throws IllegalIcmpCodesException
	 *             if the given input <code>String</code> is not a valid
	 *             {@link IcmpCodes}.
	 * @throws IllegalArgumentException
	 *             if the given input <code>String</code> is <code>null</code>.
	 */
	public static IcmpCodes parseString(String sIcmpCodes)
			throws IllegalIcmpCodesException {
		return new IcmpCodes(sIcmpCodes);
	}

	public IcmpCodes(String icmpCodes) throws IllegalIcmpCodesException {
		super();
		setIcmpCodes(icmpCodes);
	}

	public IcmpCodes(IcmpCode... icmpCodes) throws IllegalIcmpCodesException {
		super();
		setIcmpCodes(icmpCodes);
	}

	public void setIcmpCodes(IcmpCode... icmpCodes)
			throws IllegalIcmpCodesException {
		clear();
		if (icmpCodes == null) {
			return;
		}
		for (IcmpCode icmpCode : icmpCodes) {
			if (icmpCode == null) {
				continue;
			} else {
				add(icmpCode);
			}
		}
		if (size() == 0) {
			throw new IllegalIcmpCodesException(Messages.bind(
					Messages.IcmpCodesEx_EMPTY, icmpCodes));
		}
	}

	public void setIcmpCodes(String sIcmpCodes)
			throws IllegalIcmpCodesException {
		if (sIcmpCodes == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a "
					+ IcmpCodes.class.getCanonicalName() + ").");
		}
		clear();
		for (String icmpCode : sIcmpCodes.split(CODES_SEPARATOR)) {
			icmpCode = icmpCode.trim();
			if (icmpCode.length() == 0) {
				throw new IllegalIcmpCodesException(Messages.bind(
						Messages.IcmpCodesEx_EMPTY_CODE, sIcmpCodes));
			}
			if (icmpCode.equalsIgnoreCase(_ALL)) {
				add(IcmpCode.ALL);
				continue;
			}
			try {
				add(IcmpCode.parseString(icmpCode));
			} catch (IllegalIcmpCodeException Ex) {
				throw new IllegalIcmpCodesException(Messages.bind(
						Messages.IcmpCodesEx_INVALID_CODE, sIcmpCodes), Ex);
			}
		}
		if (size() == 0) {
			throw new IllegalIcmpCodesException(Messages.bind(
					Messages.IcmpCodesEx_EMPTY, sIcmpCodes));
		}
	}

}