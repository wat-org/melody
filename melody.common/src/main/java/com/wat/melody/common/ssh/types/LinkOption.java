package com.wat.melody.common.ssh.types;

import java.util.Arrays;

import com.wat.melody.common.ssh.types.exception.IllegalLinkOptionException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public enum LinkOption {

	/*
	 * keep-links keep symlinks as symlinks
	 * 
	 * copy-links copy symlinks into referent file/dir
	 * 
	 * copy-unsafe-links only "unsafe" symlinks are transformed
	 */
	KEEP_LINKS("keep_links"), COPY_LINKS("copy_links"), COPY_UNSAFE_LINKS(
			"copy_unsafe_links");

	/**
	 * <p>
	 * Convert the given <code>String</code> to an {@link LinkOption} object.
	 * </p>
	 * 
	 * @param sLinkOptions
	 *            is the given <code>String</code> to convert.
	 * 
	 * @return a {@link LinkOption} object, whose equal to the given input
	 *         <code>String</code>.
	 * 
	 * @throws IllegalLinkOptionException
	 *             if the given input <code>String</code> is not a valid
	 *             {@link LinkOption} Enumeration Constant.
	 * @throws IllegalArgumentException
	 *             if the given input <code>String</code> is <code>null</code>.
	 */
	public static LinkOption parseString(String sLinkOptions)
			throws IllegalLinkOptionException {
		if (sLinkOptions == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (an "
					+ LinkOption.class.getCanonicalName()
					+ " Enumeration Constant. Accepted values are "
					+ Arrays.asList(LinkOption.values()) + " ).");
		}
		if (sLinkOptions.trim().length() == 0) {
			throw new IllegalLinkOptionException(Messages.bind(
					Messages.LinkOptionEx_EMPTY, sLinkOptions));
		}
		for (LinkOption c : LinkOption.class.getEnumConstants()) {
			if (c.getValue().equalsIgnoreCase(sLinkOptions)) {
				return c;
			}
		}
		throw new IllegalLinkOptionException(Messages.bind(
				Messages.LinkOptionEx_INVALID, sLinkOptions,
				Arrays.asList(LinkOption.values())));
	}

	private final String msValue;

	private LinkOption(String sLinkOptions) {
		this.msValue = sLinkOptions;
	}

	public String getValue() {
		return msValue;
	}

}
