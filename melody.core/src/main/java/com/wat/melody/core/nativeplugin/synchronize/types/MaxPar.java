package com.wat.melody.core.nativeplugin.synchronize.types;

import com.wat.melody.common.messages.Msg;
import com.wat.melody.core.nativeplugin.synchronize.Messages;
import com.wat.melody.core.nativeplugin.synchronize.exception.IllegalMaxParException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class MaxPar {

	public static final MaxPar SEQUENTIAL = createMaxPar(1);
	public static final MaxPar UNLIMITED = createMaxPar(0);

	private static MaxPar createMaxPar(int lockId) {
		try {
			return MaxPar.parseInt(lockId);
		} catch (IllegalMaxParException Ex) {
			throw new RuntimeException("Unexecpted error while creating "
					+ "a default max parallelism degre. "
					+ "Since this default value is hard coded, "
					+ "such error cannot happened. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.", Ex);
		}
	}

	/**
	 * 
	 * @param iMaxPar
	 * 
	 * @return
	 * 
	 * @throws IllegalMaxParException
	 *             if input int is < 0.
	 */
	public static MaxPar parseInt(int iMaxPar) throws IllegalMaxParException {
		return new MaxPar(iMaxPar);
	}

	/**
	 * @param sMaxPar
	 * 
	 * @return
	 * 
	 * @throws IllegalMaxParException
	 *             if input string is < 0.
	 * @throws IllegalArgumentException
	 *             is input string is <tt>null</tt>.
	 */
	public static MaxPar parseString(String sMaxPar)
			throws IllegalMaxParException {
		return new MaxPar(sMaxPar);
	}

	private int _value;

	public MaxPar(int iMaxPar) throws IllegalMaxParException {
		setValue(iMaxPar);
	}

	public MaxPar(String sMaxPar) throws IllegalMaxParException {
		setValue(sMaxPar);
	}

	@Override
	public String toString() {
		return String.valueOf(_value);
	}

	@Override
	public boolean equals(Object anObject) {
		if (this == anObject) {
			return true;
		}
		if (anObject instanceof MaxPar) {
			MaxPar maxpar = (MaxPar) anObject;
			return getValue() == maxpar.getValue();
		}
		return false;
	}

	public int getValue() {
		return _value;
	}

	private int setValue(int iMaxPar) throws IllegalMaxParException {
		if (iMaxPar < 0) {
			throw new IllegalMaxParException(Msg.bind(
					Messages.MaxParEx_NEGATIVE, iMaxPar));
		}
		int previous = getValue();
		_value = iMaxPar;
		return previous;
	}

	private int setValue(String sMaxPar) throws IllegalMaxParException {
		if (sMaxPar == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a "
					+ MaxPar.class.getCanonicalName() + ").");
		}
		if (sMaxPar.trim().length() == 0) {
			throw new IllegalMaxParException(Msg.bind(Messages.MaxParEx_EMPTY,
					sMaxPar));
		}
		try {
			return setValue(Integer.parseInt(sMaxPar));
		} catch (NumberFormatException Ex) {
			throw new IllegalMaxParException(Msg.bind(
					Messages.MaxParEx_NOT_A_NUMBER, sMaxPar));
		}
	}

}