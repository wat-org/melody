package com.wat.melody.cloud.protectedarea;

import java.util.LinkedHashSet;

import com.wat.melody.cloud.protectedarea.exception.IllegalProtectedAreaNameException;
import com.wat.melody.cloud.protectedarea.exception.IllegalProtectedAreaNamesException;
import com.wat.melody.common.messages.Msg;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class ProtectedAreaNames extends LinkedHashSet<ProtectedAreaName> {

	private static final long serialVersionUID = -879656642352545322L;

	public static final String SEPARATOR = ",";

	/**
	 * <p>
	 * Convert the given <tt>String</tt> to a {@link ProtectedAreaNames} object.
	 * </p>
	 * 
	 * Input <tt>String</tt> must respect the following pattern :
	 * <tt>protected area name(','protected area name)*</tt>
	 * <ul>
	 * <li>Each <tt>protected area name</tt> must be a valid
	 * {@link ProtectedAreaName} (see
	 * {@link ProtectedAreaName#parseString(String)}) ;</li>
	 * </ul>
	 * 
	 * @param panames
	 *            is the given <tt>String</tt> to convert. Can be an empty
	 *            <tt>String</tt>.
	 * 
	 * @return a {@link ProtectedAreaNames} object, which is equal to the given
	 *         <tt>String</tt>.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given <tt>String</tt> is <tt>null</tt>.
	 * @throws IllegalProtectedAreaNamesException
	 *             if a <tt>protected area name</tt> is not a valid
	 *             {@link ProtectedAreaName}.
	 */
	public static ProtectedAreaNames parseString(String panames)
			throws IllegalProtectedAreaNamesException {
		return new ProtectedAreaNames(panames);
	}

	/**
	 * Build an empty Protected Area Name list.
	 */
	public ProtectedAreaNames() {
		super();
	}

	public ProtectedAreaNames(String panames)
			throws IllegalProtectedAreaNamesException {
		super();
		setProtectedAreaNames(panames);
	}

	public ProtectedAreaNames(ProtectedAreaName... panames) {
		super();
		setProtectedAreaNames(panames);
	}

	private void setProtectedAreaNames(ProtectedAreaName... panames) {
		clear();
		if (panames == null) {
			return;
		}
		for (ProtectedAreaName paname : panames) {
			if (paname == null) {
				continue;
			} else {
				add(paname);
			}
		}
	}

	private void setProtectedAreaNames(String panames)
			throws IllegalProtectedAreaNamesException {
		if (panames == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a "
					+ ProtectedAreaNames.class.getCanonicalName() + ").");
		}
		clear();
		for (String paname : panames.split(SEPARATOR)) {
			paname = paname.trim();
			if (paname.length() == 0) {
				throw new IllegalProtectedAreaNamesException(Msg.bind(
						Messages.ProtectedAreaNamesEx_EMPTY_NAME, panames));
			}
			try {
				add(ProtectedAreaName.parseString(paname));
			} catch (IllegalProtectedAreaNameException Ex) {
				throw new IllegalProtectedAreaNamesException(Msg.bind(
						Messages.ProtectedAreaNamesEx_INVALID_NAME, panames),
						Ex);
			}
		}
	}

}