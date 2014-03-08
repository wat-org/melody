package com.wat.melody.plugin.aws.ec2.protectedarea;

import com.wat.melody.api.Melody;
import com.wat.melody.api.annotation.Task;
import com.wat.melody.api.exception.TaskException;
import com.wat.melody.cloud.firewall.xml.FireWallRulesLoader;
import com.wat.melody.cloud.protectedarea.exception.ProtectedAreaException;
import com.wat.melody.common.firewall.FireWallRulesPerDevice;
import com.wat.melody.common.xml.exception.NodeRelatedException;
import com.wat.melody.plugin.aws.ec2.common.Messages;
import com.wat.melody.plugin.aws.ec2.common.exception.AwsPlugInEc2Exception;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
@Task(name = UpdateProtectedArea.UPDATE_PROTECTED_AREA)
public class UpdateProtectedArea extends AbstractProtectedAreaOperation {

	/**
	 * Task's name
	 */
	public static final String UPDATE_PROTECTED_AREA = "update-protected-area";

	private FireWallRulesPerDevice _rules = null;

	public UpdateProtectedArea() {
		super();
	}

	@Override
	public void validate() throws AwsPlugInEc2Exception {
		super.validate();

		// Build a FwRule's Collection with FwRule Nodes found.
		// The 'Per Device' aspect of this FwRules is not used.
		try {
			setFwRules(new FireWallRulesLoader().load(getTargetElement()));
		} catch (NodeRelatedException Ex) {
			throw new AwsPlugInEc2Exception(Ex);
		}
	}

	@Override
	public void doProcessing() throws TaskException, InterruptedException {
		Melody.getContext().handleProcessorStateUpdates();

		try {
			getProtectedAreaController().ensureProtectedAreaContentIsUpToDate(
					getFwRules());
		} catch (ProtectedAreaException Ex) {
			throw new AwsPlugInEc2Exception(new NodeRelatedException(
					getTargetElement(), Messages.PAContentEx_GENERIC_FAIL, Ex));

		}
	}

	protected FireWallRulesPerDevice getFwRules() {
		return _rules;
	}

	protected FireWallRulesPerDevice setFwRules(FireWallRulesPerDevice fwrs) {
		if (fwrs == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ FireWallRulesPerDevice.class.getCanonicalName() + ".");
		}
		FireWallRulesPerDevice previous = getFwRules();
		_rules = fwrs;
		return previous;
	}

}