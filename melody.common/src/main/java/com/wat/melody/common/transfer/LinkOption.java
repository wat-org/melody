package com.wat.melody.common.transfer;

import java.util.Arrays;

import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.transfer.exception.IllegalLinkOptionException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public enum LinkOption {

	/*
	 * keep-links : will keep the symbolic link as a symbolic link ;
	 * 
	 * copy-links : will copy the symbolic link's target into the corresponding
	 * file or a directory. If the symbolic link's target is invalid (ex: target
	 * doesn't exist), it will be skipped ;
	 * 
	 * copy-unsafe-links : will copy the symbolic link's target into the
	 * corresponding file or a directory if the symbolic link is "unsafe". If
	 * the symbolic link's target is invalid (ex: target doesn't exist), it will
	 * be skipped ;
	 * 
	 * skip-links : will skip the symbolic link ;
	 */
	KEEP_LINKS("keep_links"), COPY_LINKS("copy_links"), COPY_UNSAFE_LINKS(
			"copy_unsafe_links"), SKIP_LINKS("skip_links");

	/**
	 * <p>
	 * Convert the given <tt>String</tt> to a {@link LinkOption} object.
	 * </p>
	 * 
	 * @param linkOptions
	 *            is the given <tt>String</tt> to convert.
	 * 
	 * @return a {@link LinkOption} object, which is equal to the given
	 *         <tt>String</tt>.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given <tt>String</tt> is <tt>null</tt>.
	 * @throws IllegalLinkOptionException
	 *             <ul>
	 *             <li>if the given <tt>String</tt> is empty :</li>
	 *             <li>if the given <tt>String</tt> is not not the
	 *             {@link LinkOption} Enumeration Constant ;</li>
	 *             </ul>
	 */
	public static LinkOption parseString(String linkOptions)
			throws IllegalLinkOptionException {
		if (linkOptions == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a "
					+ LinkOption.class.getCanonicalName()
					+ " Enumeration Constant. Accepted values are "
					+ Arrays.asList(LinkOption.values()) + " ).");
		}
		if (linkOptions.trim().length() == 0) {
			throw new IllegalLinkOptionException(Msg.bind(
					Messages.LinkOptionEx_EMPTY, linkOptions));
		}
		for (LinkOption c : LinkOption.class.getEnumConstants()) {
			if (c.getValue().equalsIgnoreCase(linkOptions)) {
				return c;
			}
		}
		throw new IllegalLinkOptionException(Msg.bind(
				Messages.LinkOptionEx_INVALID, linkOptions,
				Arrays.asList(LinkOption.values())));
	}

	private final String _value;

	private LinkOption(String linkOptions) {
		this._value = linkOptions;
	}

	public String getValue() {
		return _value;
	}

}