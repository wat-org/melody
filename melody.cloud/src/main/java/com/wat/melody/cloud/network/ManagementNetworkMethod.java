package com.wat.melody.cloud.network;

import java.util.Arrays;

import com.wat.melody.cloud.network.exception.IllegalManagementMethodNetworkException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public enum ManagementNetworkMethod {

	SSH("ssh"), WINRM("winrm");

	/**
	 * <p>
	 * Convert the given <code>String</code> to a
	 * {@link ManagementNetworkMethod} object.
	 * </p>
	 * 
	 * @param sMethod
	 *            is the given <code>String</code> to convert.
	 * 
	 * @return a <code>ManagementMethod</code> object, whose equal to the given
	 *         input <code>String</code>.
	 * 
	 * @throws IllegalManagementMethodNetworkException
	 *             if the given input <code>String</code> is not a valid
	 *             <code>ManagementMethod</code> Enumeration Constant.
	 * @throws IllegalArgumentException
	 *             if the given input <code>String</code> is <code>null</code>.
	 */
	public static ManagementNetworkMethod parseString(String sMethod)
			throws IllegalManagementMethodNetworkException {
		if (sMethod == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a "
					+ ManagementNetworkMethod.class.getCanonicalName()
					+ " Enumeration Constant. Accepted values are "
					+ Arrays.asList(ManagementNetworkMethod.values()) + ").");
		}
		if (sMethod.trim().length() == 0) {
			throw new IllegalManagementMethodNetworkException(Messages.bind(
					Messages.MgmtNetworkMethodEx_EMPTY, sMethod));
		}
		for (ManagementNetworkMethod c : ManagementNetworkMethod.class
				.getEnumConstants()) {
			if (sMethod.equalsIgnoreCase(c.getValue())) {
				return c;
			}
		}
		throw new IllegalManagementMethodNetworkException(Messages.bind(
				Messages.MgmtNetworkMethodEx_INVALID, sMethod,
				Arrays.asList(ManagementNetworkMethod.values())));
	}

	private final String msValue;

	private ManagementNetworkMethod(String v) {
		this.msValue = v;
	}

	@Override
	public String toString() {
		return msValue;
	}

	private String getValue() {
		return msValue;
	}

}
