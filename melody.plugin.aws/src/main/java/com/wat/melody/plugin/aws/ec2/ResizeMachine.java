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
@Task(name = ResizeMachine.RESIZE_MACHINE)
public class ResizeMachine extends AbstractOperation {

	/**
	 * Task's name
	 */
	public static final String RESIZE_MACHINE = "resize-machine";

	public ResizeMachine() {
		super();
	}

	@Override
	public void doProcessing() throws AwsException, InterruptedException {
		Melody.getContext().handleProcessorStateUpdates();

		try {
			getInstance().ensureInstanceSizing(
					getInstanceDatas().getInstanceType());
		} catch (OperationException Ex) {
			throw new AwsException(new NodeRelatedException(getTargetElement(),
					Messages.bind(Messages.ResizeEx_GENERIC_FAIL,
							getInstanceDatas().getInstanceType()), Ex));
		}
	}

}