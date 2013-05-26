package com.wat.melody.plugin.libvirt;

import java.io.IOException;
import java.util.Arrays;

import org.w3c.dom.Element;

import com.wat.cloud.libvirt.LibVirtCloud;
import com.wat.cloud.libvirt.LibVirtInstanceController;
import com.wat.cloud.libvirt.LibVirtKeyPairRepository;
import com.wat.cloud.libvirt.exception.LibVirtKeyPairRepositoryException;
import com.wat.melody.api.Melody;
import com.wat.melody.api.annotation.Attribute;
import com.wat.melody.cloud.instance.InstanceController;
import com.wat.melody.cloud.instance.InstanceType;
import com.wat.melody.cloud.instance.exception.IllegalInstanceTypeException;
import com.wat.melody.cloud.instance.exception.OperationException;
import com.wat.melody.common.keypair.KeyPairName;
import com.wat.melody.common.keypair.KeyPairRepositoryPath;
import com.wat.melody.common.keypair.exception.IllegalKeyPairNameException;
import com.wat.melody.common.xml.exception.NodeRelatedException;
import com.wat.melody.plugin.libvirt.common.AbstractOperation;
import com.wat.melody.plugin.libvirt.common.Common;
import com.wat.melody.plugin.libvirt.common.Messages;
import com.wat.melody.plugin.libvirt.common.exception.LibVirtException;
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

	/*
	 * TODO : remove all these task attribute, and do a InstanceLoader into
	 * package 'cloud'
	 */
	/*
	 * TODO : create XPathFunciton to query for instance attributes
	 */
	/**
	 * The 'instanceType' XML attribute
	 */
	public static final String INSTANCETYPE_ATTR = "instanceType";

	/**
	 * The 'imageId' XML attribute
	 */
	public static final String IMAGEID_ATTR = "imageId";

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

	private InstanceType _instanceType = null;
	private String _imageId = null;
	private KeyPairRepositoryPath _keyPairRepositoryPath = null;
	private KeyPairName _keyPairName = null;
	private String _passphrase = null;

	public NewMachine() {
		super();
	}

	@Override
	public void validate() throws LibVirtException {
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
				throw new LibVirtException(Messages.bind(
						Messages.NewEx_INSTANCETYPE_ERROR,
						Common.INSTANCETYPE_ATTR, getTargetElementLocation()),
						Ex);
			}

			v = XPathHelper.getHeritedAttributeValue(n, Common.IMAGEID_ATTR);
			try {
				if (v != null) {
					setImageId(v);
				}
			} catch (LibVirtException Ex) {
				throw new LibVirtException(Messages.bind(
						Messages.NewEx_IMAGEID_ERROR, Common.IMAGEID_ATTR,
						getTargetElementLocation()), Ex);
			}

			v = XPathHelper.getHeritedAttributeValue(n,
					Common.KEYPAIR_NAME_ATTR);
			try {
				if (v != null) {
					setKeyPairName(KeyPairName.parseString(v));
				}
			} catch (IllegalKeyPairNameException Ex) {
				throw new LibVirtException(Messages.bind(
						Messages.NewEx_KEYPAIR_NAME_ERROR,
						Common.KEYPAIR_NAME_ATTR, getTargetElementLocation()),
						Ex);
			}

			v = XPathHelper.getHeritedAttributeValue(n, Common.PASSPHRASE_ATTR);
			if (v != null) {
				setPassphrase(v);
			}
		} catch (NodeRelatedException Ex) {
			throw new LibVirtException(Ex);
		}

		// Get the default KeyPair Repository, if not provided.
		if (getKeyPairRepositoryPath() == null) {
			setKeyPairRepositoryPath(getSshPlugInConfiguration()
					.getKeyPairRepositoryPath());
		}
		// Validate everything is provided.
		if (getInstanceType() == null) {
			throw new LibVirtException(Messages.bind(
					Messages.NewEx_MISSING_INSTANCETYPE_ATTR, new Object[] {
							NewMachine.INSTANCETYPE_ATTR,
							NewMachine.NEW_MACHINE, Common.INSTANCETYPE_ATTR,
							getTargetElementLocation() }));
		}

		if (getImageId() == null) {
			throw new LibVirtException(Messages.bind(
					Messages.NewEx_MISSING_IMAGEID_ATTR, new Object[] {
							NewMachine.IMAGEID_ATTR, NewMachine.NEW_MACHINE,
							Common.IMAGEID_ATTR, getTargetElementLocation() }));
		}

		if (getKeyPairName() == null) {
			throw new LibVirtException(Messages.bind(
					Messages.NewEx_MISSING_KEYPAIR_NAME_ATTR, new Object[] {
							NewMachine.KEYPAIR_NAME_ATTR,
							NewMachine.NEW_MACHINE, Common.KEYPAIR_NAME_ATTR,
							getTargetElementLocation() }));
		}

		// Validate task's attributes
		// imageId must be validated AFTER the libvirt Region is known
		if (!LibVirtCloud.imageIdExists(getImageId())) {
			throw new LibVirtException(Messages.bind(
					Messages.NewEx_INVALID_IMAGEID_ATTR, getImageId(),
					getRegion()));
		}
	}

	@Override
	public void doProcessing() throws LibVirtException, InterruptedException {
		Melody.getContext().handleProcessorStateUpdates();

		try {
			getInstance().ensureInstanceIsCreated(getInstanceType(), null,
					getImageId(), getKeyPairName(), getTimeout());
		} catch (OperationException Ex) {
			throw new LibVirtException(Messages.bind(
					Messages.CreateEx_GENERIC_FAIL, new Object[] { getRegion(),
							getImageId(), getInstanceType(), getKeyPairName(),
							null, getTargetElementLocation() }), Ex);
		}
	}

	/**
	 * @return an {@link LibVirtInstanceController} which provides additional
	 *         KeyPair Management features.
	 */
	@Override
	public InstanceController newLibVirtInstanceController() {
		// create LibVirtInstanceControllerWithKeyPairManagement class ?
		return new LibVirtInstanceController(getConnect(), getInstanceId()) {

			public String createInstance(InstanceType type, String site,
					String imageId, KeyPairName keyPairName, long createTimeout)
					throws OperationException, InterruptedException {
				try {
					LibVirtKeyPairRepository kpr = LibVirtKeyPairRepository
							.getLibVirtKeyPairRepository(getConnection(),
									getKeyPairRepositoryPath());
					kpr.createKeyPair(getKeyPairName(),
							getSshPlugInConfiguration().getKeyPairSize(),
							getPassphrase());
				} catch (LibVirtException | IOException
						| LibVirtKeyPairRepositoryException Ex) {
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
	public String setImageId(String imageId) throws LibVirtException {
		if (imageId == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (an libvirt AMI Id).");
		}
		String previous = getImageId();
		_imageId = imageId;
		return previous;
	}

	public KeyPairName getKeyPairName() {
		return _keyPairName;
	}

	@Attribute(name = KEYPAIR_NAME_ATTR)
	public KeyPairName setKeyPairName(KeyPairName keyPairName) {
		if (keyPairName == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (an libvirt KeyPair Name).");
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
		return _keyPairRepositoryPath;
	}

	@Attribute(name = KEYPAIR_REPO_ATTR)
	public KeyPairRepositoryPath setKeyPairRepositoryPath(
			KeyPairRepositoryPath keyPairRepository) {
		if (keyPairRepository == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid File (a Key Repository Path).");
		}
		KeyPairRepositoryPath previous = getKeyPairRepositoryPath();
		_keyPairRepositoryPath = keyPairRepository;
		return previous;
	}

}
