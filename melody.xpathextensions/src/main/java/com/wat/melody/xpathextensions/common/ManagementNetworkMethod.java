package com.wat.melody.xpathextensions.common;

import java.util.Arrays;

import com.wat.melody.xpathextensions.common.exception.IllegalManagementMethodException;

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
	 * @throws IllegalManagementMethodException
	 *             if the given input <code>String</code> is not a valid
	 *             <code>ManagementMethod</code> Enumeration Constant.
	 * @throws IllegalArgumentException
	 *             if the given input <code>String</code> is <code>null</code>.
	 */
	public static ManagementNetworkMethod parseString(String sMethod)
			throws IllegalManagementMethodException {
		if (sMethod == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a "
					+ ManagementNetworkMethod.class.getCanonicalName()
					+ " Enumeration Constant. Accepted values are "
					+ Arrays.asList(ManagementNetworkMethod.values()) + ").");
		}
		if (sMethod.trim().length() == 0) {
			throw new IllegalManagementMethodException(Messages.bind(
					Messages.NetMgmtMethodEx_EMPTY, sMethod));
		}
		for (ManagementNetworkMethod c : ManagementNetworkMethod.class
				.getEnumConstants()) {
			if (sMethod.equalsIgnoreCase(c.getValue())) {
				return c;
			}
		}
		throw new IllegalManagementMethodException(Messages.bind(
				Messages.NetMgmtMethodEx_INVALID, sMethod,
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
