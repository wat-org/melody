package com.wat.melody.cloud.instance;

import java.util.Arrays;

import com.wat.melody.cloud.instance.exception.IllegalInstanceTypeException;

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
	 * @param sType
	 *            is the given <tt>String</tt> to convert.
	 * 
	 * @return an <code>InstanceType</code> object, whose equal to the given
	 *         input <tt>String</tt>.
	 * 
	 * @throws IllegalInstanceTypeException
	 *             if the given input <tt>String</tt> is not a valid
	 *             {@link InstanceType} Enumeration Constant.
	 * @throws IllegalArgumentException
	 *             if the given input <tt>String</tt> is <tt>null</tt>.
	 */
	public static InstanceType parseString(String sType)
			throws IllegalInstanceTypeException {
		if (sType == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a "
					+ InstanceType.class.getCanonicalName()
					+ " Enumeration Constant. Accepted values are "
					+ Arrays.asList(InstanceType.values()) + ").");
		}
		if (sType.trim().length() == 0) {
			throw new IllegalInstanceTypeException(Messages.bind(
					Messages.InstanceTypeEx_EMPTY, sType));
		}
		for (InstanceType c : InstanceType.class.getEnumConstants()) {
			if (sType.equalsIgnoreCase(c.getValue())) {
				return c;
			}
		}
		throw new IllegalInstanceTypeException(Messages.bind(
				Messages.InstanceTypeEx_INVALID, sType,
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
