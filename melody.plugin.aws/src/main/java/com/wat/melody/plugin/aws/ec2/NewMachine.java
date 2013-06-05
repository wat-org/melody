package com.wat.melody.plugin.aws.ec2;

import java.io.IOException;
import java.util.Arrays;

import org.w3c.dom.Element;

import com.wat.cloud.aws.ec2.AwsEc2Cloud;
import com.wat.cloud.aws.ec2.AwsInstanceController;
import com.wat.cloud.aws.ec2.AwsKeyPairRepository;
import com.wat.cloud.aws.ec2.exception.AwsKeyPairRepositoryException;
import com.wat.melody.api.Melody;
import com.wat.melody.api.annotation.Attribute;
import com.wat.melody.api.annotation.Task;
import com.wat.melody.cloud.instance.InstanceController;
import com.wat.melody.cloud.instance.InstanceType;
import com.wat.melody.cloud.instance.exception.IllegalInstanceTypeException;
import com.wat.melody.cloud.instance.exception.OperationException;
import com.wat.melody.common.keypair.KeyPairName;
import com.wat.melody.common.keypair.KeyPairRepositoryPath;
import com.wat.melody.common.keypair.exception.IllegalKeyPairNameException;
import com.wat.melody.common.xml.exception.NodeRelatedException;
import com.wat.melody.plugin.aws.ec2.common.AbstractOperation;
import com.wat.melody.plugin.aws.ec2.common.Common;
import com.wat.melody.plugin.aws.ec2.common.Messages;
import com.wat.melody.plugin.aws.ec2.common.exception.AwsException;
import com.wat.melody.xpathextensions.XPathHelper;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
@Task(name = NewMachine.NEW_MACHINE)
public class NewMachine extends AbstractOperation {

	/**
	 * Task's name
	 */
	public static final String NEW_MACHINE = "new-machine";

	/**
	 * Task's attribute, which specifies the type of the instance to create.
	 */
	public static final String INSTANCETYPE_ATTR = "instance-type";

	/**
	 * Task's attribute, which specifies the machine image to use for the
	 * creation of the new instance.
	 */
	public static final String IMAGEID_ATTR = "image-id";

	/**
	 * Task's attribute, which specifies the location where the new instance
	 * will be located.
	 */
	public static final String AVAILABILITYZONE_ATTR = "availability-zone";

	/**
	 * Task's attribute, which specifies the key-pair to associate to the new
	 * instance.
	 */
	public static final String KEYPAIR_NAME_ATTR = "keypair-name";

	/**
	 * Task's attribute, which specifies the pass-phrase of the key-pair
	 * associated to the new instance.
	 */
	public static final String PASSPHRASE_ATTR = "passphrase";

	/**
	 * Task's attribute, which specifies the location of the key-pair associated
	 * to the new instance.
	 */
	public static final String KEYPAIR_REPO_ATTR = "keypair-repository";

	private InstanceType _instanceType = null;
	private String _imageId = null;
	private KeyPairRepositoryPath _keyPairRepository = null;
	private KeyPairName _keyPairName = null;
	private String _passphrase = null;
	private String _availabilityZone = null;

	public NewMachine() {
		super();
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
			Element n = getTargetElement();
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
						Common.INSTANCETYPE_ATTR, getTargetElementLocation()),
						Ex);
			}

			v = XPathHelper.getHeritedAttributeValue(n, Common.IMAGEID_ATTR);
			try {
				if (v != null) {
					setImageId(v);
				}
			} catch (AwsException Ex) {
				throw new AwsException(Messages.bind(
						Messages.NewEx_IMAGEID_ERROR, Common.IMAGEID_ATTR,
						getTargetElementLocation()), Ex);
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
						Common.AVAILABILITYZONE_ATTR,
						getTargetElementLocation()), Ex);
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
						Common.KEYPAIR_NAME_ATTR, getTargetElementLocation()),
						Ex);
			}

			v = XPathHelper.getHeritedAttributeValue(n, Common.PASSPHRASE_ATTR);
			if (v != null) {
				setPassphrase(v);
			}
		} catch (NodeRelatedException Ex) {
			throw new AwsException(Ex);
		}

		// Get the default KeyPair Repository, if not provided.
		if (getKeyPairRepositoryPath() == null) {
			setKeyPairRepositoryPath(getSshPlugInConfiguration()
					.getKeyPairRepositoryPath());
		}
		// Validate everything is provided.
		if (getInstanceType() == null) {
			throw new AwsException(Messages.bind(
					Messages.NewEx_MISSING_INSTANCETYPE_ATTR,
					NewMachine.INSTANCETYPE_ATTR, NewMachine.NEW_MACHINE,
					Common.INSTANCETYPE_ATTR, getTargetElementLocation()));
		}

		if (getImageId() == null) {
			throw new AwsException(Messages.bind(
					Messages.NewEx_MISSING_IMAGEID_ATTR,
					NewMachine.IMAGEID_ATTR, NewMachine.NEW_MACHINE,
					Common.IMAGEID_ATTR, getTargetElementLocation()));
		}

		if (getKeyPairName() == null) {
			throw new AwsException(Messages.bind(
					Messages.NewEx_MISSING_KEYPAIR_NAME_ATTR,
					NewMachine.KEYPAIR_NAME_ATTR, NewMachine.NEW_MACHINE,
					Common.KEYPAIR_NAME_ATTR, getTargetElementLocation()));
		}

		// Validate task's attributes
		// AZ must be validated AFTER the Aws Region is known
		// AZ can be null
		if (getAvailabilityZoneFullName() != null
				&& !AwsEc2Cloud.availabilityZoneExists(getEc2(),
						getAvailabilityZoneFullName())) {
			throw new AwsException(Messages.bind(
					Messages.NewEx_INVALID_AVAILABILITYZONE_ATTR,
					getAvailabilityZone(), getRegion()));
		}
		// imageId must be validated AFTER the Aws Region is known
		if (!AwsEc2Cloud.imageIdExists(getEc2(), getImageId())) {
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
					getAvailabilityZoneFullName(), getTargetElementLocation()),
					Ex);
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
					kpr.createKeyPair(getKeyPairName(),
							getSshPlugInConfiguration().getKeyPairSize(),
							getPassphrase());
				} catch (AwsException | IOException
						| AwsKeyPairRepositoryException Ex) {
					throw new OperationException(Ex);
				}

				return super.createInstance(type, site, imageId, keyPairName,
						createTimeout);
			}

		};
	}

	public InstanceType getInstanceType() {
		return _instanceType;
	}

	@Attribute(name = INSTANCETYPE_ATTR)
	public InstanceType setInstanceType(InstanceType instanceType) {
		if (instanceType == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid InstanceType (Accepted values are "
					+ Arrays.asList(InstanceType.values()) + ").");
		}
		InstanceType previous = getInstanceType();
		_instanceType = instanceType;
		return previous;
	}

	public String getImageId() {
		return _imageId;
	}

	@Attribute(name = IMAGEID_ATTR)
	public String setImageId(String imageId) throws AwsException {
		if (imageId == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (an AWS AMI Id).");
		}
		String previous = getImageId();
		_imageId = imageId;
		return previous;
	}

	public String getAvailabilityZone() {
		return _availabilityZone;
	}

	@Attribute(name = AVAILABILITYZONE_ATTR)
	public String setAvailabilityZone(String sAZ) throws AwsException {
		if (sAZ == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (an AWS Availability Zone).");
		}
		String previous = getAvailabilityZone();
		_availabilityZone = sAZ;
		return previous;
	}

	public String getAvailabilityZoneFullName() {
		if (getAvailabilityZone() == null) {
			return null;
		}
		return getRegion() + getAvailabilityZone();
	}

	public KeyPairName getKeyPairName() {
		return _keyPairName;
	}

	@Attribute(name = KEYPAIR_NAME_ATTR)
	public KeyPairName setKeyPairName(KeyPairName keyPairName) {
		if (keyPairName == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (an AWS KeyPair Name).");
		}
		KeyPairName previous = getKeyPairName();
		_keyPairName = keyPairName;
		return previous;
	}

	public String getPassphrase() {
		return _passphrase;
	}

	@Attribute(name = PASSPHRASE_ATTR)
	public String setPassphrase(String sPassphrase) {
		if (sPassphrase == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Cannot be null.");
		}
		String previous = getPassphrase();
		_passphrase = sPassphrase;
		return previous;
	}

	public KeyPairRepositoryPath getKeyPairRepositoryPath() {
		return _keyPairRepository;
	}

	@Attribute(name = KEYPAIR_REPO_ATTR)
	public KeyPairRepositoryPath setKeyPairRepositoryPath(
			KeyPairRepositoryPath keyPairRepository) {
		if (keyPairRepository == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid File (a Key Repository Path).");
		}
		KeyPairRepositoryPath previous = getKeyPairRepositoryPath();
		_keyPairRepository = keyPairRepository;
		return previous;
	}

}
