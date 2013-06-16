package com.wat.cloud.aws.ec2;

import java.util.Arrays;

import com.wat.cloud.aws.ec2.exception.IllegalVolumeStateException;
import com.wat.melody.common.messages.Msg;

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
	 * Convert the given <tt>String</tt> to a {@link VolumeState} object.
	 * </p>
	 * 
	 * @param type
	 *            is the given <tt>String</tt> to convert.
	 * 
	 * @return a {@link VolumeState} object, whose equal to the given
	 *         <tt>String</tt>.
	 * 
	 * @throws IllegalVolumeStateException
	 *             if the given <tt>String</tt> is not a valid
	 *             {@link VolumeState} Enumeration Constant.
	 * @throws IllegalArgumentException
	 *             if the given <tt>String</tt> is <tt>null</tt>.
	 */
	public static VolumeState parseString(String type)
			throws IllegalVolumeStateException {
		if (type == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a "
					+ VolumeState.class.getCanonicalName()
					+ " Enumeration Constant. Accepted values are "
					+ Arrays.asList(VolumeState.values()) + ").");
		}
		if (type.trim().length() == 0) {
			throw new IllegalVolumeStateException(Msg.bind(
					Messages.VolumeStateEx_EMPTY, type));
		}
		for (VolumeState c : VolumeState.class.getEnumConstants()) {
			if (type.equalsIgnoreCase(c.getValue())) {
				return c;
			}
		}
		throw new IllegalVolumeStateException(Msg.bind(
				Messages.VolumeStateEx_INVALID, type,
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