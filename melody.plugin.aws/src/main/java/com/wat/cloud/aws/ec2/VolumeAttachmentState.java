package com.wat.cloud.aws.ec2;

import java.util.Arrays;

import com.wat.cloud.aws.ec2.exception.IllegalVolumeAttachmentStateException;

/**
 * Note that the DELETED state cannot be used. That's the reason why it is not
 * mentioned in this enumeration. Use {@link VolumeState#AVAILABLE} instead.
 * 
 * @author Guillaume Cornet
 * 
 */
public enum VolumeAttachmentState {

	ATTACHING("attaching"), ATTACHED("attached"), DETACHING("detaching");

	/**
	 * <p>
	 * Convert the given <tt>String</tt> to a {@link VolumeAttachmentState}
	 * object.
	 * </p>
	 * 
	 * @param sType
	 *            is the given <tt>String</tt> to convert.
	 * 
	 * @return an {@link VolumeAttachmentState} object, whose equal to the given
	 *         input <tt>String</tt>.
	 * 
	 * @throws IllegalVolumeAttachmentStateException
	 *             if the given input <tt>String</tt> is not a valid
	 *             {@link VolumeAttachmentState} Enumeration Constant.
	 * @throws IllegalArgumentException
	 *             if the given input <tt>String</tt> is <tt>null</tt>.
	 */
	public static VolumeAttachmentState parseString(String sType)
			throws IllegalVolumeAttachmentStateException {
		if (sType == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a "
					+ VolumeAttachmentState.class.getCanonicalName()
					+ " Enumeration Constant. Accepted values are "
					+ Arrays.asList(VolumeAttachmentState.values()) + ").");
		}
		if (sType.trim().length() == 0) {
			throw new IllegalVolumeAttachmentStateException(Messages.bind(
					Messages.VolumeAttachmentStateEx_EMPTY, sType));
		}
		for (VolumeAttachmentState c : VolumeAttachmentState.class
				.getEnumConstants()) {
			if (sType.equalsIgnoreCase(c.getValue())) {
				return c;
			}
		}
		throw new IllegalVolumeAttachmentStateException(Messages.bind(
				Messages.VolumeAttachmentStateEx_INVALID, sType,
				Arrays.asList(VolumeAttachmentState.values())));
	}

	private final String msValue;

	private VolumeAttachmentState(String v) {
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
