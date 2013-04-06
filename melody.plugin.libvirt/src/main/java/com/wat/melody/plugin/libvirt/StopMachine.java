package com.wat.melody.plugin.libvirt;

import com.wat.melody.cloud.instance.exception.OperationException;
import com.wat.melody.plugin.libvirt.common.AbstractOperation;
import com.wat.melody.plugin.libvirt.common.Messages;
import com.wat.melody.plugin.libvirt.common.exception.LibVirtException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class StopMachine extends AbstractOperation {

	/**
	 * The 'StopMachine' XML element
	 */
	public static final String STOP_MACHINE = "StopMachine";

	public StopMachine() {
		super();
	}

	@Override
	public void doProcessing() throws LibVirtException, InterruptedException {
		getContext().handleProcessorStateUpdates();

		try {
			getInstance().ensureInstanceIsStoped(getTimeout());
		} catch (OperationException Ex) {
			throw new LibVirtException(Messages.bind(
					Messages.StopEx_GENERIC_FAIL, getTargetNodeLocation()), Ex);
		}
	}

}