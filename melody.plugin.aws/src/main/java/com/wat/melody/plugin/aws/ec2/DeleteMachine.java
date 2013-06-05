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
@Task(name = DeleteMachine.DELETE_MACHINE)
public class DeleteMachine extends AbstractOperation {

	/**
	 * Task's name
	 */
	public static final String DELETE_MACHINE = "delete-machine";

	public DeleteMachine() {
		super();
		try {
			// delete operation can be long. double default timeout
			setTimeout(getTimeout() * 2);
		} catch (AwsException Ex) {
			throw new RuntimeException("Unexpected error while initializing "
					+ "the " + DELETE_MACHINE + " timeout to "
					+ (getTimeout() * 2) + ". "
					+ "Because this value is hardocded, suche error cannot "
					+ "happened. "
					+ "Source code has certainly been modified and a bug "
					+ "have been introduced.", Ex);
		}
	}

	@Override
	public void doProcessing() throws AwsException, InterruptedException {
		Melody.getContext().handleProcessorStateUpdates();

		try {
			getInstance().ensureInstanceIsDestroyed(getTimeout());
		} catch (OperationException Ex) {
			throw new AwsException(
					Messages.bind(Messages.DestroyEx_GENERIC_FAIL,
							getTargetElementLocation()), Ex);
		}
	}

}
