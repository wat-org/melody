package com.wat.melody.plugin.aws.ec2;

import java.io.IOException;
import java.util.Arrays;

import org.w3c.dom.Node;

import com.wat.melody.api.Melody;
import com.wat.melody.api.annotation.Attribute;
import com.wat.melody.api.exception.ResourcesDescriptorException;
import com.wat.melody.cloud.instance.InstanceController;
import com.wat.melody.cloud.instance.InstanceType;
import com.wat.melody.cloud.instance.exception.IllegalInstanceTypeException;
import com.wat.melody.cloud.instance.exception.OperationException;
import com.wat.melody.common.keypair.KeyPairName;
import com.wat.melody.common.keypair.KeyPairRepositoryPath;
import com.wat.melody.common.keypair.exception.IllegalKeyPairNameException;
import com.wat.melody.plugin.aws.ec2.common.AbstractOperation;
import com.wat.melody.plugin.aws.ec2.common.AwsInstanceController;
import com.wat.melody.plugin.aws.ec2.common.AwsKeyPairRepository;
import com.wat.melody.plugin.aws.ec2.common.Common;
import com.wat.melody.plugin.aws.ec2.common.Messages;
import com.wat.melody.plugin.aws.ec2.common.exception.AwsException;
import com.wat.melody.xpathextensions.XPathHelper;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class NewMachine extends AbstractOperation {

	/**
	 * The 'NewMachine' XML element
	 */
	public static final String NEW_MACHINE = "NewMachine";

	/**
	 * The 'instanceType' XML attribute
	 */
	public static final String INSTANCETYPE_ATTR = "instanceType";

	/**
	 * The 'imageId' XML attribute
	 */
	public static final String IMAGEID_ATTR = "imageId";

	/**
	 * The 'availabilityZone' XML attribute
	 */
	public static final String AVAILABILITYZONE_ATTR = "availabilityZone";

	/**
	 * The 'keyPairName' XML attribute
	 */
	public static final String KEYPAIR_NAME_ATTR = "keypair-name";

	/**
	 * The 'passphrase' XML attribute
	 */
	public static final String PASSPHRASE_ATTR = "passphrase";

	/**
	 * The 'keyRepository' XML attribute
	 */
	public static final String KEYPAIR_REPO_ATTR = "keypair-repository";

	private InstanceType msInstanceType;
	private String msImageId;
	private KeyPairRepositoryPath moKeyPairRepository;
	private KeyPairName moKeyPairName;
	private String msPassphrase;
	private String msAvailabilityZone;

	public NewMachine() {
		super();
		initAvailabilityZone();
		initPassphrase();
		initKeyPairName();
		initKeyPairRepository();
		initImageId();
		initInstanceType();
	}

	private void initAvailabilityZone() {
		msAvailabilityZone = null;
	}

	private void initPassphrase() {
		msPassphrase = null;
	}

	private void initKeyPairName() {
		moKeyPairName = null;
	}

	private void initKeyPairRepository() {
		moKeyPairRepository = null;
	}

	private void initImageId() {
		msImageId = null;
	}

	private void initInstanceType() {
		msInstanceType = null;
	}

	@Override
	public void validate() throws AwsException {
		super.validate();

		try {
			/*
			 * TODO : create an InstanceDatasLoader and put all this inside.
			 * 
			 * Also create an InstanceHelper and remove all task attribute.
			 */
			Node n = getTargetNode();
			String v = null;

			v = XPathHelper.getHeritedAttributeValue(n,
					Common.INSTANCETYPE_ATTR);
			try {
				if (v != null) {
					setInstanceType(InstanceType.parseString(v));
				}
			} catch (IllegalInstanceTypeException Ex) {
				throw new AwsException(Messages.bind(
						Messages.NewEx_INSTANCETYPE_ERROR,
						Common.INSTANCETYPE_ATTR, getTargetNodeLocation()), Ex);
			}

			v = XPathHelper.getHeritedAttributeValue(n, Common.IMAGEID_ATTR);
			try {
				if (v != null) {
					setImageId(v);
				}
			} catch (AwsException Ex) {
				throw new AwsException(Messages.bind(
						Messages.NewEx_IMAGEID_ERROR, Common.IMAGEID_ATTR,
						getTargetNodeLocation()), Ex);
			}

			v = XPathHelper.getHeritedAttributeValue(n,
					Common.AVAILABILITYZONE_ATTR);
			try {
				if (v != null) {
					setAvailabilityZone(v);
				}
			} catch (AwsException Ex) {
				throw new AwsException(Messages.bind(
						Messages.NewEx_AVAILABILITYZONE_ERROR,
						Common.AVAILABILITYZONE_ATTR, getTargetNodeLocation()),
						Ex);
			}

			v = XPathHelper.getHeritedAttributeValue(n,
					Common.KEYPAIR_NAME_ATTR);
			try {
				if (v != null) {
					setKeyPairName(KeyPairName.parseString(v));
				}
			} catch (IllegalKeyPairNameException Ex) {
				throw new AwsException(Messages.bind(
						Messages.NewEx_KEYPAIR_NAME_ERROR,
						Common.KEYPAIR_NAME_ATTR, getTargetNodeLocation()), Ex);
			}

			v = XPathHelper.getHeritedAttributeValue(n, Common.PASSPHRASE_ATTR);
			if (v != null) {
				setPassphrase(v);
			}
		} catch (ResourcesDescriptorException Ex) {
			throw new AwsException(Ex);
		}

		// Get the default KeyPair Repository, if not provided.
		if (getKeyPairRepositoryPath() == null) {
			setKeyPairRepositoryPath(getSshPlugInConf()
					.getKeyPairRepositoryPath());
		}
		// Validate everything is provided.
		if (getInstanceType() == null) {
			throw new AwsException(Messages.bind(
					Messages.NewEx_MISSING_INSTANCETYPE_ATTR,
					NewMachine.INSTANCETYPE_ATTR, NewMachine.NEW_MACHINE,
					Common.INSTANCETYPE_ATTR, getTargetNodeLocation()));
		}

		if (getImageId() == null) {
			throw new AwsException(Messages.bind(
					Messages.NewEx_MISSING_IMAGEID_ATTR,
					NewMachine.IMAGEID_ATTR, NewMachine.NEW_MACHINE,
					Common.IMAGEID_ATTR, getTargetNodeLocation()));
		}

		if (getKeyPairName() == null) {
			throw new AwsException(Messages.bind(
					Messages.NewEx_MISSING_KEYPAIR_NAME_ATTR,
					NewMachine.KEYPAIR_NAME_ATTR, NewMachine.NEW_MACHINE,
					Common.KEYPAIR_NAME_ATTR, getTargetNodeLocation()));
		}

		// Validate task's attributes
		// AZ must be validated AFTER the Aws Region is known
		// AZ can be null
		if (getAvailabilityZoneFullName() != null
				&& !Common.availabilityZoneExists(getEc2(),
						getAvailabilityZoneFullName())) {
			throw new AwsException(Messages.bind(
					Messages.NewEx_INVALID_AVAILABILITYZONE_ATTR,
					getAvailabilityZone(), getRegion()));
		}
		// imageId must be validated AFTER the Aws Region is known
		if (!Common.imageIdExists(getEc2(), getImageId())) {
			throw new AwsException(Messages.bind(
					Messages.NewEx_INVALID_IMAGEID_ATTR, getImageId(),
					getRegion()));
		}
	}

	@Override
	public void doProcessing() throws AwsException, InterruptedException {
		Melody.getContext().handleProcessorStateUpdates();

		try {
			getInstance().ensureInstanceIsCreated(getInstanceType(),
					getAvailabilityZoneFullName(), getImageId(),
					getKeyPairName(), getTimeout());
		} catch (OperationException Ex) {
			throw new AwsException(Messages.bind(
					Messages.CreateEx_GENERIC_FAIL, getRegion(), getImageId(),
					getInstanceType(), getKeyPairName(),
					getAvailabilityZoneFullName(), getTargetNodeLocation()), Ex);
		}
	}

	/**
	 * @return an {@link AwsInstanceController} which provides additional
	 *         KeyPair Management features.
	 */
	@Override
	public InstanceController newAwsInstanceController() {
		// create AwsInstanceControllerWithKeyPairManagement class ?
		return new AwsInstanceController(getEc2(), getInstanceId()) {

			public String createInstance(InstanceType type, String site,
					String imageId, KeyPairName keyPairName, long createTimeout)
					throws OperationException, InterruptedException {
				try {
					AwsKeyPairRepository kpr = AwsKeyPairRepository
							.getAwsKeyPairRepository(getConnection(),
									getKeyPairRepositoryPath());
					kpr.createKeyPair(getKeyPairName(), getSshPlugInConf()
							.getKeyPairSize(), getPassphrase());
				} catch (IOException | AwsException Ex) {
					throw new OperationException(Ex);
				}

				return super.createInstance(type, site, imageId, keyPairName,
						createTimeout);
			}

		};
	}

	public InstanceType getInstanceType() {
		return msInstanceType;
	}

	@Attribute(name = INSTANCETYPE_ATTR)
	public InstanceType setInstanceType(InstanceType instanceType) {
		if (instanceType == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid InstanceType (Accepted values are "
					+ Arrays.asList(InstanceType.values()) + ").");
		}
		InstanceType previous = getInstanceType();
		msInstanceType = instanceType;
		return previous;
	}

	public String getImageId() {
		return msImageId;
	}

	@Attribute(name = IMAGEID_ATTR)
	public String setImageId(String imageId) throws AwsException {
		if (imageId == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (an AWS AMI Id).");
		}
		String previous = getImageId();
		msImageId = imageId;
		return previous;
	}

	public String getAvailabilityZone() {
		return msAvailabilityZone;
	}

	@Attribute(name = AVAILABILITYZONE_ATTR)
	public String setAvailabilityZone(String sAZ) throws AwsException {
		if (sAZ == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (an AWS Availability Zone).");
		}
		String previous = getAvailabilityZone();
		msAvailabilityZone = sAZ;
		return previous;
	}

	public String getAvailabilityZoneFullName() {
		if (getAvailabilityZone() == null) {
			return null;
		}
		return getRegion() + getAvailabilityZone();
	}

	public KeyPairName getKeyPairName() {
		return moKeyPairName;
	}

	@Attribute(name = KEYPAIR_NAME_ATTR)
	public KeyPairName setKeyPairName(KeyPairName keyPairName) {
		if (keyPairName == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (an AWS KeyPair Name).");
		}
		KeyPairName previous = getKeyPairName();
		moKeyPairName = keyPairName;
		return previous;
	}

	public String getPassphrase() {
		return msPassphrase;
	}

	@Attribute(name = PASSPHRASE_ATTR)
	public String setPassphrase(String sPassphrase) {
		if (sPassphrase == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Cannot be null.");
		}
		String previous = getPassphrase();
		msPassphrase = sPassphrase;
		return previous;
	}

	public KeyPairRepositoryPath getKeyPairRepositoryPath() {
		return moKeyPairRepository;
	}

	@Attribute(name = KEYPAIR_REPO_ATTR)
	public KeyPairRepositoryPath setKeyPairRepositoryPath(
			KeyPairRepositoryPath keyPairRepository) {
		if (keyPairRepository == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid File (a Key Repository Path).");
		}
		KeyPairRepositoryPath previous = getKeyPairRepositoryPath();
		moKeyPairRepository = keyPairRepository;
		return previous;
	}

}
