package com.wat.melody.plugin.aws.ec2.common;

import java.util.Arrays;

import com.wat.melody.plugin.aws.ec2.common.exception.IllegalVolumeStateException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public enum VolumeState {

	CREATING("creating"), AVAILABLE("available"), IN_USE("in-use"), DELETING(
			"deleting"), ERROR("error");

	/**
	 * <p>
	 * Convert the given <code>String</code> to a {@link VolumeState} object.
	 * </p>
	 * 
	 * @param sType
	 *            is the given <code>String</code> to convert.
	 * 
	 * @return an <code>VolumeState</code> object, whose equal to the given
	 *         input <code>String</code>.
	 * 
	 * @throws IllegalVolumeStateException
	 *             if the given input <code>String</code> is not a valid
	 *             <code>VolumeState</code> Enumeration Constant.
	 * @throws IllegalArgumentException
	 *             if the given input <code>String</code> is <code>null</code>.
	 */
	public static VolumeState parseString(String sType)
			throws IllegalVolumeStateException {
		if (sType == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a "
					+ VolumeState.class.getCanonicalName()
					+ " Enumeration Constant. Accepted values are "
					+ Arrays.asList(VolumeState.values()) + ").");
		}
		if (sType.trim().length() == 0) {
			throw new IllegalVolumeStateException(Messages.bind(
					Messages.VolumeStateEx_EMPTY, sType));
		}
		for (VolumeState c : VolumeState.class.getEnumConstants()) {
			if (sType.equalsIgnoreCase(c.getValue())) {
				return c;
			}
		}
		throw new IllegalVolumeStateException(Messages.bind(
				Messages.VolumeStateEx_INVALID, sType,
				Arrays.asList(VolumeState.values())));
	}

	private final String msValue;

	private VolumeState(String v) {
		this.msValue = v;
	}

	@Override
	public String toString() {
		return msValue;
	}

	private String getValue() {
		return msValue;
	}

}