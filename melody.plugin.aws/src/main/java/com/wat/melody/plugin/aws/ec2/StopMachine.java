package com.wat.melody.plugin.aws.ec2;

import com.wat.melody.cloud.instance.exception.OperationException;
import com.wat.melody.plugin.aws.ec2.common.AbstractOperation;
import com.wat.melody.plugin.aws.ec2.common.Messages;
import com.wat.melody.plugin.aws.ec2.common.exception.AwsException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class StopMachine extends AbstractOperation {

	/**
	 * The 'StopMachine' XML element
	 */
	public static final String STOP_MACHINE = "StopMachine";

	public StopMachine() {
		super();
	}

	@Override
	public void doProcessing() throws AwsException, InterruptedException {
		getContext().handleProcessorStateUpdates();

		try {
			getInstance().ensureInstanceIsStoped(getTimeout());
		} catch (OperationException Ex) {
			throw new AwsException(Messages.bind(Messages.StopEx_GENERIC_FAIL,
					getTargetNodeLocation()), Ex);
		}
	}

}