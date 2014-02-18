package com.wat.cloud.aws.s3;

import java.util.Arrays;

import com.wat.cloud.aws.s3.exception.IllegalStorageModeException;
import com.wat.melody.common.messages.Msg;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public enum StorageMode {

	FILE("file"), METADATA("metadata");

	/**
	 * <p>
	 * Convert the given <tt>String</tt> to a {@link StorageMode} object.
	 * </p>
	 * 
	 * @param storageMode
	 *            is the given <tt>String</tt> to convert.
	 * 
	 * @return a {@link StorageMode} object, which is equal to the given
	 *         <tt>String</tt>.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given <tt>String</tt> is <tt>null</tt>.
	 * @throws IllegalStorageModeException
	 *             <ul>
	 *             <li>if the given <tt>String</tt> is not a valid
	 *             {@link StorageMode} Enumeration Constant ;</li>
	 *             <li>if the given <tt>String</tt> is empty ;</li>
	 *             </ul>
	 */
	public static StorageMode parseString(String storageMode)
			throws IllegalStorageModeException {
		if (storageMode == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a "
					+ StorageMode.class.getCanonicalName()
					+ " Enumeration Constant. Accepted values are "
					+ Arrays.asList(StorageMode.values()) + ").");
		}
		if (storageMode.trim().length() == 0) {
			throw new IllegalStorageModeException(Msg.bind(
					Messages.StorageModeEx_EMPTY, storageMode));
		}
		for (StorageMode c : StorageMode.class.getEnumConstants()) {
			if (c.getValue().equalsIgnoreCase(storageMode.trim())) {
				return c;
			}
		}
		throw new IllegalStorageModeException(Msg.bind(
				Messages.StorageModeEx_INVALID, storageMode,
				Arrays.asList(StorageMode.values())));
	}

	private final String _value;

	private StorageMode(String storageMode) {
		this._value = storageMode;
	}

	public String getValue() {
		return _value;
	}

}