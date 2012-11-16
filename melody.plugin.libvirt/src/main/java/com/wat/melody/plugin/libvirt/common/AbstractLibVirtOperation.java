package com.wat.melody.plugin.libvirt.common;

import javax.xml.xpath.XPathExpressionException;

import org.libvirt.Connect;
import org.libvirt.LibvirtException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.wat.cloud.libvirt.Instance;
import com.wat.cloud.libvirt.LibVirtCloud;
import com.wat.melody.api.IResourcesDescriptor;
import com.wat.melody.api.ITask;
import com.wat.melody.api.ITaskContext;
import com.wat.melody.api.annotation.Attribute;
import com.wat.melody.common.network.Host;
import com.wat.melody.common.network.exception.IllegalHostException;
import com.wat.melody.common.utils.DUNID;
import com.wat.melody.common.utils.Doc;
import com.wat.melody.common.utils.exception.NoSuchDUNIDException;
import com.wat.melody.plugin.libvirt.common.exception.ConfigurationException;
import com.wat.melody.plugin.libvirt.common.exception.LibVirtException;
import com.wat.melody.plugin.ssh.common.exception.SshException;
import com.wat.melody.xpathextensions.GetHeritedAttribute;
import com.wat.melody.xpathextensions.common.exception.ResourcesDescriptorException;

public abstract class AbstractLibVirtOperation implements ITask {

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

	private ITaskContext moContext;
	private Configuration moPluginConf;
	private com.wat.melody.plugin.ssh.common.Configuration moSshPluginConf;
	private Connect moConnect;
	private String msInstanceID;
	private Node moTargetNode;
	private DUNID msMelodyId;
	private String msRegion;
	private String msTarget;
	private long mlTimeout;

	public AbstractLibVirtOperation() {
		initContext();
		initPluginConf();
		initCnx();
		initMelodyId();
		initTargetNode();
		initInstanceId();
		initRegion();
		initTimeout();
	}

	private void initContext() {
		moContext = null;
	}

	private void initPluginConf() {
		moPluginConf = null;
	}

	private void initCnx() {
		moConnect = null;
	}

	private void initMelodyId() {
		msMelodyId = null;
	}

	private void initTargetNode() {
		moTargetNode = null;
	}

	private void initInstanceId() {
		msInstanceID = null;
	}

	private void initRegion() {
		msRegion = null;
	}

	private void initTimeout() {
		mlTimeout = 90000;
	}

	@Override
	public void validate() throws LibVirtException {
		// Initialize task parameters with their default value
		String v = null;
		try {
			v = GetHeritedAttribute.getHeritedAttributeValue(getTargetNode(),
					Common.REGION_ATTR);
		} catch (ResourcesDescriptorException Ex) {
			throw new LibVirtException(Messages.bind(
					Messages.MachineEx_HERIT_ERROR, "",
					getED().getLocation(Ex.getErrorNode()).toFullString()), Ex);
		}
		try {
			if (v != null) {
				setRegion(v);
			}
		} catch (LibVirtException Ex) {
			throw new LibVirtException(Messages.bind(
					Messages.MachineEx_REGION_ERROR, Common.REGION_ATTR,
					getTargetNodeLocation()), Ex);
		}

		// Is everything correctly loaded ?
		if (getRegion() == null) {
			throw new LibVirtException(Messages.bind(
					Messages.MachineEx_MISSING_REGION_ATTR, new Object[] {
							REGION_ATTR,
							getClass().getSimpleName().toLowerCase(),
							Common.REGION_ATTR, getTargetNodeLocation() }));
		}

		// Keep the Connection in a dedicated membre
		try {
			// TODO : put the new Connect into another place
			setConnect(new Connect(getRegion(), false));
		} catch (LibvirtException Ex) {
			throw new LibVirtException(Ex);
		}
	}

	public IResourcesDescriptor getED() {
		return getContext().getProcessorManager().getResourcesDescriptor();
	}

	public String getTargetNodeLocation() {
		return getED().getLocation(getTargetNode()).toFullString();
	}

	public Instance getInstance() {
		return LibVirtCloud.getInstance(getConnect(), getInstanceID());
	}

	public InstanceState getInstanceState() {
		return LibVirtCloud.getInstanceState(getConnect(), getInstanceID());
	}

	public boolean instanceLives() {
		return LibVirtCloud.instanceLives(getConnect(), getInstanceID());
	}

	public void newInstance(InstanceType type, String sImageId, String sKeyName)
			throws LibVirtException {
		Instance i = LibVirtCloud.newInstance(getConnect(), type, sImageId,
				sKeyName);
		if (i == null) {
			// TODO externalize error message
			throw new LibVirtException(Messages.bind(
					"[{4}] Failed to create a new LibVirt Instance "
							+ "with the following characteristics region:"
							+ "{0}, image-id:{1}, type:{2}, keypair:{3}.",
					new Object[] { getRegion(), sImageId, type, sKeyName,
							getTargetNodeLocation() }));
		}
		setInstanceID(i.getInstanceId());
		setInstanceRelatedInfosToED(i);
	}

	public void deleteInstance() {
		LibVirtCloud.deleteInstance(getConnect(), getInstanceID());
		setInstanceID(null);
	}

	protected void setInstanceRelatedInfosToED(Instance i) {
		if (i == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid Instance.");
		}
		try {
			getED().setAttributeValue(getMelodyID(), Common.INSTANCE_ID_ATTR,
					i.getInstanceId());
			setInstancePrivIpToED(i.getPrivateIpAddress());
			setInstancePrivFQDNToED(i.getPrivateDnsName());
		} catch (NoSuchDUNIDException Ex) {
			throw new RuntimeException("Unexpected error while retrieving a "
					+ "node via its DUNID. " + "No node DUNID match "
					+ getMelodyID() + ". "
					+ "Source code has certainly been modified and a bug "
					+ "have been introduced.", Ex);
		}
	}

	private void setInstancePrivIpToED(String sIpAddr)
			throws NoSuchDUNIDException {
		if (sIpAddr == null || sIpAddr.length() == 0) {
			return;
		}
		try {
			Host.parseString(sIpAddr);
		} catch (IllegalHostException Ex) {
			throw new RuntimeException(Messages.bind("''{0}'': Not Accepted. "
					+ "Failed to parse ''{1}'', which was given by "
					+ "LibVirt.", sIpAddr, Common.IP_PRIV_ATTR), Ex);
		}
		getED().setAttributeValue(getMelodyID(), Common.IP_PRIV_ATTR, sIpAddr);
	}

	private void setInstancePrivFQDNToED(String sFQDN)
			throws NoSuchDUNIDException {
		if (sFQDN == null || sFQDN.length() == 0) {
			return;
		}
		getED().setAttributeValue(getMelodyID(), Common.FQDN_PRIV_ATTR, sFQDN);
	}

	protected void removeInstanceRelatedInfosToED(boolean deleted) {
		try {
			if (deleted == true) {
				getED().removeAttribute(getMelodyID(), Common.INSTANCE_ID_ATTR);
			}
			getED().removeAttribute(getMelodyID(), Common.IP_PRIV_ATTR);
			getED().removeAttribute(getMelodyID(), Common.FQDN_PRIV_ATTR);
		} catch (NoSuchDUNIDException Ex) {
			throw new RuntimeException("Unexpected error while removing a "
					+ "node's attribute via the node DUNID. "
					+ "No node DUNID match " + getMelodyID() + ". "
					+ "Source code has certainly been modified and a bug "
					+ "have been introduced.", Ex);
		}
	}

	@Override
	public ITaskContext getContext() {
		return moContext;
	}

	/**
	 * <p>
	 * Set the {@link ITaskContext} of this object with the given
	 * {@link ITaskContext}. Retrieve the LibVirt Plug-In {@link Configuration}
	 * and the Ssh Plug-In
	 * {@link com.wat.melody.plugin.ssh.common.Configuration}.
	 * </p>
	 * 
	 * @param p
	 *            is the {@link ITaskContext} to set.
	 * 
	 * @throws LibVirtException
	 *             if an error occurred while retrieving the Libvirt Plug-In
	 *             {@link Configuration}.
	 * @throws SshException
	 *             if an error occurred while retrieving the Ssh Plug-In
	 *             {@link com.wat.melody.plugin.ssh.common.Configuration}.
	 * @throws IllegalArgumentException
	 *             if the given {@link ITaskContext} is <tt>null</tt>.
	 */
	@Override
	public void setContext(ITaskContext p) throws LibVirtException,
			SshException {
		if (p == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid ITaskContext.");
		}
		moContext = p;

		// Get the configuration at the very beginning
		try {
			setPluginConf(Configuration.get(getContext().getProcessorManager()));
		} catch (ConfigurationException Ex) {
			throw new LibVirtException(Ex);
		}

		// Get the Ssh Plug-In configuration at the very beginning
		try {
			setSshPluginConf(com.wat.melody.plugin.ssh.common.Configuration
					.get(getContext().getProcessorManager()));
		} catch (com.wat.melody.plugin.ssh.common.exception.ConfigurationException Ex) {
			throw new SshException(Ex);
		}
	}

	protected Configuration getPluginConf() {
		return moPluginConf;
	}

	public Configuration setPluginConf(Configuration p) {
		if (p == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid Configuration.");
		}
		Configuration previous = getPluginConf();
		moPluginConf = p;
		return previous;
	}

	protected com.wat.melody.plugin.ssh.common.Configuration getSshPluginConf() {
		return moSshPluginConf;
	}

	public com.wat.melody.plugin.ssh.common.Configuration setSshPluginConf(
			com.wat.melody.plugin.ssh.common.Configuration p) {
		if (p == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid Configuration.");
		}
		com.wat.melody.plugin.ssh.common.Configuration previous = getSshPluginConf();
		moSshPluginConf = p;
		return previous;
	}

	protected Connect getConnect() {
		return moConnect;
	}

	private Connect setConnect(Connect cnx) {
		if (cnx == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid Connect.");
		}
		Connect previous = getConnect();
		moConnect = cnx;
		return previous;
	}

	/**
	 * @return the {@link DUNID} of the targeted {@link Node}.
	 */
	public DUNID getMelodyID() {
		return msMelodyId;
	}

	private DUNID setMelodyID(DUNID melodyID) throws NoSuchDUNIDException {
		if (melodyID == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a MelodyID).");
		}
		setInstanceID(getED().getAttributeValue(melodyID,
				Common.INSTANCE_ID_ATTR));
		DUNID previous = getMelodyID();
		msMelodyId = melodyID;
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
					+ "Must be a valid Node (the targeted Instance Node).");
		}
		Node previous = getTargetNode();
		moTargetNode = n;
		return previous;
	}

	/**
	 * @return the Instance Id which is registered in the targeted Node (can be
	 *         <code>null</code>).
	 */
	protected String getInstanceID() {
		return msInstanceID;
	}

	protected String setInstanceID(String sInstanceID) {
		// can be null, if no Instance have been created yet
		String previous = getInstanceID();
		msInstanceID = sInstanceID;
		return previous;
	}

	public String getRegion() {
		return msRegion;
	}

	@Attribute(name = REGION_ATTR)
	public String setRegion(String region) throws LibVirtException {
		if (region == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a libvirt connection URI).");
		}
		// TODO : how to validate the region? by testing the connection ?
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
	public String setTarget(String target) throws LibVirtException {
		if (target == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (an XPath Expression, which "
					+ "selects a sole XML Element node in the Resources "
					+ "Descriptor.");
		}

		NodeList nl = null;
		try {
			nl = getED().evaluateAsNodeList(target);
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
					target, Doc.parseInt(n.getNodeType())));
		}
		setTargetNode(n);
		DUNID dunid = getED().getMelodyID(n);
		try {
			setMelodyID(dunid);
		} catch (NoSuchDUNIDException Ex) {
			throw new RuntimeException("Unexpected error while manipulating "
					+ "the MelodyID of the element node which "
					+ "position is '" + getTarget() + "'. " + "The MelodyID '"
					+ dunid + "' doesn't seems to be valid. "
					+ "Since the MelodyID have been automatically added to "
					+ "all element node by the Melody's Engine, such error "
					+ "can't happened. "
					+ "Source code has certainly been modified and "
					+ "a bug have been introduced.", Ex);
		}
		String previous = getTarget();
		msTarget = target;
		return previous;
	}

	public long getTimeout() {
		return mlTimeout;
	}

	@Attribute(name = TIMEOUT_ATTR)
	public long setTimeout(long timeout) throws LibVirtException {
		if (timeout < 0) {
			throw new LibVirtException(Messages.bind(
					Messages.MachineEx_INVALID_TIMEOUT_ATTR, new Object[] {
							timeout, TIMEOUT_ATTR,
							getClass().getSimpleName().toLowerCase() }));
		}
		long previous = getTimeout();
		mlTimeout = timeout;
		return previous;
	}

}