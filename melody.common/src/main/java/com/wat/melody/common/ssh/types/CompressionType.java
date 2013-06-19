package com.wat.melody.common.ssh.types;

import java.util.Arrays;

import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.ssh.types.exception.IllegalCompressionTypeException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public enum CompressionType {

	ZLIB("zlib"), NONE("none");

	/**
	 * <p>
	 * Convert the given <tt>String</tt> to a {@link CompressionType} object.
	 * </p>
	 * 
	 * @param compressionType
	 *            is the given <tt>String</tt> to convert.
	 * 
	 * @return a {@link CompressionType} object, which is equal to the given
	 *         <tt>String</tt>.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given input <tt>String</tt> is <tt>null</tt>.
	 * @throws IllegalCompressionTypeException
	 *             <ul>
	 *             <li>if the given <tt>String</tt> is empty :</li>
	 *             <li>if the given <tt>String</tt> is not not the
	 *             {@link CompressionType} Enumeration Constant ;</li>
	 *             </ul>
	 */
	public static CompressionType parseString(String compressionType)
			throws IllegalCompressionTypeException {
		if (compressionType == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a "
					+ CompressionType.class.getCanonicalName()
					+ " Enumeration Constant. Accepted values are "
					+ Arrays.asList(CompressionType.values()) + " ).");
		}
		if (compressionType.trim().length() == 0) {
			throw new IllegalCompressionTypeException(Msg.bind(
					Messages.CompressionTypeEx_EMPTY, compressionType));
		}
		for (CompressionType c : CompressionType.class.getEnumConstants()) {
			if (c.getValue().equalsIgnoreCase(compressionType)) {
				return c;
			}
		}
		throw new IllegalCompressionTypeException(Msg.bind(
				Messages.CompressionTypeEx_INVALID, compressionType,
				Arrays.asList(CompressionType.values())));
	}

	private final String _value;

	private CompressionType(String compressionType) {
		this._value = compressionType;
	}

	public String getValue() {
		return _value;
	}

}