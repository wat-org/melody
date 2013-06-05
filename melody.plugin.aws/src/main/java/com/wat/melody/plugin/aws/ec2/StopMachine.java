package com.wat.melody.plugin.aws.ec2;

import com.wat.melody.api.Melody;
import com.wat.melody.api.annotation.Task;
import com.wat.melody.cloud.instance.exception.OperationException;
import com.wat.melody.plugin.aws.ec2.common.AbstractOperation;
import com.wat.melody.plugin.aws.ec2.common.Messages;
import com.wat.melody.plugin.aws.ec2.common.exception.AwsException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
@Task(name = StopMachine.STOP_MACHINE)
public class StopMachine extends AbstractOperation {

	/**
	 * Task's name
	 */
	public static final String STOP_MACHINE = "stop-machine";

	public StopMachine() {
		super();
	}

	@Override
	public void doProcessing() throws AwsException, InterruptedException {
		Melody.getContext().handleProcessorStateUpdates();

		try {
			getInstance().ensureInstanceIsStoped(getTimeout());
		} catch (OperationException Ex) {
			throw new AwsException(Messages.bind(Messages.StopEx_GENERIC_FAIL,
					getTargetElementLocation()), Ex);
		}
	}

}