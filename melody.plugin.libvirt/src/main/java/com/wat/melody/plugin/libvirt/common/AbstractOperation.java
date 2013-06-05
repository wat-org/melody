package com.wat.melody.plugin.libvirt.common;

import javax.xml.xpath.XPathExpressionException;

import org.libvirt.Connect;
import org.libvirt.LibvirtException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.wat.cloud.libvirt.LibVirtInstanceController;
import com.wat.melody.api.IResourcesDescriptor;
import com.wat.melody.api.ITask;
import com.wat.melody.api.Melody;
import com.wat.melody.api.annotation.Attribute;
import com.wat.melody.api.exception.PlugInConfigurationException;
import com.wat.melody.cloud.instance.InstanceController;
import com.wat.melody.cloud.instance.InstanceControllerWithNetworkManagement;
import com.wat.melody.cloud.instance.InstanceControllerWithRelatedNode;
import com.wat.melody.cloud.instance.InstanceDatasLoader;
import com.wat.melody.cloud.network.NetworkManagementHelper;
import com.wat.melody.cloud.network.NetworkManagerFactoryConfigurationCallback;
import com.wat.melody.common.xml.DocHelper;
import com.wat.melody.common.xml.exception.NodeRelatedException;
import com.wat.melody.plugin.libvirt.common.exception.LibVirtException;
import com.wat.melody.plugin.ssh.common.SshPlugInConfiguration;
import com.wat.melody.xpathextensions.XPathHelper;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class AbstractOperation implements ITask,
		NetworkManagerFactoryConfigurationCallback {

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

	private Connect _connect = null;
	private InstanceController _instance = null;
	private String _instanceId = null;
	private Element _targetElement = null;
	private String _region = null;
	private String _target = null;
	private long _timeout = 90000;

	public AbstractOperation() {
	}

	@Override
	public void validate() throws LibVirtException {
		// Initialize task parameters with their default value
		String v = null;
		try {
			v = XPathHelper.getHeritedAttributeValue(getTargetElement(),
					Common.REGION_ATTR);
		} catch (NodeRelatedException Ex) {
			throw new LibVirtException(Ex);
		}
		try {
			if (v != null) {
				setRegion(v);
			}
		} catch (LibVirtException Ex) {
			throw new LibVirtException(Messages.bind(
					Messages.MachineEx_REGION_ERROR, Common.REGION_ATTR,
					getTargetElementLocation()), Ex);
		}

		// Is everything correctly loaded ?
		if (getRegion() == null) {
			throw new LibVirtException(Messages.bind(
					Messages.MachineEx_MISSING_REGION_ATTR, REGION_ATTR,
					getClass().getSimpleName().toLowerCase(),
					Common.REGION_ATTR, getTargetElementLocation()));
		}

		// Keep the Connection in a dedicated member
		try {
			// TODO : put the new Connect into another place
			setConnect(new Connect(getRegion(), false));
		} catch (LibvirtException Ex) {
			throw new LibVirtException(Ex);
		}

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
		return new LibVirtInstanceController(getConnect(), getInstanceId());
	}

	public IResourcesDescriptor getRD() {
		return Melody.getContext().getProcessorManager()
				.getResourcesDescriptor();
	}

	public String getTargetElementLocation() {
		return DocHelper.getNodeLocation(getTargetElement()).toFullString();
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

	protected Connect getConnect() {
		return _connect;
	}

	private Connect setConnect(Connect cnx) {
		if (cnx == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Connect.class.getCanonicalName()
					+ ".");
		}
		Connect previous = getConnect();
		_connect = cnx;
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
	 * @return the targeted {@link Node}.
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
	 * @return the Instance Id which is registered in the targeted Node (can be
	 *         <code>null</code>).
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

	public String getRegion() {
		return _region;
	}

	@Attribute(name = REGION_ATTR)
	public String setRegion(String region) throws LibVirtException {
		if (region == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a libvirt connection URI).");
		}
		// TODO : how to validate the region? by testing the connection ?
		String previous = getRegion();
		this._region = region;
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

	public long getTimeout() {
		return _timeout;
	}

	@Attribute(name = TIMEOUT_ATTR)
	public long setTimeout(long timeout) throws LibVirtException {
		if (timeout < 0) {
			throw new LibVirtException(Messages.bind(
					Messages.MachineEx_INVALID_TIMEOUT_ATTR, timeout));
		}
		long previous = getTimeout();
		_timeout = timeout;
		return previous;
	}

}