package com.wat.melody.plugin.libvirt;

import com.wat.melody.api.Melody;
import com.wat.melody.api.annotation.Task;
import com.wat.melody.cloud.instance.exception.OperationException;
import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.xml.exception.NodeRelatedException;
import com.wat.melody.plugin.libvirt.common.AbstractOperation;
import com.wat.melody.plugin.libvirt.common.Messages;
import com.wat.melody.plugin.libvirt.common.exception.LibVirtException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
@Task(name = ResizeMachine.RESIZE_MACHINE)
public class ResizeMachine extends AbstractOperation {

	/**
	 * Task's name
	 */
	public static final String RESIZE_MACHINE = "resize-machine";

	public ResizeMachine() {
		super();
	}

	@Override
	public void doProcessing() throws LibVirtException, InterruptedException {
		Melody.getContext().handleProcessorStateUpdates();

		try {
			getInstance().ensureInstanceSizing(
					getInstanceDatas().getInstanceType());
		} catch (OperationException Ex) {
			throw new LibVirtException(new NodeRelatedException(
					getTargetElement(), Msg.bind(
							Messages.ResizeEx_GENERIC_FAIL, getInstanceDatas()
									.getInstanceType()), Ex));
		}
	}

}