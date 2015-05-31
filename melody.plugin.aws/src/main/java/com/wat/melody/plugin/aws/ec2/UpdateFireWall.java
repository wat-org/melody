package com.wat.melody.plugin.aws.ec2;

import com.wat.melody.api.Melody;
import com.wat.melody.api.annotation.Task;
import com.wat.melody.api.annotation.condition.Condition;
import com.wat.melody.api.annotation.condition.Conditions;
import com.wat.melody.api.annotation.condition.Match;
import com.wat.melody.cloud.firewall.xml.FireWallRulesLoader;
import com.wat.melody.cloud.instance.exception.OperationException;
import com.wat.melody.common.firewall.FireWallRulesPerDevice;
import com.wat.melody.common.xml.exception.NodeRelatedException;
import com.wat.melody.plugin.aws.ec2.common.AbstractOperation;
import com.wat.melody.plugin.aws.ec2.common.Messages;
import com.wat.melody.plugin.aws.ec2.common.exception.AwsPlugInEc2Exception;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
@Task(name = UpdateFireWall.UPDATE_FIREWALL)
@Conditions({
		@Condition({ @Match(expression = "§[@provider]§", value = "aws") }),
		@Condition({ @Match(expression = "§[provider.cloud]§", value = "aws") }) })
public class UpdateFireWall extends AbstractOperation {

	/**
	 * Task's name
	 */
	public static final String UPDATE_FIREWALL = "update-firewall";

	private FireWallRulesPerDevice _rulesPerDevice = null;

	public UpdateFireWall() {
		super();
	}

	@Override
	public void validate() throws AwsPlugInEc2Exception {
		super.validate();

		// Build a FwRule's Collection with FwRule Nodes found
		try {
			setFwRules(new FireWallRulesLoader().load(getTargetElement()));
		} catch (NodeRelatedException Ex) {
			throw new AwsPlugInEc2Exception(Ex);
		}
	}

	@Override
	public void doProcessing() throws AwsPlugInEc2Exception,
			InterruptedException {
		Melody.getContext().handleProcessorStateUpdates();

		try {
			getInstanceController().ensureInstanceFireWallRulesAreUpToDate(
					getFwRules());
		} catch (OperationException Ex) {
			throw new AwsPlugInEc2Exception(new NodeRelatedException(
					getTargetElement(), Messages.UpdateFireWallEx_GENERIC_FAIL,
					Ex));

		}
	}

	protected FireWallRulesPerDevice getFwRules() {
		return _rulesPerDevice;
	}

	protected FireWallRulesPerDevice setFwRules(FireWallRulesPerDevice fwrs) {
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