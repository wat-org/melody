package com.wat.melody.common.transfer;

import java.util.LinkedHashSet;

import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.transfer.exception.IllegalTransferBehaviorException;
import com.wat.melody.common.transfer.exception.IllegalTransferBehaviorsException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class TransferBehaviors extends LinkedHashSet<TransferBehavior> {

	private static final long serialVersionUID = -675433245478656392L;

	public static final String TRANSFER_BEHAVIORS_SEPARATOR = ",";

	public static TransferBehaviors DEFAULT = createTransferBehaviors(TransferBehavior.OVERWRITE_IF_SRC_NEWER);

	protected static TransferBehaviors createTransferBehaviors(
			TransferBehavior... transferBehaviors) {
		try {
			return new TransferBehaviors(transferBehaviors);
		} catch (IllegalTransferBehaviorsException Ex) {
			throw new RuntimeException("Unexpected error while initializing "
					+ "the TransferBehaviors with its default value. "
					+ "Because this default value initialization is "
					+ "hardcoded, such error cannot happened. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.", Ex);
		}
	}

	/**
	 * <p>
	 * Convert the given <tt>String</tt> to a {@link TransferBehaviors} object.
	 * </p>
	 * 
	 * Input <tt>String</tt> must respect the following pattern :
	 * <tt>transferBehavior(','transferBehavior)*</tt>
	 * <ul>
	 * <li><Each <tt>transferBehavior</tt> must be a valid
	 * {@link TransferBehavior} (see
	 * {@link TransferBehavior#parseString(String)}) ;</li>
	 * </ul>
	 * 
	 * @param transferBehaviors
	 *            is the given <tt>String</tt> to convert.
	 * 
	 * @return a {@link TransferBehaviors} object, which is equal to the given
	 *         <tt>String</tt>.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given <tt>String</tt> is <tt>null</tt>.
	 * @throws IllegalTransferBehaviorsException
	 *             <ul>
	 *             <li>if the given <tt>String</tt> is empty ;</li>
	 *             <li>if a <tt>transferBehavior</tt> is not a valid
	 *             {@link TransferBehavior} ;</li>
	 *             </ul>
	 */
	public static TransferBehaviors parseString(String transferBehaviors)
			throws IllegalTransferBehaviorsException {
		return new TransferBehaviors(transferBehaviors);
	}

	public TransferBehaviors(String transferBehaviors)
			throws IllegalTransferBehaviorsException {
		super();
		setTransferBehaviors(transferBehaviors);
	}

	public TransferBehaviors(TransferBehavior... transferBehaviors)
			throws IllegalTransferBehaviorsException {
		super();
		setTransferBehaviors(transferBehaviors);
	}

	private void setTransferBehaviors(TransferBehavior... transferBehaviors)
			throws IllegalTransferBehaviorsException {
		clear();
		if (transferBehaviors == null) {
			return;
		}
		for (TransferBehavior transferBehavior : transferBehaviors) {
			if (transferBehavior == null) {
				continue;
			} else {
				add(transferBehavior);
			}
		}
		if (size() == 0) {
			throw new IllegalTransferBehaviorsException(Msg.bind(
					Messages.TransferBehaviorsEx_EMPTY,
					(Object[]) transferBehaviors));
		}
	}

	private void setTransferBehaviors(String transferBehaviors)
			throws IllegalTransferBehaviorsException {
		if (transferBehaviors == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a "
					+ TransferBehaviors.class.getCanonicalName() + ").");
		}
		clear();
		for (String transferBehavior : transferBehaviors
				.split(TRANSFER_BEHAVIORS_SEPARATOR)) {
			transferBehavior = transferBehavior.trim();
			if (transferBehavior.length() == 0) {
				throw new IllegalTransferBehaviorsException(Msg.bind(
						Messages.TransferBehaviorsEx_EMPTY_TRANSFER_BEHAVIOR,
						transferBehaviors));
			}
			try {
				add(TransferBehavior.parseString(transferBehavior));
			} catch (IllegalTransferBehaviorException Ex) {
				throw new IllegalTransferBehaviorsException(Msg.bind(
						Messages.TransferBehaviorsEx_INVALID_TRANSFER_BEHAVIOR,
						transferBehaviors), Ex);
			}
		}
		if (size() == 0) {
			throw new IllegalTransferBehaviorsException(Msg.bind(
					Messages.TransferBehaviorsEx_EMPTY, transferBehaviors));
		}
	}

}