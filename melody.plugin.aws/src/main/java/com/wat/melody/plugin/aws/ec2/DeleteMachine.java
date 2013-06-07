package com.wat.melody.plugin.aws.ec2;

import com.wat.melody.api.Melody;
import com.wat.melody.api.annotation.Task;
import com.wat.melody.cloud.instance.exception.OperationException;
import com.wat.melody.common.xml.exception.NodeRelatedException;
import com.wat.melody.plugin.aws.ec2.common.AbstractOperation;
import com.wat.melody.plugin.aws.ec2.common.Messages;
import com.wat.melody.plugin.aws.ec2.common.exception.AwsException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
@Task(name = DeleteMachine.DELETE_MACHINE)
public class DeleteMachine extends AbstractOperation {

	/**
	 * Task's name
	 */
	public static final String DELETE_MACHINE = "delete-machine";

	public DeleteMachine() {
		super();
	}

	@Override
	public void doProcessing() throws AwsException, InterruptedException {
		Melody.getContext().handleProcessorStateUpdates();

		try {
			getInstance().ensureInstanceIsDestroyed(
					getInstanceDatas().getDeleteTimeout().getTimeoutInMillis());
		} catch (OperationException Ex) {
			throw new AwsException(new NodeRelatedException(getTargetElement(),
					Messages.DestroyEx_GENERIC_FAIL, Ex));
		}
	}

}