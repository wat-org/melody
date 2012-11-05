package com.wat.melody.plugin.aws.ec2;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.wat.melody.plugin.aws.ec2.common.AbstractMachineOperation;
import com.wat.melody.plugin.aws.ec2.common.InstanceState;
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
	 * The 'StartMachine' XML element used in the Sequence Descriptor
	 */
	public static final String START_MACHINE = "StartMachine";

	public StartMachine() {
		super();
	}

	@Override
	public void doProcessing() throws AwsException, InterruptedException {
		getContext().handleProcessorStateUpdates();

		InstanceState is = getInstanceState();
		if (getInstance() == null) {
			removeInstanceRelatedInfosToED(true);
			throw new AwsException(Messages.bind(
					Messages.StartEx_NO_INSTANCE,
					new Object[] { StartMachine.START_MACHINE,
							NewMachine.NEW_MACHINE,
							NewMachine.class.getPackage() }));
		} else if (is == InstanceState.PENDING) {
			log.warn(Messages.bind(Messages.StartMsg_PENDING, new Object[] {
					getAwsInstanceID(), InstanceState.PENDING,
					InstanceState.RUNNING }));
			waitUntilInstanceStatusBecomes(InstanceState.RUNNING, getTimeout());
			setInstanceRelatedInfosToED(getInstance());
			enableManagement();
		} else if (is == InstanceState.RUNNING) {
			log.warn(Messages.bind(Messages.StartMsg_RUNNING,
					getAwsInstanceID(), InstanceState.RUNNING));
			setInstanceRelatedInfosToED(getInstance());
			enableManagement();
		} else if (is == InstanceState.STOPPING) {
			log.warn(Messages.bind(Messages.StartMsg_STOPPING, new Object[] {
					getAwsInstanceID(), InstanceState.STOPPING,
					InstanceState.STOPPED }));
			disableManagement();
			waitUntilInstanceStatusBecomes(InstanceState.STOPPED, getTimeout());
			startInstance();
			setInstanceRelatedInfosToED(getInstance());
			enableManagement();
		} else if (is == InstanceState.SHUTTING_DOWN) {
			disableManagement();
			removeInstanceRelatedInfosToED(true);
			throw new AwsException(Messages.bind(
					Messages.StartEx_SHUTTING_DOWN, new Object[] {
							getAwsInstanceID(), InstanceState.SHUTTING_DOWN,
							StartMachine.START_MACHINE, NewMachine.NEW_MACHINE,
							NewMachine.class.getPackage() }));
		} else if (is == InstanceState.TERMINATED) {
			disableManagement();
			removeInstanceRelatedInfosToED(true);
			throw new AwsException(Messages.bind(Messages.StartEx_TERMINATED,
					new Object[] { getAwsInstanceID(),
							InstanceState.TERMINATED,
							StartMachine.START_MACHINE, NewMachine.NEW_MACHINE,
							NewMachine.class.getPackage() }));
		} else {
			startInstance();
			setInstanceRelatedInfosToED(getInstance());
			enableManagement();
		}
	}

}
