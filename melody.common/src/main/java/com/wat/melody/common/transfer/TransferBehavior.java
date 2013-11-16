package com.wat.melody.common.transfer;

import java.util.Arrays;

import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.transfer.exception.IllegalTransferBehaviorException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public enum TransferBehavior {

	FAIL_IF_DIFFRENT_TYPE("fail-if-different-type"), FORCE_OVERWRITE(
			"force-overwrite"), OVERWRITE_IF_SRC_NEWER(
			"overwrite-if-source-is-newer");

	/**
	 * <p>
	 * Convert the given <tt>String</tt> to a {@link TransferBehavior} object.
	 * </p>
	 * 
	 * @param transferBehavior
	 *            is the given <tt>String</tt> to convert.
	 * 
	 * @return a {@link TransferBehavior} object, which is equal to the given
	 *         <tt>String</tt>.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given input <tt>String</tt> is <tt>null</tt>.
	 * @throws IllegalTransferBehaviorException
	 *             <ul>
	 *             <li>if the given <tt>String</tt> is empty :</li>
	 *             <li>if the given <tt>String</tt> is not not the
	 *             {@link TransferBehavior} Enumeration Constant ;</li>
	 *             </ul>
	 */
	public static TransferBehavior parseString(String transferBehavior)
			throws IllegalTransferBehaviorException {
		if (transferBehavior == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a "
					+ TransferBehavior.class.getCanonicalName()
					+ " Enumeration Constant. Accepted values are "
					+ Arrays.asList(TransferBehavior.values()) + " ).");
		}
		if (transferBehavior.trim().length() == 0) {
			throw new IllegalTransferBehaviorException(Msg.bind(
					Messages.TransferBehaviorEx_EMPTY, transferBehavior));
		}
		for (TransferBehavior c : TransferBehavior.class.getEnumConstants()) {
			if (c.getValue().equalsIgnoreCase(transferBehavior)) {
				return c;
			}
		}
		throw new IllegalTransferBehaviorException(Msg.bind(
				Messages.TransferBehaviorEx_INVALID, transferBehavior,
				Arrays.asList(TransferBehavior.values())));
	}

	private final String _value;

	private TransferBehavior(String transferBehavior) {
		this._value = transferBehavior;
	}

	public String getValue() {
		return _value;
	}

}