package com.wat.melody.common.ssh.types;

import java.util.Arrays;

import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.ssh.types.exception.IllegalProxyTypeException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public enum ProxyType {

	ProxyHTTP("ProxyHTTP"), ProxySOCKS4("ProxySOCKS4"), ProxySOCKS5(
			"ProxySOCKS5");

	/**
	 * <p>
	 * Convert the given <tt>String</tt> to a {@link ProxyType} object.
	 * </p>
	 * 
	 * @param proxyType
	 *            is the given <tt>String</tt> to convert.
	 * 
	 * @return a {@link ProxyType} object, which is equal to the given
	 *         <tt>String</tt>.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given input <tt>String</tt> is <tt>null</tt>.
	 * @throws IllegalProxyTypeException
	 *             <ul>
	 *             <li>if the given <tt>String</tt> is empty :</li>
	 *             <li>if the given <tt>String</tt> is not not the
	 *             {@link ProxyType} Enumeration Constant ;</li>
	 *             </ul>
	 */
	public static ProxyType parseString(String proxyType)
			throws IllegalProxyTypeException {
		if (proxyType == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a "
					+ ProxyType.class.getCanonicalName()
					+ " Enumeration Constant. Accepted values are "
					+ Arrays.asList(ProxyType.values()) + " ).");
		}
		if (proxyType.trim().length() == 0) {
			throw new IllegalProxyTypeException(Msg.bind(
					Messages.ProxyTypeEx_EMPTY, proxyType));
		}
		for (ProxyType c : ProxyType.class.getEnumConstants()) {
			if (c.getValue().equalsIgnoreCase(proxyType)) {
				return c;
			}
		}
		throw new IllegalProxyTypeException(Msg.bind(
				Messages.ProxyTypeEx_INVALID, proxyType,
				Arrays.asList(ProxyType.values())));
	}

	private final String _value;

	private ProxyType(String proxyType) {
		this._value = proxyType;
	}

	public String getValue() {
		return _value;
	}

}