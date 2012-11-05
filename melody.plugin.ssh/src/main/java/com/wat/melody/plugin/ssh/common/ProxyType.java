package com.wat.melody.plugin.ssh.common;

import java.util.Arrays;

import com.wat.melody.plugin.ssh.common.exception.IllegalProxyTypeException;

public enum ProxyType {

	ProxyHTTP("ProxyHTTP"), ProxySOCKS4("ProxySOCKS4"), ProxySOCKS5(
			"ProxySOCKS5");

	/**
	 * <p>
	 * Convert the given <code>String</code> to a {@link ProxyType} object.
	 * </p>
	 * 
	 * @param sProxyType
	 *            is the given <code>String</code> to convert.
	 * 
	 * @return a <code>ProxyType</code> object, whose equal to the given input
	 *         <code>String</code>.
	 * 
	 * @throws IllegalProxyTypeException
	 *             if the given input <code>String</code> is not a valid
	 *             <code>ProxyType</code> Enumeration Constant.
	 * @throws IllegalArgumentException
	 *             if the given input <code>String</code> is <code>null</code>.
	 */
	public static ProxyType parseString(String sProxyType)
			throws IllegalProxyTypeException {
		if (sProxyType == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (an "
					+ ProxyType.class.getCanonicalName()
					+ " Enumeration Constant. Accepted values are "
					+ Arrays.asList(ProxyType.values()) + " ).");
		}
		if (sProxyType.trim().length() == 0) {
			throw new IllegalProxyTypeException(Messages.bind(
					Messages.ProxyTypeEx_EMPTY, sProxyType));
		}
		for (ProxyType c : ProxyType.class.getEnumConstants()) {
			if (c.getValue().equalsIgnoreCase(sProxyType)) {
				return c;
			}
		}
		throw new IllegalProxyTypeException(Messages.bind(
				Messages.ProxyTypeEx_INVALID, sProxyType,
				Arrays.asList(ProxyType.values())));
	}

	private final String msValue;

	private ProxyType(String sProxyType) {
		this.msValue = sProxyType;
	}

	public String getValue() {
		return msValue;
	}

}
