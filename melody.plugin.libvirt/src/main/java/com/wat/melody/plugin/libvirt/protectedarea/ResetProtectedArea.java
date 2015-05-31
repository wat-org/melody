package com.wat.melody.plugin.libvirt.protectedarea;

import com.wat.melody.api.Melody;
import com.wat.melody.api.annotation.Task;
import com.wat.melody.api.annotation.condition.Condition;
import com.wat.melody.api.annotation.condition.Conditions;
import com.wat.melody.api.annotation.condition.Match;
import com.wat.melody.api.exception.TaskException;
import com.wat.melody.cloud.protectedarea.exception.ProtectedAreaException;
import com.wat.melody.common.firewall.FireWallRulesPerDevice;
import com.wat.melody.common.xml.exception.NodeRelatedException;
import com.wat.melody.plugin.libvirt.common.Messages;
import com.wat.melody.plugin.libvirt.common.exception.LibVirtException;

/**
 * <P>
 * This Task must be called before {@link DeleteProtectedArea}.
 * 
 * <P>
 * Doing so, we are sure that no other Protected Area contains a reference to
 * this one. And the deletion will work properly.
 * 
 * <P>
 * Note that the deletion may still fail (because AWS EC2 Instance use it, or
 * because non-empty protected area use it) but, under this circumstances,
 * failure is the correct behavior.
 * 
 * @author Guillaume Cornet
 * 
 */
@Task(name = ResetProtectedArea.RESET_PROTECTED_AREA)
@Conditions({
		@Condition({ @Match(expression = "§[@provider]§", value = "libvirt") }),
		@Condition({ @Match(expression = "§[provider.cloud]§", value = "libvirt") }) })
public class ResetProtectedArea extends AbstractProtectedAreaOperation {

	/**
	 * Task's name
	 */
	public static final String RESET_PROTECTED_AREA = "reset-protected-area";

	public ResetProtectedArea() {
		super();
	}

	@Override
	public void doProcessing() throws TaskException, InterruptedException {
		Melody.getContext().handleProcessorStateUpdates();

		try {
			// pass an empty fire rules list, which will remove all fire rules
			getProtectedAreaController().ensureProtectedAreaContentIsUpToDate(
					new FireWallRulesPerDevice());
		} catch (ProtectedAreaException Ex) {
			throw new LibVirtException(new NodeRelatedException(
					getTargetElement(), Messages.PAResetEx_GENERIC_FAIL, Ex));
		}
	}

}