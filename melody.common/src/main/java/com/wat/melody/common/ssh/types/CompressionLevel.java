package com.wat.melody.common.ssh.types;

import java.util.Arrays;

import com.wat.melody.common.ssh.Messages;
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
	 * Convert the given <code>String</code> to a {@link CompressionLevel}
	 * object.
	 * </p>
	 * 
	 * @param sCompressionLevel
	 *            is the given <code>String</code> to convert.
	 * 
	 * @return a <code>CompressionLevel</code> object, whose equal to the given
	 *         input <code>String</code>.
	 * 
	 * @throws IllegalCompressionLevelException
	 *             if the given input <code>String</code> is not a valid
	 *             <code>CompressionLevel</code> Enumeration Constant.
	 * @throws IllegalArgumentException
	 *             if the given input <code>String</code> is <code>null</code>.
	 */
	public static CompressionLevel parseString(String sCompressionLevel)
			throws IllegalCompressionLevelException {
		if (sCompressionLevel == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (an "
					+ CompressionLevel.class.getCanonicalName()
					+ " Enumeration Constant. Accepted values are "
					+ Arrays.asList(CompressionLevel.values()) + " ).");
		}
		if (sCompressionLevel.trim().length() == 0) {
			throw new IllegalCompressionLevelException(Messages.bind(
					Messages.CompressionLevelEx_EMPTY, sCompressionLevel));
		}
		for (CompressionLevel c : CompressionLevel.class.getEnumConstants()) {
			if (c.getValue().equalsIgnoreCase(sCompressionLevel)) {
				return c;
			}
		}
		throw new IllegalCompressionLevelException(Messages.bind(
				Messages.CompressionLevelEx_INVALID, sCompressionLevel,
				Arrays.asList(CompressionLevel.values())));
	}

	private final String msValue;

	private CompressionLevel(String sCompressionLevel) {
		this.msValue = sCompressionLevel;
	}

	public String getValue() {
		return msValue;
	}

}
