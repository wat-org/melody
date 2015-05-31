package com.wat.melody.plugin.libvirt;

import com.wat.melody.api.Melody;
import com.wat.melody.api.annotation.Task;
import com.wat.melody.api.annotation.condition.Condition;
import com.wat.melody.api.annotation.condition.Conditions;
import com.wat.melody.api.annotation.condition.Match;
import com.wat.melody.cloud.instance.exception.OperationException;
import com.wat.melody.common.xml.exception.NodeRelatedException;
import com.wat.melody.plugin.libvirt.common.AbstractOperation;
import com.wat.melody.plugin.libvirt.common.Messages;
import com.wat.melody.plugin.libvirt.common.exception.LibVirtException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
@Task(name = StopMachine.STOP_MACHINE)
@Conditions({
		@Condition({ @Match(expression = "§[@provider]§", value = "libvirt") }),
		@Condition({ @Match(expression = "§[provider.cloud]§", value = "libvirt") }) })
public class StopMachine extends AbstractOperation {

	/**
	 * Task's name
	 */
	public static final String STOP_MACHINE = "stop-machine";

	public StopMachine() {
		super();
	}

	@Override
	public void doProcessing() throws LibVirtException, InterruptedException {
		Melody.getContext().handleProcessorStateUpdates();

		try {
			getInstanceController().ensureInstanceIsStoped(
					getInstanceDatas().getStopTimeout().getTimeoutInMillis());
		} catch (OperationException Ex) {
			throw new LibVirtException(new NodeRelatedException(
					getTargetElement(), Messages.StopEx_GENERIC_FAIL, Ex));
		}
	}

}