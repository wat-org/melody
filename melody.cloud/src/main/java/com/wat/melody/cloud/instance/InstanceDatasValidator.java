package com.wat.melody.cloud.instance;

import com.wat.melody.cloud.instance.exception.IllegalInstanceDatasException;

/**
 * <P>
 * A call-back, called at the end of an {@link InstanceDatas} object
 * construction. Validate and transform, in an implementation specific way, the
 * new {@link InstanceDatas} object.
 * 
 * @author Guillaume Cornet
 * 
 */
public interface InstanceDatasValidator {

	/**
	 * @param datas
	 *            is an {@link InstanceDatas}, which will be validated.
	 * 
	 * @throws IllegalInstanceDatasException
	 *             if the given {@link InstanceDatas} is not valid.
	 */
	public void validateAndTransform(InstanceDatas datas)
			throws IllegalInstanceDatasException;

}