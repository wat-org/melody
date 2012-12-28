package com.wat.melody.plugin.aws.ec2;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.wat.melody.plugin.aws.ec2.common.AbstractMachineOperation;
import com.wat.melody.plugin.aws.ec2.common.Common;
import com.wat.melody.plugin.aws.ec2.common.Messages;
import com.wat.melody.plugin.aws.ec2.common.exception.AwsException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class DeleteMachine extends AbstractMachineOperation {

	private static Log log = LogFactory.getLog(DeleteMachine.class);

	/**
	 * The 'DeleteMachine' XML element
	 */
	public static final String DELETE_MACHINE = "DeleteMachine";

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
		getContext().handleProcessorStateUpdates();

		if (getInstance() == null) {
			log.warn(Messages.bind(Messages.DeleteMsg_NO_INSTANCE,
					getTargetNodeLocation()));
			disableNetworkManagement();
			removeInstanceRelatedInfosToED(true);
		} else if (!instanceLives()) {
			log.warn(Messages.bind(Messages.DeleteMsg_TERMINATED, new Object[] {
					getAwsInstanceID(), "DEAD", getTargetNodeLocation() }));
			disableNetworkManagement();
			removeInstanceRelatedInfosToED(true);
			Common.deleteSecurityGroup(getEc2(), getInstance()
					.getSecurityGroups().get(0).getGroupName());
		} else {
			disableNetworkManagement();
			deleteInstance();
			removeInstanceRelatedInfosToED(true);
		}
		/*
		 * TODO : when creating an instance, store the generated SG name into
		 * the ED. When deleting an instance, delete the SG based on its name.
		 */
	}

}
