package com.wat.melody.plugin.aws.ec2.common;

import java.util.Arrays;

import com.wat.melody.plugin.aws.ec2.common.exception.IllegalInstanceStateException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public enum InstanceState {

	PENDING(0), RUNNING(16), SHUTTING_DOWN(32), TERMINATED(48), STOPPING(64), STOPPED(
			80);

	/**
	 * <p>
	 * Convert the given <code>String</code> to a {@link InstanceState} object.
	 * </p>
	 * 
	 * @param type
	 *            is the given <code>String</code> to convert.
	 * 
	 * @return an <code>InstanceState</code> object, whose equal to the given
	 *         input <code>String</code>.
	 * 
	 * @throws IllegalInstanceStateException
	 *             if the given input <code>String</code> is not a valid
	 *             <code>InstanceState</code> Enumeration Constant.
	 * @throws IllegalArgumentException
	 *             if the given input <code>String</code> is <code>null</code>.
	 */
	public static InstanceState parseString(String sState)
			throws IllegalInstanceStateException {
		if (sState == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a "
					+ InstanceState.class.getCanonicalName()
					+ " Enumeration Constant. Accepted values are "
					+ Arrays.asList(InstanceState.values()) + ").");
		}
		if (sState.trim().length() == 0) {
			throw new IllegalInstanceStateException(Messages.bind(
					Messages.InstanceStateEx_EMPTY, sState));
		}
		for (InstanceState c : InstanceState.class.getEnumConstants()) {
			if (sState.equalsIgnoreCase(c.toString())) {
				return c;
			}
		}
		throw new IllegalInstanceStateException(Messages.bind(
				Messages.InstanceStateEx_INVALID, sState,
				Arrays.asList(InstanceState.values())));
	}

	/**
	 * <p>
	 * Convert the given <code>String</code> to a {@link InstanceState} object.
	 * </p>
	 * 
	 * @param type
	 *            is the given <code>String</code> to convert.
	 * 
	 * @return an <code>InstanceState</code> object, whose equal to the given
	 *         input <code>String</code>.
	 * 
	 * @throws IllegalInstanceStateException
	 *             if the given input <code>String</code> is not a valid
	 *             <code>InstanceState</code> Enumeration Constant.
	 * @throws IllegalArgumentException
	 *             if the given input <code>String</code> is <code>null</code>.
	 */
	public static InstanceState parseInt(String sState)
			throws IllegalInstanceStateException {
		if (sState == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a "
					+ InstanceState.class.getCanonicalName()
					+ " Enumeration Constant. Accepted values are "
					+ InstanceState.values() + ").");
		}
		if (sState.trim().length() == 0) {
			throw new IllegalInstanceStateException(Messages.bind(
					Messages.InstanceStateEx_EMPTY, sState));
		}
		for (InstanceState c : InstanceState.class.getEnumConstants()) {
			if (sState.equalsIgnoreCase(String.valueOf(c.getState()))) {
				return c;
			}
		}
		throw new IllegalInstanceStateException(Messages.bind(
				Messages.InstanceStateEx_INVALID, sState,
				InstanceState.class.getCanonicalName()));
	}

	/**
	 * <p>
	 * Convert the given <code>int</code> to a {@link InstanceState} object.
	 * </p>
	 * 
	 * @param type
	 *            is the given <code>int</code> to convert.
	 * 
	 * @return an <code>InstanceState</code> object, whose equal to the given
	 *         input <code>int</code>.
	 * 
	 * @throws IllegalInstanceStateException
	 *             if the given input <code>int</code> is not a valid
	 *             <code>InstanceState</code> Enumeration Constant.
	 */
	public static InstanceState parseInt(int iState)
			throws IllegalInstanceStateException {
		for (InstanceState c : InstanceState.class.getEnumConstants()) {
			if (c.getState() == iState) {
				return c;
			}
		}
		throw new IllegalInstanceStateException(Messages.bind(
				Messages.InstanceStateEx_INVALID, iState,
				InstanceState.class.getCanonicalName()));
	}

	private final int miState;

	private InstanceState(int v) {
		this.miState = v;
	}

	private int getState() {
		return this.miState;
	}

}
