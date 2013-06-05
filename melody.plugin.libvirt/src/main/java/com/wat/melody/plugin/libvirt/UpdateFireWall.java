package com.wat.melody.plugin.libvirt;

import com.wat.melody.api.ITask;
import com.wat.melody.api.Melody;
import com.wat.melody.api.annotation.Task;
import com.wat.melody.cloud.firewall.FireWallRulesLoader;
import com.wat.melody.cloud.instance.exception.OperationException;
import com.wat.melody.common.firewall.FireWallRulesPerDevice;
import com.wat.melody.common.xml.exception.NodeRelatedException;
import com.wat.melody.plugin.libvirt.common.AbstractOperation;
import com.wat.melody.plugin.libvirt.common.Messages;
import com.wat.melody.plugin.libvirt.common.exception.LibVirtException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
@Task(name = UpdateFireWall.UPDATE_FIREWALL)
public class UpdateFireWall extends AbstractOperation implements ITask {

	/**
	 * Task's name
	 */
	public static final String UPDATE_FIREWALL = "update-firewall";

	private FireWallRulesPerDevice _rulesPerDevice = null;

	public UpdateFireWall() {
		super();
	}

	@Override
	public void validate() throws LibVirtException {
		super.validate();

		// Build a FwRule's Collection with FwRule Nodes found
		try {
			setFwRules(new FireWallRulesLoader().load(getTargetElement()));
		} catch (NodeRelatedException Ex) {
			throw new LibVirtException(Ex);
		}
	}

	@Override
	public void doProcessing() throws LibVirtException, InterruptedException {
		Melody.getContext().handleProcessorStateUpdates();

		try {
			getInstance().ensureInstanceFireWallRulesAreUpToDate(getFwRules());
		} catch (OperationException Ex) {
			throw new LibVirtException(Messages.bind(
					Messages.UpdateFireWallEx_GENERIC_FAIL,
					getTargetElementLocation()), Ex);
		}
	}

	private FireWallRulesPerDevice getFwRules() {
		return _rulesPerDevice;
	}

	private FireWallRulesPerDevice setFwRules(FireWallRulesPerDevice fwrs) {
		if (fwrs == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ FireWallRulesPerDevice.class.getCanonicalName() + ".");
		}
		FireWallRulesPerDevice previous = getFwRules();
		_rulesPerDevice = fwrs;
		return previous;
	}

}
