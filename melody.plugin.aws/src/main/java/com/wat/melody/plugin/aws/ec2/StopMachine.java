package com.wat.melody.plugin.aws.ec2;

import com.wat.melody.api.Melody;
import com.wat.melody.api.annotation.Task;
import com.wat.melody.cloud.instance.exception.OperationException;
import com.wat.melody.common.xml.exception.NodeRelatedException;
import com.wat.melody.plugin.aws.ec2.common.AbstractOperation;
import com.wat.melody.plugin.aws.ec2.common.Messages;
import com.wat.melody.plugin.aws.ec2.common.exception.AwsPlugInEc2Exception;

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
	public void doProcessing() throws AwsPlugInEc2Exception,
			InterruptedException {
		Melody.getContext().handleProcessorStateUpdates();

		try {
			getInstanceController().ensureInstanceIsStoped(
					getInstanceDatas().getStopTimeout().getTimeoutInMillis());
		} catch (OperationException Ex) {
			throw new AwsPlugInEc2Exception(new NodeRelatedException(
					getTargetElement(), Messages.StopEx_GENERIC_FAIL, Ex));
		}
	}

}