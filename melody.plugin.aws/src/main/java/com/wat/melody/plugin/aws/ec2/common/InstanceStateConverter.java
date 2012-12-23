package com.wat.melody.plugin.aws.ec2.common;

import java.util.Arrays;

import com.wat.melody.cloud.InstanceState;
import com.wat.melody.cloud.exception.IllegalInstanceStateException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class InstanceStateConverter {

	/**
	 * <p>
	 * Convert the given <code>int</code> to a {@link InstanceState} object.
	 * </p>
	 * 
	 * @param type
	 *            is the given <code>int</code> to convert.
	 * 
	 * @return an {@link InstanceState} object, whose equal to the given input
	 *         <code>int</code>.
	 * 
	 * @throws IllegalInstanceStateException
	 *             if the given input <code>int</code> is not a valid
	 *             {@link InstanceState} Enumeration Constant.
	 */
	public static InstanceState parse(int iState)
			throws IllegalInstanceStateException {
		switch (iState) {
		case 0:
			return InstanceState.PENDING;
		case 16:
			return InstanceState.RUNNING;
		case 32:
			return InstanceState.SHUTTING_DOWN;
		case 48:
			return InstanceState.TERMINATED;
		case 64:
			return InstanceState.STOPPING;
		case 80:
			return InstanceState.STOPPED;
		}
		throw new IllegalInstanceStateException(Messages.bind(
				com.wat.melody.cloud.Messages.InstanceStateEx_INVALID, iState,
				Arrays.asList(new int[] { 0, 16, 32, 48, 64, 80 })));
	}

}
