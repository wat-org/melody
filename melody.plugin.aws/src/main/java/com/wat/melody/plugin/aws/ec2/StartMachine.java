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
public class StartMachine extends AbstractMachineOperation {

	private static Log log = LogFactory.getLog(StartMachine.class);

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

		InstanceState is = getInstanceState();
		if (getAwsInstance() == null) {
			removeInstanceRelatedInfosToED(true);
			throw new AwsException(Messages.bind(
					Messages.StartEx_NO_INSTANCE,
					new Object[] { StartMachine.START_MACHINE,
							NewMachine.NEW_MACHINE,
							NewMachine.class.getPackage(),
							getTargetNodeLocation() }));
		} else if (is == InstanceState.PENDING) {
			AwsException Ex = new AwsException(Messages.bind(
					Messages.StartMsg_PENDING, new Object[] { getInstanceID(),
							InstanceState.PENDING, InstanceState.RUNNING,
							getTargetNodeLocation() }));
			log.warn(Util.getUserFriendlyStackTrace(new AwsException(
					Messages.StartMsg_GENERIC_WARN, Ex)));
			waitUntilInstanceStatusBecomes(InstanceState.RUNNING, getTimeout());
			setInstanceRelatedInfosToED(getAwsInstance());
			enableNetworkManagement();
		} else if (is == InstanceState.RUNNING) {
			AwsException Ex = new AwsException(Messages.bind(
					Messages.StartMsg_RUNNING, new Object[] { getInstanceID(),
							InstanceState.RUNNING, getTargetNodeLocation() }));
			log.warn(Util.getUserFriendlyStackTrace(new AwsException(
					Messages.StartMsg_GENERIC_WARN, Ex)));
			setInstanceRelatedInfosToED(getAwsInstance());
			enableNetworkManagement();
		} else if (is == InstanceState.STOPPING) {
			AwsException Ex = new AwsException(Messages.bind(
					Messages.StartMsg_STOPPING, new Object[] { getInstanceID(),
							InstanceState.STOPPING, InstanceState.STOPPED,
							getTargetNodeLocation() }));
			log.warn(Util.getUserFriendlyStackTrace(new AwsException(
					Messages.StartMsg_GENERIC_WARN, Ex)));
			disableNetworkManagement();
			waitUntilInstanceStatusBecomes(InstanceState.STOPPED, getTimeout());
			startInstance();
			setInstanceRelatedInfosToED(getAwsInstance());
			enableNetworkManagement();
		} else if (is == InstanceState.SHUTTING_DOWN) {
			disableNetworkManagement();
			removeInstanceRelatedInfosToED(true);
			throw new AwsException(Messages.bind(
					Messages.StartEx_SHUTTING_DOWN, new Object[] {
							getInstanceID(), InstanceState.SHUTTING_DOWN,
							StartMachine.START_MACHINE, NewMachine.NEW_MACHINE,
							NewMachine.class.getPackage(),
							getTargetNodeLocation() }));
		} else if (is == InstanceState.TERMINATED) {
			disableNetworkManagement();
			removeInstanceRelatedInfosToED(true);
			throw new AwsException(Messages.bind(Messages.StartEx_TERMINATED,
					new Object[] { getInstanceID(), InstanceState.TERMINATED,
							StartMachine.START_MACHINE, NewMachine.NEW_MACHINE,
							NewMachine.class.getPackage(),
							getTargetNodeLocation() }));
		} else {
			startInstance();
			setInstanceRelatedInfosToED(getAwsInstance());
			enableNetworkManagement();
		}
	}

}
