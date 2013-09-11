package com.wat.cloud.aws.s3;

import com.wat.cloud.aws.s3.exception.IllegalBucketNameException;
import com.wat.melody.common.messages.Msg;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class BucketName {

	/**
	 * <p>
	 * Convert the given <tt>String</tt> to a {@link BucketName} object.
	 * </p>
	 * 
	 * <p>
	 * Note that the AWS SDK JavaDoc doesn't specifies that bucket name cannot
	 * contains dashes ('.'). But Some tests show that many stuff will not work
	 * correctly if the given <tt>String</tt> contains dashes (e.g.
	 * 'my.bucket.name'). For this reason, an exception will be raised if the
	 * given <tt>String</tt> contains dash.
	 * </p>
	 * 
	 * @param bucketName
	 *            is the given <tt>String</tt> to convert.
	 * 
	 * @return a {@link BucketName} object, which is equal to the given
	 *         <tt>String</tt>.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given <tt>String</tt> is <tt>null</tt>.
	 * @throws IllegalBucketNameException
	 *             <ul>
	 *             <li>if the given <tt>String</tt> is empty ;</li>
	 *             <li>if the given <tt>String</tt> length is < 3 and > 63 ;</li>
	 *             <li>if the given <tt>String</tt> doesn't match the pattern
	 *             {@link #PATTERN} ;</li>
	 *             </ul>
	 */
	public static BucketName parseString(String bucketName)
			throws IllegalBucketNameException {
		return new BucketName(bucketName);
	}

	/**
	 * The pattern an BucketName must satisfy.
	 */
	public static final String PATTERN = "[a-z0-9]+([-][a-z0-9]+)*";

	private String _value;

	public BucketName(String bucketName) throws IllegalBucketNameException {
		setValue(bucketName);
	}

	@Override
	public int hashCode() {
		return _value.hashCode();
	}

	@Override
	public String toString() {
		return _value;
	}

	@Override
	public boolean equals(Object anObject) {
		if (this == anObject) {
			return true;
		}
		if (anObject instanceof BucketName) {
			BucketName on = (BucketName) anObject;
			return getValue().equals(on.getValue());
		}
		return false;
	}

	public String getValue() {
		return _value;
	}

	private String setValue(String bucketName)
			throws IllegalBucketNameException {
		if (bucketName == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (an "
					+ BucketName.class.getCanonicalName() + ").");
		}
		if (bucketName.trim().length() == 0) {
			throw new IllegalBucketNameException(Msg.bind(
					Messages.BucketNameEx_EMPTY, bucketName));
		} else if (bucketName.length() < 3 || bucketName.length() > 63) {
			throw new IllegalBucketNameException(Msg.bind(
					Messages.BucketNameEx_INVALID_LENGTH, bucketName));
		} else if (!bucketName.matches("^" + PATTERN + "$")) {
			throw new IllegalBucketNameException(Msg.bind(
					Messages.BucketNameEx_INVALID, bucketName, PATTERN));
		}
		String previous = toString();
		_value = bucketName;
		return previous;
	}

}