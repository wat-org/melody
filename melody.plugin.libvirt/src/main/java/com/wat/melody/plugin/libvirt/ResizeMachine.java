package com.wat.melody.plugin.libvirt;

import java.util.Arrays;

import com.wat.melody.api.annotation.Attribute;
import com.wat.melody.api.exception.ResourcesDescriptorException;
import com.wat.melody.cloud.instance.InstanceType;
import com.wat.melody.cloud.instance.exception.IllegalInstanceTypeException;
import com.wat.melody.plugin.libvirt.common.AbstractOperation;
import com.wat.melody.plugin.libvirt.common.Common;
import com.wat.melody.plugin.libvirt.common.Messages;
import com.wat.melody.plugin.libvirt.common.exception.LibVirtException;
import com.wat.melody.xpathextensions.XPathExpander;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class ResizeMachine extends AbstractOperation {

//	private static Log log = LogFactory.getLog(ResizeMachine.class);

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
	public void validate() throws LibVirtException {
		super.validate();

		try {
			String v = null;
			v = XPathExpander.getHeritedAttributeValue(getTargetNode(),
					Common.INSTANCETYPE_ATTR);
			try {
				try {
					if (v != null) {
						setInstanceType(InstanceType.parseString(v));
					}
				} catch (IllegalInstanceTypeException Ex) {
					throw new LibVirtException(Messages.bind(
							Messages.ResizeEx_INVALID_INSTANCETYPE_ATTR, v));
				}
			} catch (LibVirtException Ex) {
				throw new LibVirtException(Messages.bind(
						Messages.ResizeEx_INSTANCETYPE_ERROR,
						Common.INSTANCETYPE_ATTR, getTargetNodeLocation()), Ex);
			}
		} catch (ResourcesDescriptorException Ex) {
			throw new LibVirtException(Ex);
		}

		// Initialize optional task's attributes with their default value
		if (getInstanceType() == null) {
			throw new LibVirtException(
					Messages.bind(Messages.ResizeEx_MISSING_INSTANCETYPE_ATTR,
							new Object[] { ResizeMachine.INSTANCETYPE_ATTR,
									ResizeMachine.RESIZE_MACHINE,
									Common.INSTANCETYPE_ATTR,
									getTargetNodeLocation() }));
		}
	}

	@Override
	public void doProcessing() throws LibVirtException, InterruptedException {
		getContext().handleProcessorStateUpdates();
		
		/*
		 * TODO : implement resize.
		 */
		throw new RuntimeException("Not implemented yet");

//		Instance i = getInstance();
//		if (i == null) {
//			removeInstanceRelatedInfosToED(true);
//			throw new AwsException(Messages.bind(
//					Messages.ResizeEx_NO_INSTANCE,
//					new Object[] { ResizeMachine.RESIZE_MACHINE,
//							NewMachine.NEW_MACHINE,
//							NewMachine.class.getPackage(),
//							getTargetNodeLocation() }));
//		} else if (Common.getInstanceState(getEc2(), getInstanceID()) != InstanceState.STOPPED) {
//			setInstanceRelatedInfosToED(i);
//			throw new AwsException(Messages.bind(
//					Messages.ResizeEx_NOT_STOPPED,
//					new Object[] { getInstanceID(), InstanceState.STOPPED,
//							ResizeMachine.RESIZE_MACHINE,
//							StopMachine.STOP_MACHINE,
//							StopMachine.class.getPackage(),
//							getTargetNodeLocation() }));
//		} else {
//			InstanceType currentType = i.getInstanceType();
//			if (currentType != getInstanceType()) {
//				if (!resizeInstance(getInstanceType())) {
//					throw new AwsException(
//							Messages.bind(Messages.ResizeEx_FAILED,
//									new Object[] { getInstanceID(),
//											currentType, getInstanceType(),
//											getTargetNodeLocation() }));
//				}
//			} else {
//				AwsException Ex = new AwsException(Messages.bind(
//						Messages.ResizeMsg_NO_NEED, new Object[] {
//								getInstanceID(), getInstanceType(),
//								getTargetNodeLocation() }));
//				log.warn(Util.getUserFriendlyStackTrace(new AwsException(
//						Messages.ResizeMsg_GENERIC_WARN, Ex)));
//			}
//			setInstanceRelatedInfosToED(i);
//		}
	}

	public InstanceType getInstanceType() {
		return msInstanceType;
	}

	@Attribute(name = INSTANCETYPE_ATTR)
	public InstanceType setInstanceType(InstanceType instanceType)
			throws LibVirtException {
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
