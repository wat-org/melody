package com.wat.melody.core.nativeplugin.synchronize.types;

import com.wat.melody.common.messages.Msg;
import com.wat.melody.core.nativeplugin.synchronize.Messages;
import com.wat.melody.core.nativeplugin.synchronize.exception.IllegalLockIdException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class LockId {

	public static final LockId DEFAULT_LOCK_ID = createLock("defaultLock");

	private static LockId createLock(String lockId) {
		try {
			return LockId.parseString(lockId);
		} catch (IllegalLockIdException Ex) {
			throw new RuntimeException("Unexecpted error while creating "
					+ "a lock id. "
					+ "Since this default value is hard coded, "
					+ "such error cannot happened. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.", Ex);
		}
	}

	public static final String PATTERN = "[a-zA-Z0-9>-]+([.][a-zA-Z0-9>-]+)*";

	/**
	 * <p>
	 * Convert the given <code>String</code> to a {@link LockId} object.
	 * </p>
	 * 
	 * @param sLockId
	 *            is the given <code>String</code> to convert.
	 * 
	 * @return a {@link LockId} object, whose equal to the given input
	 *         <code>String</code>.
	 * 
	 * 
	 * @throws IllegalLockIdException
	 *             if the given input <code>String</code> is not a valid
	 *             {@link LockId}.
	 * @throws IllegalArgumentException
	 *             if the given input <code>String</code> is <code>null</code>.
	 */
	public static LockId parseString(String sLockId)
			throws IllegalLockIdException {
		return new LockId(sLockId);
	}

	private String msValue;

	public LockId(String sLockId) throws IllegalLockIdException {
		setValue(sLockId);
	}

	@Override
	public String toString() {
		return msValue;
	}

	@Override
	public boolean equals(Object anObject) {
		if (this == anObject) {
			return true;
		}
		if (anObject instanceof LockId) {
			LockId lockId = (LockId) anObject;
			return getValue().equals(lockId.getValue());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return getValue().hashCode();
	}

	public String getValue() {
		return msValue;
	}

	public String setValue(String sLockId) throws IllegalLockIdException {
		if (sLockId == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (an "
					+ LockId.class.getCanonicalName() + ").");
		}
		if (sLockId.trim().length() == 0) {
			throw new IllegalLockIdException(Msg.bind(Messages.LockIdEx_EMPTY,
					sLockId));
		} else if (!sLockId.matches("^" + PATTERN + "$")) {
			throw new IllegalLockIdException(Msg.bind(
					Messages.LockIdEx_INVALID, sLockId, PATTERN));
		}
		String previous = getValue();
		msValue = sLockId;
		return previous;
	}

}