package com.wat.melody.common.systool;

import java.util.UUID;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class SysTool {

	/**
	 * Is equal to System.getProperty("user.dir")
	 */
	public static final String CWD = System.getProperty("user.dir");

	/**
	 * Is equal to System.getProperty("line.separator")
	 */
	public static final String NEW_LINE = System.getProperty("line.separator");

	/**
	 * Is equal to System.getProperty("file.separator")
	 */
	public static final String FILE_SEPARATOR = System
			.getProperty("file.separator");

	/**
	 * Is equal to System.getProperty("java.io.tmpdir")
	 */
	public static final String SYSTEM_TEMP_DIR = System
			.getProperty("java.io.tmpdir");

	public static UUID newUUID() {
		return java.util.UUID.randomUUID();
	}

	/**
	 * <p>
	 * A convenient method to replace all occurrence of a given sequence in a
	 * {@link StringBuilder}.
	 * </p>
	 * 
	 * @param builder
	 *            is the subject.
	 * @param from
	 *            is the sequence to replace.
	 * @param to
	 *            is the sequence used for replacement.
	 * 
	 * @return the given {@link StringBuilder}, where all occurrence of 'from'
	 *         have been replaced by 'to'.
	 */
	public static StringBuilder replaceAll(StringBuilder builder, String from,
			String to) {
		int index = builder.indexOf(from);
		while (index != -1) {
			builder.replace(index, index + from.length(), to);
			index += to.length(); // Move to the end of the replacement
			index = builder.indexOf(from, index);
		}
		return builder;
	}

}
