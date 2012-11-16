package com.wat.melody.plugin.libvirt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.wat.melody.plugin.libvirt.common.AbstractMachineOperation;
import com.wat.melody.plugin.libvirt.common.Messages;
import com.wat.melody.plugin.libvirt.common.exception.LibVirtException;

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
		} catch (LibVirtException Ex) {
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
	public void doProcessing() throws LibVirtException, InterruptedException {
		getContext().handleProcessorStateUpdates();

		if (getInstance() == null) {
			log.warn(Messages.bind(Messages.DeleteMsg_NO_INSTANCE,
					getTargetNodeLocation()));
			disableManagement();
			removeInstanceRelatedInfosToED(true);
		} else if (!instanceLives()) {
			log.warn(Messages.bind(Messages.DeleteMsg_TERMINATED, new Object[] {
					getInstanceID(), "DEAD", getTargetNodeLocation() }));
			disableManagement();
			removeInstanceRelatedInfosToED(true);
		} else {
			disableManagement();
			deleteInstance();
			removeInstanceRelatedInfosToED(true);
		}
	}

}