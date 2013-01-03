package com.wat.melody.plugin.ssh.common;

import java.util.Arrays;

import com.wat.melody.plugin.ssh.common.exception.IllegalCompressionTypeException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public enum CompressionType {

	ZLIB("zlib"), NONE("none");

	/**
	 * <p>
	 * Convert the given <code>String</code> to a {@link CompressionType}
	 * object.
	 * </p>
	 * 
	 * @param sCompressionType
	 *            is the given <code>String</code> to convert.
	 * 
	 * @return a <code>CompressionType</code> object, whose equal to the given
	 *         input <code>String</code>.
	 * 
	 * @throws IllegalCompressionTypeException
	 *             if the given input <code>String</code> is not a valid
	 *             <code>CompressionType</code> Enumeration Constant.
	 * @throws IllegalArgumentException
	 *             if the given input <code>String</code> is <code>null</code>.
	 */
	public static CompressionType parseString(String sCompressionType)
			throws IllegalCompressionTypeException {
		if (sCompressionType == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (an "
					+ CompressionType.class.getCanonicalName()
					+ " Enumeration Constant. Accepted values are "
					+ Arrays.asList(CompressionType.values()) + " ).");
		}
		if (sCompressionType.trim().length() == 0) {
			throw new IllegalCompressionTypeException(Messages.bind(
					Messages.CompressionTypeEx_EMPTY, sCompressionType));
		}
		for (CompressionType c : CompressionType.class.getEnumConstants()) {
			if (c.getValue().equalsIgnoreCase(sCompressionType)) {
				return c;
			}
		}
		throw new IllegalCompressionTypeException(Messages.bind(
				Messages.CompressionTypeEx_INVALID, sCompressionType,
				Arrays.asList(CompressionType.values())));
	}

	private final String msValue;

	private CompressionType(String sCompressionType) {
		this.msValue = sCompressionType;
	}

	public String getValue() {
		return msValue;
	}

}
