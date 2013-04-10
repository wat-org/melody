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
public class StartMachine extends AbstractOperation {

	/**
	 * The 'StartMachine' XML element
	 */
	public static final String START_MACHINE = "StartMachine";

	public StartMachine() {
		super();
	}

	@Override
	public void doProcessing() throws LibVirtException, InterruptedException {
		Melody.getContext().handleProcessorStateUpdates();

		try {
			getInstance().ensureInstanceIsStarted(getTimeout());
		} catch (OperationException Ex) {
			throw new LibVirtException(Messages.bind(
					Messages.StartEx_GENERIC_FAIL, getTargetNodeLocation()), Ex);
		}
	}

}
