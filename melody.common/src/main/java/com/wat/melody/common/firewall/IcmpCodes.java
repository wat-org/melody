package com.wat.melody.common.firewall;

import java.util.LinkedHashSet;

import com.wat.melody.common.firewall.exception.IllegalIcmpCodeException;
import com.wat.melody.common.firewall.exception.IllegalIcmpCodesException;
import com.wat.melody.common.messages.Msg;

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
	 * Convert the given <tt>String</tt> to an {@link IcmpCodes} object.
	 * </p>
	 * 
	 * Input <tt>String</tt> must respect the following pattern :
	 * <tt>icmpCode(','icmpCode)*</tt>
	 * <ul>
	 * <li>Each <tt>icmpCode</tt> must be a valid {@link IcmpCode} (see
	 * {@link IcmpCode#parseString(String)}) ;</li>
	 * <li>The given <tt>String</tt> can also be equal to 'all', which is
	 * equivalent to an {@link IcmpCode} equal to 0 ;</li>
	 * </ul>
	 * 
	 * @param icmpCodes
	 *            is the given <tt>String</tt> to convert.
	 * 
	 * @return an {@link IcmpCodes} object, which is equal to the given
	 *         <tt>String</tt>.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given <tt>String</tt> is <tt>null</tt>.
	 * @throws IllegalIcmpCodesException
	 *             <ul>
	 *             <li>if the given <tt>String</tt> is empty ;</li>
	 *             <li>if an <tt>icmpCode</tt> is neither a valid
	 *             {@link IcmpCode} nor equals to 'all' ;</li>
	 *             </ul>
	 */
	public static IcmpCodes parseString(String icmpCodes)
			throws IllegalIcmpCodesException {
		return new IcmpCodes(icmpCodes);
	}

	public IcmpCodes(String icmpCodes) throws IllegalIcmpCodesException {
		super();
		setIcmpCodes(icmpCodes);
	}

	public IcmpCodes(IcmpCode... icmpCodes) throws IllegalIcmpCodesException {
		super();
		setIcmpCodes(icmpCodes);
	}

	private void setIcmpCodes(IcmpCode... icmpCodes)
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
			throw new IllegalIcmpCodesException(Msg.bind(
					Messages.IcmpCodesEx_EMPTY, (Object[]) icmpCodes));
		}
	}

	private void setIcmpCodes(String icmpCodes)
			throws IllegalIcmpCodesException {
		if (icmpCodes == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (an "
					+ IcmpCodes.class.getCanonicalName() + ").");
		}
		clear();
		for (String icmpCode : icmpCodes.split(CODES_SEPARATOR)) {
			icmpCode = icmpCode.trim();
			if (icmpCode.length() == 0) {
				throw new IllegalIcmpCodesException(Msg.bind(
						Messages.IcmpCodesEx_EMPTY_CODE, icmpCodes));
			}
			if (icmpCode.equalsIgnoreCase(_ALL)) {
				add(IcmpCode.ALL);
				continue;
			}
			try {
				add(IcmpCode.parseString(icmpCode));
			} catch (IllegalIcmpCodeException Ex) {
				throw new IllegalIcmpCodesException(Msg.bind(
						Messages.IcmpCodesEx_INVALID_CODE, icmpCodes), Ex);
			}
		}
		if (size() == 0) {
			throw new IllegalIcmpCodesException(Msg.bind(
					Messages.IcmpCodesEx_EMPTY, icmpCodes));
		}
	}

}