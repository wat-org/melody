package com.wat.melody.plugin.libvirt.common;

import javax.xml.xpath.XPathExpressionException;

import org.libvirt.Connect;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.wat.cloud.libvirt.LibVirtCloud;
import com.wat.cloud.libvirt.LibVirtInstanceController;
import com.wat.melody.api.IResourcesDescriptor;
import com.wat.melody.api.ITask;
import com.wat.melody.api.Melody;
import com.wat.melody.api.annotation.Attribute;
import com.wat.melody.api.exception.PlugInConfigurationException;
import com.wat.melody.cloud.instance.InstanceController;
import com.wat.melody.cloud.instance.InstanceControllerWithNetworkManagement;
import com.wat.melody.cloud.instance.InstanceControllerWithRelatedNode;
import com.wat.melody.cloud.instance.InstanceDatas;
import com.wat.melody.cloud.instance.InstanceDatasLoader;
import com.wat.melody.cloud.instance.InstanceDatasValidator;
import com.wat.melody.cloud.instance.InstanceType;
import com.wat.melody.cloud.instance.exception.IllegalInstanceDatasException;
import com.wat.melody.cloud.network.NetworkManagementHelper;
import com.wat.melody.cloud.network.NetworkManagerFactoryConfigurationCallback;
import com.wat.melody.common.keypair.KeyPairName;
import com.wat.melody.common.keypair.KeyPairRepositoryPath;
import com.wat.melody.common.keypair.KeyPairSize;
import com.wat.melody.common.timeout.GenericTimeout;
import com.wat.melody.common.timeout.exception.IllegalTimeoutException;
import com.wat.melody.common.xml.DocHelper;
import com.wat.melody.common.xml.exception.NodeRelatedException;
import com.wat.melody.plugin.libvirt.common.exception.LibVirtException;
import com.wat.melody.plugin.ssh.common.SshPlugInConfiguration;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class AbstractOperation implements ITask,
		InstanceDatasValidator, NetworkManagerFactoryConfigurationCallback {

	private static GenericTimeout createTimeout(int timeout) {
		try {
			return GenericTimeout.parseLong(timeout);
		} catch (IllegalTimeoutException Ex) {
			throw new RuntimeException("Unexpected error while initializing "
					+ "a GenricTimeout with value '" + timeout + "'. "
					+ "Because this default value initialization is "
					+ "hardcoded, such error cannot happened. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.", Ex);
		}
	}

	/**
	 * Task's attribute, which specifies the region where the instance is.
	 */
	public static final String REGION_ATTR = "region";

	/**
	 * Task's attribute, which specifies the {@link Element} which contains the
	 * instance description.
	 */
	public static final String TARGET_ATTR = "target";

	/**
	 * Task's attribute, which specifies the operation timeout.
	 */
	public static final String TIMEOUT_ATTR = "timeout";

	private String _target = null;
	private Element _targetElement = null;
	private String _instanceId = null;
	private InstanceDatas _instanceDatas = null;
	private Connect _cloudConnection = null;
	private InstanceController _instance = null;
	private GenericTimeout _defaultTimeout;

	public AbstractOperation() {
		setDefaultTimeout(createTimeout(90000));
	}

	@Override
	public void validate() throws LibVirtException {
		// Build an InstanceDatas with target Element's datas found in the RD
		try {
			setInstanceDatas(new InstanceDatasLoader().load(getTargetElement(),
					this));
		} catch (NodeRelatedException Ex) {
			throw new LibVirtException(Ex);
		}

		// Initialize Cloud Connection
		setCloudConnection(getLibVirtPlugInConfiguration().getCloudConnection(
				getInstanceDatas().getRegion()));

		setInstance(createInstance());
	}

	public InstanceController createInstance() throws LibVirtException {
		InstanceController instance = newLibVirtInstanceController();
		instance = new InstanceControllerWithRelatedNode(instance,
				getTargetElement());
		if (NetworkManagementHelper
				.isManagementNetworkEnable(getTargetElement())) {
			instance = new InstanceControllerWithNetworkManagement(instance,
					this, getTargetElement());
		}
		return instance;
	}

	/**
	 * Can be override by subclasses to provide enhanced behavior of the
	 * {@link AwsInstanceController}.
	 */
	public InstanceController newLibVirtInstanceController() {
		return new LibVirtInstanceController(getCloudConnection(),
				getInstanceId());
	}

	public IResourcesDescriptor getRD() {
		return Melody.getContext().getProcessorManager()
				.getResourcesDescriptor();
	}

	public LibVirtPlugInConfiguration getLibVirtPlugInConfiguration()
			throws LibVirtException {
		try {
			return LibVirtPlugInConfiguration.get(Melody.getContext()
					.getProcessorManager());
		} catch (PlugInConfigurationException Ex) {
			throw new LibVirtException(Ex);
		}
	}

	public SshPlugInConfiguration getSshPlugInConfiguration()
			throws LibVirtException {
		try {
			return SshPlugInConfiguration.get(Melody.getContext()
					.getProcessorManager());
		} catch (PlugInConfigurationException Ex) {
			throw new LibVirtException(Ex);
		}
	}

	@Override
	public SshPlugInConfiguration getSshConfiguration() {
		try {
			return getSshPlugInConfiguration();
		} catch (LibVirtException Ex) {
			throw new RuntimeException("Unexpected error when retrieving Ssh "
					+ " Plug-In configuration. "
					+ "Because such configuration registration have been "
					+ "previously prouved, such error cannot happened.");
		}
	}

	@Override
	public String validateRegion(InstanceDatas datas, String region)
			throws IllegalInstanceDatasException {
		if (region == null) {
			throw new IllegalInstanceDatasException(Messages.bind(
					Messages.MachineEx_MISSING_REGION_ATTR,
					InstanceDatasLoader.REGION_ATTR));
		}
		LibVirtPlugInConfiguration conf;
		try {
			conf = getLibVirtPlugInConfiguration();
		} catch (LibVirtException Ex) {
			throw new IllegalInstanceDatasException(Ex);
		}
		if (conf.getCloudConnection(region) == null) {
			throw new IllegalInstanceDatasException(Messages.bind(
					Messages.MachineEx_INVALID_REGION_ATTR, region));
		}
		return region;
	}

	public String validateSite(InstanceDatas datas, String site)
			throws IllegalInstanceDatasException {
		// can be null
		return site;
	}

	@Override
	public String validateImageId(InstanceDatas datas, String imageId)
			throws IllegalInstanceDatasException {
		if (imageId == null) {
			throw new IllegalInstanceDatasException(Messages.bind(
					Messages.MachineEx_MISSING_IMAGEID_ATTR,
					InstanceDatasLoader.IMAGEID_ATTR));
		}
		if (!LibVirtCloud.imageIdExists(imageId)) {
			throw new IllegalInstanceDatasException(Messages.bind(
					Messages.MachineEx_INVALID_IMAGEID_ATTR, imageId,
					datas.getRegion()));
		}
		return imageId;
	}

	@Override
	public InstanceType validateInstanceType(InstanceDatas datas,
			InstanceType instanceType) throws IllegalInstanceDatasException {
		if (instanceType != null) {
			return instanceType;
		}
		throw new IllegalInstanceDatasException(Messages.bind(
				Messages.MachineEx_MISSING_INSTANCETYPE_ATTR,
				InstanceDatasLoader.INSTANCETYPE_ATTR));
	}

	@Override
	public KeyPairRepositoryPath validateKeyPairRepositoryPath(
			InstanceDatas datas, KeyPairRepositoryPath keyPairRepositoryPath)
			throws IllegalInstanceDatasException {
		if (keyPairRepositoryPath != null) {
			return keyPairRepositoryPath;
		}
		try {
			// Get the default value
			return getSshPlugInConfiguration().getKeyPairRepositoryPath();
		} catch (LibVirtException Ex) {
			throw new IllegalInstanceDatasException(Ex);
		}
	}

	@Override
	public KeyPairName validateKeyPairName(InstanceDatas datas,
			KeyPairName keyPairName) throws IllegalInstanceDatasException {
		if (keyPairName != null) {
			return keyPairName;
		}
		throw new IllegalInstanceDatasException(Messages.bind(
				Messages.MachineEx_MISSING_KEYPAIR_NAME_ATTR,
				InstanceDatasLoader.KEYPAIR_NAME_ATTR));
	}

	@Override
	public String validatePassphrase(InstanceDatas datas, String passphrase)
			throws IllegalInstanceDatasException {
		// can be null
		return passphrase;
	}

	@Override
	public KeyPairSize validateKeyPairSize(InstanceDatas datas,
			KeyPairSize keyPairSize) throws IllegalInstanceDatasException {
		if (keyPairSize != null) {
			return keyPairSize;
		}
		try {
			// Get the default value
			return getSshPlugInConfiguration().getKeyPairSize();
		} catch (LibVirtException Ex) {
			throw new IllegalInstanceDatasException(Ex);
		}
	}

	@Override
	public GenericTimeout validateCreateTimeout(InstanceDatas datas,
			GenericTimeout createTimeout) throws IllegalInstanceDatasException {
		if (createTimeout != null) {
			return createTimeout;
		}
		return getDefaultTimeout();
	}

	@Override
	public GenericTimeout validateDeleteTimeout(InstanceDatas datas,
			GenericTimeout destroyTimeout) throws IllegalInstanceDatasException {
		if (destroyTimeout != null) {
			return destroyTimeout;
		}
		try {
			return GenericTimeout.parseLong(getDefaultTimeout()
					.getTimeoutInMillis() * 2);
		} catch (IllegalTimeoutException Ex) {
			throw new RuntimeException("Unexpected error while initializing "
					+ "the 'timeout-delete' to "
					+ (getDefaultTimeout().getTimeoutInMillis() * 2) + ". "
					+ "Because this value is hardocded, suche error cannot "
					+ "happened. "
					+ "Source code has certainly been modified and a bug "
					+ "have been introduced.", Ex);
		}
	}

	@Override
	public GenericTimeout validateStartTimeout(InstanceDatas datas,
			GenericTimeout startTimeout) throws IllegalInstanceDatasException {
		if (startTimeout == null) {
			return getDefaultTimeout();
		}
		return startTimeout;
	}

	@Override
	public GenericTimeout validateStopTimeout(InstanceDatas datas,
			GenericTimeout stopTimeout) throws IllegalInstanceDatasException {
		if (stopTimeout == null) {
			return getDefaultTimeout();
		}
		return stopTimeout;
	}

	public InstanceDatas getInstanceDatas() {
		return _instanceDatas;
	}

	private InstanceDatas setInstanceDatas(InstanceDatas instanceDatas) {
		if (instanceDatas == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ InstanceDatas.class.getCanonicalName() + ".");
		}
		InstanceDatas previous = getInstanceDatas();
		_instanceDatas = instanceDatas;
		return previous;
	}

	public GenericTimeout getDefaultTimeout() {
		return _defaultTimeout;
	}

	private GenericTimeout setDefaultTimeout(GenericTimeout timeout) {
		if (timeout == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ GenericTimeout.class.getCanonicalName() + ".");
		}
		GenericTimeout previous = getDefaultTimeout();
		_defaultTimeout = timeout;
		return previous;
	}

	protected Connect getCloudConnection() {
		return _cloudConnection;
	}

	private Connect setCloudConnection(Connect cnx) {
		if (cnx == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Connect.class.getCanonicalName()
					+ ".");
		}
		Connect previous = getCloudConnection();
		_cloudConnection = cnx;
		return previous;
	}

	public InstanceController getInstance() {
		return _instance;
	}

	public InstanceController setInstance(InstanceController instance) {
		if (instance == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ InstanceController.class.getCanonicalName() + ".");
		}
		InstanceController previous = getInstance();
		_instance = instance;
		return previous;
	}

	/**
	 * @return the targeted {@link Element}.
	 */
	public Element getTargetElement() {
		return _targetElement;
	}

	public Element setTargetElement(Element n) {
		if (n == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Element.class.getCanonicalName()
					+ " (the targeted LibVirt Instance Element Node).");
		}
		Element previous = getTargetElement();
		_targetElement = n;
		return previous;
	}

	/**
	 * @return the Instance Id which is registered in the targeted Element Node
	 *         (can be <code>null</code>).
	 */
	protected String getInstanceId() {
		return _instanceId;
	}

	protected String setInstanceId(String sInstanceID) {
		// can be null, if no Instance have been created yet
		String previous = getInstanceId();
		_instanceId = sInstanceID;
		return previous;
	}

	/**
	 * @return the XPath expression which selects the targeted Node.
	 */
	public String getTarget() {
		return _target;
	}

	@Attribute(name = TARGET_ATTR, mandatory = true)
	public String setTarget(String target) throws LibVirtException {
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
			throw new LibVirtException(Messages.bind(
					Messages.MachineEx_INVALID_TARGET_ATTR_NOT_XPATH, target));
		}
		if (nl.getLength() == 0) {
			throw new LibVirtException(Messages.bind(
					Messages.MachineEx_INVALID_TARGET_ATTR_NO_NODE_MATCH,
					target));
		} else if (nl.getLength() > 1) {
			throw new LibVirtException(Messages.bind(
					Messages.MachineEx_INVALID_TARGET_ATTR_MANY_NODES_MATCH,
					target, nl.getLength()));
		}
		Node n = nl.item(0);
		if (n.getNodeType() != Node.ELEMENT_NODE) {
			throw new LibVirtException(Messages.bind(
					Messages.MachineEx_INVALID_TARGET_ATTR_NOT_ELMT_MATCH,
					target, DocHelper.parseNodeType(n)));
		}
		setTargetElement((Element) n);
		try {
			setInstanceId(n.getAttributes()
					.getNamedItem(InstanceDatasLoader.INSTANCE_ID_ATTR)
					.getNodeValue());
		} catch (NullPointerException ignored) {
		}
		String previous = getTarget();
		_target = target;
		return previous;
	}

}