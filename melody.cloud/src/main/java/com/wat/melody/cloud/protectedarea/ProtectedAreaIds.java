package com.wat.melody.cloud.protectedarea;

import java.util.LinkedHashSet;

import com.wat.melody.cloud.protectedarea.exception.IllegalProtectedAreaIdException;
import com.wat.melody.cloud.protectedarea.exception.IllegalProtectedAreaIdsException;
import com.wat.melody.common.messages.Msg;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class ProtectedAreaIds extends LinkedHashSet<ProtectedAreaId> {

	private static final long serialVersionUID = -879656642352545322L;

	public static final String PROTECTED_AREA_ID_SEPARATOR = ",";

	/**
	 * <p>
	 * Convert the given <tt>String</tt> to a {@link ProtectedAreaIds} object.
	 * </p>
	 * 
	 * Input <tt>String</tt> must respect the following pattern :
	 * <tt>protected area identifier(','protected area identifier)*</tt>
	 * <ul>
	 * <li>Each <tt>protected area identifier</tt> must be a valid
	 * {@link ProtectedAreaId} (see {@link ProtectedAreaId#parseString(String)})
	 * ;</li>
	 * </ul>
	 * 
	 * @param paids
	 *            is the given <tt>String</tt> to convert. Can be an empty
	 *            <tt>String</tt>.
	 * 
	 * @return a {@link ProtectedAreaIds} object, which is equal to the given
	 *         <tt>String</tt>.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given <tt>String</tt> is <tt>null</tt>.
	 * @throws IllegalProtectedAreaIdsException
	 *             if a <tt>protected area name</tt> is not a valid
	 *             {@link ProtectedAreaName}.
	 */
	public static ProtectedAreaIds parseString(String paids)
			throws IllegalProtectedAreaIdsException {
		return new ProtectedAreaIds(paids);
	}

	/**
	 * Build an empty Protected Area Ids list.
	 */
	public ProtectedAreaIds() {
		super();
	}

	public ProtectedAreaIds(String paids)
			throws IllegalProtectedAreaIdsException {
		super();
		setProtectedAreaIds(paids);
	}

	public ProtectedAreaIds(ProtectedAreaId... paids)
			throws IllegalProtectedAreaIdsException {
		super();
		setProtectedAreaIds(paids);
	}

	private void setProtectedAreaIds(ProtectedAreaId... paids)
			throws IllegalProtectedAreaIdsException {
		clear();
		if (paids == null) {
			return;
		}
		for (ProtectedAreaId paid : paids) {
			if (paid == null) {
				continue;
			} else {
				add(paid);
			}
		}
	}

	private void setProtectedAreaIds(String paids)
			throws IllegalProtectedAreaIdsException {
		if (paids == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a "
					+ ProtectedAreaIds.class.getCanonicalName() + ").");
		}
		clear();
		for (String paid : paids.split(PROTECTED_AREA_ID_SEPARATOR)) {
			paid = paid.trim();
			if (paid.length() == 0) {
				throw new IllegalProtectedAreaIdsException(Msg.bind(
						Messages.ProtectedAreaIdsEx_EMPTY_NAME, paids));
			}
			try {
				add(ProtectedAreaId.parseString(paid));
			} catch (IllegalProtectedAreaIdException Ex) {
				throw new IllegalProtectedAreaIdsException(Msg.bind(
						Messages.ProtectedAreaIdsEx_INVALID_NAME, paids), Ex);
			}
		}
	}

}