package com.wat.melody.plugin.aws.ec2;

import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.amazonaws.services.ec2.model.Instance;
import com.wat.melody.api.annotation.Attribute;
import com.wat.melody.cloud.InstanceType;
import com.wat.melody.cloud.exception.IllegalInstanceTypeException;
import com.wat.melody.plugin.aws.ec2.common.AbstractAwsOperation;
import com.wat.melody.plugin.aws.ec2.common.Common;
import com.wat.melody.plugin.aws.ec2.common.InstanceState;
import com.wat.melody.plugin.aws.ec2.common.Messages;
import com.wat.melody.plugin.aws.ec2.common.exception.AwsException;
import com.wat.melody.xpathextensions.GetHeritedAttribute;
import com.wat.melody.xpathextensions.common.exception.ResourcesDescriptorException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class ResizeMachine extends AbstractAwsOperation {

	private static Log log = LogFactory.getLog(ResizeMachine.class);

	/**
	 * The 'ResizeMachine' XML element
	 */
	public static final String RESIZE_MACHINE = "ResizeMachine";

	/**
	 * The 'instanceType' XML attribute
	 */
	public static final String INSTANCETYPE_ATTR = "instanceType";

	private InstanceType msInstanceType;

	public ResizeMachine() {
		super();
		initInstanceType();
	}

	private void initInstanceType() {
		msInstanceType = null;
	}

	@Override
	public void validate() throws AwsException {
		super.validate();

		try {
			String v = null;
			v = GetHeritedAttribute.getHeritedAttributeValue(getTargetNode(),
					Common.INSTANCETYPE_ATTR);
			try {
				try {
					if (v != null) {
						setInstanceType(InstanceType.parseString(v));
					}
				} catch (IllegalInstanceTypeException Ex) {
					throw new AwsException(Messages.bind(
							Messages.ResizeEx_INVALID_INSTANCETYPE_ATTR, v));
				}
			} catch (AwsException Ex) {
				throw new AwsException(Messages.bind(
						Messages.ResizeEx_INSTANCETYPE_ERROR,
						Common.INSTANCETYPE_ATTR, getTargetNodeLocation()), Ex);
			}
		} catch (ResourcesDescriptorException Ex) {
			throw new AwsException(Ex);
		}

		// Initialize optional task's attributes with their default value
		if (getInstanceType() == null) {
			throw new AwsException(
					Messages.bind(Messages.ResizeEx_MISSING_INSTANCETYPE_ATTR,
							new Object[] { ResizeMachine.INSTANCETYPE_ATTR,
									ResizeMachine.RESIZE_MACHINE,
									Common.INSTANCETYPE_ATTR,
									getTargetNodeLocation() }));
		}
	}

	@Override
	public void doProcessing() throws AwsException, InterruptedException {
		getContext().handleProcessorStateUpdates();

		Instance i = getInstance();
		if (i == null) {
			removeInstanceRelatedInfosToED(true);
			throw new AwsException(Messages.bind(
					Messages.ResizeEx_NO_INSTANCE,
					new Object[] { ResizeMachine.RESIZE_MACHINE,
							NewMachine.NEW_MACHINE,
							NewMachine.class.getPackage(),
							getTargetNodeLocation() }));
		} else if (Common.getInstanceState(getEc2(), getAwsInstanceID()) != InstanceState.STOPPED) {
			setInstanceRelatedInfosToED(i);
			throw new AwsException(Messages.bind(
					Messages.ResizeEx_NOT_STOPPED,
					new Object[] { getAwsInstanceID(), InstanceState.STOPPED,
							ResizeMachine.RESIZE_MACHINE,
							StopMachine.STOP_MACHINE,
							StopMachine.class.getPackage(),
							getTargetNodeLocation() }));
		} else {
			InstanceType currentType = null;
			try {
				currentType = InstanceType.parseString(i.getInstanceType());
			} catch (IllegalInstanceTypeException Ex) {
				throw new RuntimeException("Unexpected error while parsing "
						+ "the InstanceType '" + i.getInstanceType() + "'. "
						+ "Because this value have just been retreive from "
						+ "AWS, such error cannot happened. "
						+ "Source code has certainly been modified and a bug "
						+ "have been introduced.", Ex);
			}
			if (currentType != getInstanceType()) {
				if (!resizeInstance(getInstanceType())) {
					throw new AwsException(
							Messages.bind(Messages.ResizeEx_FAILED,
									new Object[] { getAwsInstanceID(),
											currentType, getInstanceType(),
											getTargetNodeLocation() }));
				}
			} else {
				log.warn(Messages.bind(Messages.ResizeMsg_NO_NEED,
						new Object[] { getAwsInstanceID(), getInstanceType(),
								getTargetNodeLocation() }));
			}
			setInstanceRelatedInfosToED(i);
		}
	}

	public InstanceType getInstanceType() {
		return msInstanceType;
	}

	@Attribute(name = INSTANCETYPE_ATTR)
	public InstanceType setInstanceType(InstanceType instanceType)
			throws AwsException {
		if (instanceType == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid InstanceType (Accepted values are "
					+ Arrays.asList(InstanceType.values()) + ").");
		}
		InstanceType previous = getInstanceType();
		msInstanceType = instanceType;
		return previous;
	}

}
