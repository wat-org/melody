package com.wat.melody.plugin.aws.ec2;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.wat.melody.cloud.InstanceState;
import com.wat.melody.plugin.aws.ec2.common.AbstractMachineOperation;
import com.wat.melody.plugin.aws.ec2.common.Messages;
import com.wat.melody.plugin.aws.ec2.common.exception.AwsException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class StopMachine extends AbstractMachineOperation {

	private static Log log = LogFactory.getLog(StopMachine.class);

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

		if (getInstance() == null) {
			disableManagement();
			removeInstanceRelatedInfosToED(false);
			throw new AwsException(Messages.bind(
					Messages.StopEx_NO_INSTANCE,
					new Object[] { StopMachine.STOP_MACHINE,
							NewMachine.NEW_MACHINE,
							NewMachine.class.getPackage(),
							getTargetNodeLocation() }));
		} else if (!instanceRuns()) {
			log.warn(Messages.bind(Messages.StopMsg_ALREADY_STOPPED,
					new Object[] { getAwsInstanceID(), InstanceState.STOPPED,
							getTargetNodeLocation() }));
			disableManagement();
			removeInstanceRelatedInfosToED(false);
		} else {
			disableManagement();
			stopInstance();
			removeInstanceRelatedInfosToED(false);
		}
	}

}