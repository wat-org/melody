package com.wat.melody.cloud.protectedarea;

import com.wat.melody.cloud.protectedarea.exception.IllegalProtectedAreaDatasException;

/**
 * <P>
 * A call-back, called at the end of an {@link ProtectedAreaDatas} object
 * construction. Validate and transform, in an implementation specific way, the
 * new {@link ProtectedAreaDatas} object.
 * 
 * @author Guillaume Cornet
 * 
 */
public interface ProtectedAreaDatasValidator {

	/**
	 * @param datas
	 *            is an {@link ProtectedAreaDatas}, which will be validated.
	 * 
	 * @throws IllegalProtectedAreaDatasException
	 *             if the given {@link ProtectedAreaDatas} is not valid.
	 */
	public void validateAndTransform(ProtectedAreaDatas datas)
			throws IllegalProtectedAreaDatasException;

}