package com.wat.melody.cloud.instance;

import java.util.Arrays;

import com.wat.melody.cloud.instance.exception.IllegalInstanceTypeException;
import com.wat.melody.common.messages.Msg;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public enum InstanceType {

	T1Micro("t1.micro"), M1Small("m1.small"), C1Medium("c1.medium"), M1Medium(
			"m1.medium"), M1Large("m1.large"), M1Xlarge("m1.xlarge"), M2Xlarge(
			"m2.xlarge"), M22xlarge("m2.2xlarge"), M24xlarge("m2.4xlarge"), C1Xlarge(
			"c1.xlarge");

	/**
	 * <p>
	 * Convert the given <tt>String</tt> to a {@link InstanceType} object.
	 * </p>
	 * 
	 * @param type
	 *            is the given <tt>String</tt> to convert.
	 * 
	 * @return an {@link InstanceType} object, which is equal to the given
	 *         <tt>String</tt>.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given <tt>String</tt> is <tt>null</tt>.
	 * @throws IllegalInstanceTypeException
	 *             <ul>
	 *             <li>if the given <tt>String</tt> is empty ;</li>
	 *             <li>if the given <tt>String</tt> is not one of the
	 *             {@link InstanceType} Enumeration Constant ;</li>
	 *             </ul>
	 */
	public static InstanceType parseString(String type)
			throws IllegalInstanceTypeException {
		if (type == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a "
					+ InstanceType.class.getCanonicalName()
					+ " Enumeration Constant. Accepted values are "
					+ Arrays.asList(InstanceType.values()) + ").");
		}
		if (type.trim().length() == 0) {
			throw new IllegalInstanceTypeException(Msg.bind(
					Messages.InstanceTypeEx_EMPTY, type));
		}
		for (InstanceType c : InstanceType.class.getEnumConstants()) {
			if (type.equalsIgnoreCase(c.getValue())) {
				return c;
			}
		}
		throw new IllegalInstanceTypeException(Msg.bind(
				Messages.InstanceTypeEx_INVALID, type,
				Arrays.asList(InstanceType.values())));
	}

	private final String _value;

	private InstanceType(String v) {
		this._value = v;
	}

	@Override
	public String toString() {
		return _value;
	}

	private String getValue() {
		return _value;
	}

}