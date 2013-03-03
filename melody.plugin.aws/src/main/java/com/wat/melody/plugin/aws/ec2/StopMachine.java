package com.wat.melody.plugin.aws.ec2;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.wat.melody.cloud.instance.InstanceState;
import com.wat.melody.common.ex.Util;
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

		if (getAwsInstance() == null) {
			disableNetworkManagement();
			removeInstanceRelatedInfosToED(false);
			throw new AwsException(Messages.bind(
					Messages.StopEx_NO_INSTANCE,
					new Object[] { StopMachine.STOP_MACHINE,
							NewMachine.NEW_MACHINE,
							NewMachine.class.getPackage(),
							getTargetNodeLocation() }));
		} else if (!instanceRuns()) {
			AwsException Ex = new AwsException(Messages.bind(
					Messages.StopMsg_ALREADY_STOPPED, new Object[] {
							getInstanceID(), InstanceState.STOPPED,
							getTargetNodeLocation() }));
			log.warn(Util.getUserFriendlyStackTrace(new AwsException(
					Messages.StopMsg_GENERIC_WARN, Ex)));
			disableNetworkManagement();
			removeInstanceRelatedInfosToED(false);
		} else {
			disableNetworkManagement();
			stopInstance();
			removeInstanceRelatedInfosToED(false);
		}
	}

}