package com.wat.melody.plugin.libvirt;

import com.wat.melody.api.Melody;
import com.wat.melody.cloud.instance.exception.OperationException;
import com.wat.melody.plugin.libvirt.common.AbstractOperation;
import com.wat.melody.plugin.libvirt.common.Messages;
import com.wat.melody.plugin.libvirt.common.exception.LibVirtException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class DeleteMachine extends AbstractOperation {

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
		Melody.getContext().handleProcessorStateUpdates();

		try {
			getInstance().ensureInstanceIsDestroyed(getTimeout());
		} catch (OperationException Ex) {
			throw new LibVirtException(
					Messages.bind(Messages.DestroyEx_GENERIC_FAIL,
							getTargetElementLocation()), Ex);
		}
	}

}