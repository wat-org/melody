package com.wat.melody.plugin.aws.ec2.common;

import java.io.IOException;
import java.security.KeyPair;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.amazonaws.services.ec2.AmazonEC2;
import com.wat.melody.api.IResourcesDescriptor;
import com.wat.melody.api.ITask;
import com.wat.melody.api.Melody;
import com.wat.melody.api.annotation.Attribute;
import com.wat.melody.api.exception.PlugInConfigurationException;
import com.wat.melody.api.exception.ResourcesDescriptorException;
import com.wat.melody.cloud.instance.InstanceController;
import com.wat.melody.cloud.instance.InstanceControllerWithNetworkManagement;
import com.wat.melody.cloud.instance.InstanceControllerWithRelatedNode;
import com.wat.melody.cloud.instance.InstanceDatasLoader;
import com.wat.melody.cloud.instance.InstanceType;
import com.wat.melody.cloud.instance.exception.OperationException;
import com.wat.melody.cloud.network.NetworkManagementHelper;
import com.wat.melody.cloud.network.NetworkManagerFactoryConfigurationCallback;
import com.wat.melody.common.keypair.KeyPairName;
import com.wat.melody.common.keypair.KeyPairRepository;
import com.wat.melody.common.keypair.KeyPairRepositoryPath;
import com.wat.melody.common.xml.Doc;
import com.wat.melody.plugin.aws.ec2.common.exception.AwsException;
import com.wat.melody.plugin.ssh.common.SshPlugInConfiguration;
import com.wat.melody.xpathextensions.XPathExpander;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
abstract public class AbstractOperation implements ITask,
		NetworkManagerFactoryConfigurationCallback {

	/**
	 * The 'region' XML attribute
	 */
	public static final String REGION_ATTR = "region";

	/**
	 * The 'target' XML attribute
	 */
	public static final String TARGET_ATTR = "target";

	/**
	 * The 'timeout' XML attribute
	 */
	public static final String TIMEOUT_ATTR = "timeout";

	private AmazonEC2 moEC2;
	private InstanceController moInstance;
	private String msInstanceId;
	private Node moTargetNode;
	private String msRegion;
	private String msTarget;
	private long mlTimeout;

	public AbstractOperation() {
		initEC2();
		initInstance();
		initTargetNode();
		initInstanceId();
		initRegion();
		initTimeout();
	}

	private void initEC2() {
		moEC2 = null;
	}

	private void initInstance() {
		moInstance = null;
	}

	private void initTargetNode() {
		moTargetNode = null;
	}

	private void initInstanceId() {
		msInstanceId = null;
	}

	private void initRegion() {
		msRegion = null;
	}

	private void initTimeout() {
		mlTimeout = 90000;
	}

	@Override
	public void validate() throws AwsException {
		// Initialize task parameters with their default value
		String v = null;
		try {
			v = XPathExpander.getHeritedAttributeValue(getTargetNode(),
					Common.REGION_ATTR);
		} catch (ResourcesDescriptorException Ex) {
			throw new AwsException(Ex);
		}
		try {
			if (v != null) {
				setRegion(v);
			}
		} catch (AwsException Ex) {
			throw new AwsException(Messages.bind(
					Messages.MachineEx_REGION_ERROR, Common.REGION_ATTR,
					getTargetNodeLocation()), Ex);
		}

		// Is everything correctly loaded ?
		if (getRegion() == null) {
			throw new AwsException(Messages.bind(
					Messages.MachineEx_MISSING_REGION_ATTR, new Object[] {
							REGION_ATTR,
							getClass().getSimpleName().toLowerCase(),
							Common.REGION_ATTR, getTargetNodeLocation() }));
		}

		// Initialize AmazonEC2 for the current region
		setEc2(getPluginConf().getAmazonEC2(getRegion()));

		setInstance(createInstance());
	}

	public InstanceController createInstance() throws AwsException {
		try {
			InstanceController instance = new AwsInstanceController(getEc2(),
					getInstanceId());
			instance = new InstanceControllerWithRelatedNode(instance, getRD(),
					getTargetNode());
			if (NetworkManagementHelper
					.isManagementNetworkEnable(getTargetNode())) {
				instance = new InstanceControllerWithNetworkManagement(
						instance, this, getTargetNode());
			}
			return instance;
		} catch (OperationException Ex) {
			throw new AwsException(Ex);
		}
	}

	/**
	 * <p>
	 * Enable the given KeyPair in Aws. More formally, this will :
	 * <ul>
	 * <li>Create a new {@link KeyPair} and store it in the given local
	 * {@link KeyPairRepository} in openSSH RSA format if the {@link KeyPair}
	 * can not be found the given local {@link KeyPairRepository} ;</li>
	 * <li>Import the public part of the given {@link KeyPair} in the Aws Region
	 * defined by {@link #getRegion()} if the {@link KeyPair} exists in the
	 * given local {@link KeyPairRepository} and doesn't exists in the given Aws
	 * Region ;</li>
	 * <li>Compare the public part of the given {@link KeyPair} with the public
	 * part of the Aws {@link com.amazonaws.services.ec2.model.KeyPair} if the
	 * {@link KeyPair} exists in the given local {@link KeyPairRepository} and
	 * also exists in the given Aws Region, and will throw an
	 * {@link AwsException} if they doesn't match ;</li>
	 * </ul>
	 * </p>
	 * 
	 * @param kprp
	 *            is the {@link KeyPairRepository}.
	 * @param keyPairName
	 *            is the name of the {@link KeyPair} to enable.
	 * @param iKeySize
	 *            is the size of the {@link KeyPair} to create (only apply if
	 *            the local {@link KeyPairRepository} doesn't contains the key
	 *            pair).
	 * @param sPassphrase
	 *            is the passphrase to associate to the {@link KeyPair} to
	 *            create (only apply if the local {@link KeyPairRepository}
	 *            doesn't contains the key pair).
	 * 
	 * @throws AwsException
	 *             if the {@link KeyPair} found in the local
	 *             {@link KeyPairRepository} is corrupted (ex : not a valid
	 *             OpenSSH RSA KeyPair) or if the {@link KeyPair} found in the
	 *             local {@link KeyPairRepository} is not equal to the Aws
	 *             {@link com.amazonaws.services.ec2.model.KeyPair}.
	 * @throws IOException
	 *             if an I/O error occurred while reading/storing the
	 *             {@link KeyPair} in the local {@link KeyPairRepository}.
	 */
	/*
	 * TODO : put this in AwsInstanceController
	 */
	public synchronized void enableKeyPair(KeyPairRepositoryPath kprp,
			KeyPairName keyPairName, int iKeySize, String sPassphrase)
			throws AwsException, IOException {
		KeyPairRepository kpr = KeyPairRepository.getKeyPairRepository(kprp);
		// Create KeyPair in the KeyPair Repository
		KeyPair kp = null;
		if (!kpr.containsKeyPair(keyPairName)) {
			kp = kpr.createKeyPair(keyPairName, iKeySize, sPassphrase);
		} else {
			kp = kpr.getKeyPair(keyPairName, sPassphrase);
		}

		// Create KeyPair in Aws
		if (Common.keyPairExists(getEc2(), keyPairName) == true) {
			String fingerprint = KeyPairRepository.getFingerprint(kp);
			if (Common.keyPairCompare(getEc2(), keyPairName, fingerprint) == false) {
				/*
				 * TODO : externalize error message
				 */
				throw new AwsException("Aws KeyPair and Local KeyPair doesn't "
						+ "match.");
			}
		} else {
			String pubkey = KeyPairRepository.getPublicKeyInOpenSshFormat(kp,
					"Generated by Melody");
			Common.importKeyPair(getEc2(), keyPairName, pubkey);
		}
	}

	public IResourcesDescriptor getRD() {
		return Melody.getContext().getProcessorManager()
				.getResourcesDescriptor();
	}

	public String getTargetNodeLocation() {
		return Doc.getNodeLocation(getTargetNode()).toFullString();
	}

	protected boolean resizeInstance(InstanceType instanceType) {
		return Common
				.resizeAwsInstance(getEc2(), getInstanceId(), instanceType);
	}

	protected AwsPlugInConfiguration getPluginConf() throws AwsException {
		try {
			return AwsPlugInConfiguration.get(Melody.getContext()
					.getProcessorManager());
		} catch (PlugInConfigurationException Ex) {
			throw new AwsException(Ex);
		}
	}

	public SshPlugInConfiguration getSshPlugInConf() throws AwsException {
		try {
			return SshPlugInConfiguration.get(Melody.getContext()
					.getProcessorManager());
		} catch (PlugInConfigurationException Ex) {
			throw new AwsException(Ex);
		}
	}

	@Override
	public SshPlugInConfiguration getSshConfiguration() {
		try {
			return getSshPlugInConf();
		} catch (AwsException Ex) {
			throw new RuntimeException("Unexpected error when retrieving Ssh "
					+ " Plug-In configuration. "
					+ "Because such configuration registration have been "
					+ "previously prouved, such error cannot happened.");
		}
	}

	protected AmazonEC2 getEc2() {
		return moEC2;
	}

	private AmazonEC2 setEc2(AmazonEC2 ec2) {
		if (ec2 == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid AmazonEC2.");
		}
		AmazonEC2 previous = getEc2();
		moEC2 = ec2;
		return previous;
	}

	public InstanceController getInstance() {
		return moInstance;
	}

	public InstanceController setInstance(InstanceController instance) {
		if (instance == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ InstanceController.class.getCanonicalName() + ".");
		}
		InstanceController previous = getInstance();
		moInstance = instance;
		return previous;
	}

	/**
	 * @return the targeted {@link Node}.
	 */
	public Node getTargetNode() {
		return moTargetNode;
	}

	public Node setTargetNode(Node n) {
		if (n == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid Node (the targeted AWS Instance Node).");
		}
		Node previous = getTargetNode();
		moTargetNode = n;
		return previous;
	}

	/**
	 * @return the Aws Instance Id which is registered in the targeted Node (can
	 *         be <code>null</code>).
	 */
	protected String getInstanceId() {
		return msInstanceId;
	}

	protected String setInstanceId(String instanceID) {
		// can be null, if no AWS instance have been created yet
		String previous = getInstanceId();
		msInstanceId = instanceID;
		return previous;
	}

	public String getRegion() {
		return msRegion;
	}

	@Attribute(name = REGION_ATTR)
	public String setRegion(String region) throws AwsException {
		if (region == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (an AWS Region Name).");
		}
		if (getPluginConf().getAmazonEC2(region) == null) {
			throw new AwsException(Messages.bind(
					Messages.MachineEx_INVALID_REGION_ATTR, region));
		}
		String previous = getRegion();
		this.msRegion = region;
		return previous;
	}

	/**
	 * @return the XPath expression which selects the targeted Node.
	 */
	public String getTarget() {
		return msTarget;
	}

	@Attribute(name = TARGET_ATTR, mandatory = true)
	public String setTarget(String target) throws AwsException {
		if (target == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (an XPath Expression, which "
					+ "selects a sole XML Element node in the Resources "
					+ "Descriptor.");
		}

		NodeList nl = null;
		try {
			nl = getRD().evaluateAsNodeList(target);
		} catch (XPathExpressionException Ex) {
			throw new AwsException(Messages.bind(
					Messages.MachineEx_INVALID_TARGET_ATTR_NOT_XPATH, target));
		}
		if (nl.getLength() == 0) {
			throw new AwsException(Messages.bind(
					Messages.MachineEx_INVALID_TARGET_ATTR_NO_NODE_MATCH,
					target));
		} else if (nl.getLength() > 1) {
			throw new AwsException(Messages.bind(
					Messages.MachineEx_INVALID_TARGET_ATTR_MANY_NODES_MATCH,
					target, nl.getLength()));
		}
		Node n = nl.item(0);
		if (n.getNodeType() != Node.ELEMENT_NODE) {
			throw new AwsException(Messages.bind(
					Messages.MachineEx_INVALID_TARGET_ATTR_NOT_ELMT_MATCH,
					target, Doc.parseInt(n.getNodeType())));
		}
		setTargetNode(n);
		try {
			setInstanceId(n.getAttributes()
					.getNamedItem(InstanceDatasLoader.INSTANCE_ID_ATTR)
					.getNodeValue());
		} catch (NullPointerException ignored) {
		}
		String previous = getTarget();
		msTarget = target;
		return previous;
	}

	public long getTimeout() {
		return mlTimeout;
	}

	@Attribute(name = TIMEOUT_ATTR)
	public long setTimeout(long timeout) throws AwsException {
		if (timeout < 0) {
			throw new AwsException(Messages.bind(
					Messages.MachineEx_INVALID_TIMEOUT_ATTR, timeout));
		}
		long previous = getTimeout();
		mlTimeout = timeout;
		return previous;
	}

}