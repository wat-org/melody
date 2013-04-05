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
public class StartMachine extends AbstractOperation {

	/**
	 * The 'StartMachine' XML element
	 */
	public static final String START_MACHINE = "StartMachine";

	public StartMachine() {
		super();
	}

	@Override
	public void doProcessing() throws AwsException, InterruptedException {
		getContext().handleProcessorStateUpdates();

		try {
			getInstance().ensureInstanceIsStarted(getTimeout());
		} catch (OperationException Ex) {
			throw new AwsException(Messages.bind(Messages.StartEx_GENERIC_FAIL,
					getTargetNodeLocation()), Ex);
		}
	}

}
