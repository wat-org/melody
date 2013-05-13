package com.wat.cloud.aws.ec2;

import java.util.Arrays;

import com.wat.melody.cloud.instance.InstanceState;
import com.wat.melody.cloud.instance.Messages;
import com.wat.melody.cloud.instance.exception.IllegalInstanceStateException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class InstanceStateConverter {

	/**
	 * <p>
	 * Convert the given <tt>int</tt> to an {@link InstanceState} object.
	 * </p>
	 * 
	 * @param type
	 *            is the given <tt>int</tt> to convert.
	 * 
	 * @return an {@link InstanceState} object, whose equal to the given input
	 *         <tt>int</tt>.
	 * 
	 * @throws IllegalInstanceStateException
	 *             if the given input <tt>int</tt> is not a valid
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
				Messages.InstanceStateEx_INVALID, iState,
				Arrays.asList(new int[] { 0, 16, 32, 48, 64, 80 })));
	}

}
