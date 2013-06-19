package com.wat.melody.common.ssh.types;

import java.util.Arrays;

import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.ssh.types.exception.IllegalCompressionLevelException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public enum CompressionLevel {

	NONE("0"), BEST_SPEED("1"), TWO("2"), THREE("3"), FOUR("4"), FIVE("5"), BEST_COMPRESSION(
			"6");

	/**
	 * <p>
	 * Convert the given <tt>String</tt> to a {@link CompressionLevel} object.
	 * </p>
	 * 
	 * @param compressionLevel
	 *            is the given <tt>String</tt> to convert.
	 * 
	 * @return a {@link CompressionLevel} object, which is equal to the given
	 *         <tt>String</tt>.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given input <tt>String</tt> is <tt>null</tt>.
	 * @throws IllegalCompressionLevelException
	 *             <ul>
	 *             <li>if the given <tt>String</tt> is empty :</li>
	 *             <li>if the given <tt>String</tt> is not not the
	 *             {@link CompressionLevel} Enumeration Constant ;</li>
	 *             </ul>
	 */
	public static CompressionLevel parseString(String compressionLevel)
			throws IllegalCompressionLevelException {
		if (compressionLevel == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a "
					+ CompressionLevel.class.getCanonicalName()
					+ " Enumeration Constant. Accepted values are "
					+ Arrays.asList(CompressionLevel.values()) + " ).");
		}
		if (compressionLevel.trim().length() == 0) {
			throw new IllegalCompressionLevelException(Msg.bind(
					Messages.CompressionLevelEx_EMPTY, compressionLevel));
		}
		for (CompressionLevel c : CompressionLevel.class.getEnumConstants()) {
			if (c.getValue().equalsIgnoreCase(compressionLevel)) {
				return c;
			}
		}
		throw new IllegalCompressionLevelException(Msg.bind(
				Messages.CompressionLevelEx_INVALID, compressionLevel,
				Arrays.asList(CompressionLevel.values())));
	}

	private final String _value;

	private CompressionLevel(String compressionLevel) {
		this._value = compressionLevel;
	}

	public String getValue() {
		return _value;
	}

}