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
import com.wat.melody.api.exception.PlugInConfigurationException;
import com.wat.melody.api.exception.ResourcesDescriptorException;
import com.wat.melody.cloud.disk.DiskDeviceList;
import com.wat.melody.cloud.instance.InstanceState;
import com.wat.melody.cloud.instance.InstanceType;
import com.wat.melody.cloud.network.NetworkDevice;
import com.wat.melody.cloud.network.NetworkDeviceDatas;
import com.wat.melody.cloud.network.NetworkDeviceList;
import com.wat.melody.cloud.network.NetworkDevicesLoader;
import com.wat.melody.cloud.network.NetworkManagementHelper;
import com.wat.melody.common.keypair.KeyPairName;
import com.wat.melody.common.xml.DUNID;
import com.wat.melody.common.xml.Doc;
import com.wat.melody.common.xml.exception.NoSuchDUNIDException;
import com.wat.melody.plugin.libvirt.common.exception.LibVirtException;
import com.wat.melody.plugin.ssh.common.SshPlugInConfiguration;
import com.wat.melody.xpath.XPathHelper;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
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
	private LibVirtPlugInConfiguration moPluginConf;
	private SshPlugInConfiguration moSshPluginConf;
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
			v = XPathHelper.getHeritedAttributeValue(getTargetNode(),
					Common.REGION_ATTR);
		} catch (ResourcesDescriptorException Ex) {
			throw new LibVirtException(Ex);
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

		// Keep the Connection in a dedicated member
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
		return Doc.getNodeLocation(getTargetNode()).toFullString();
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

	public boolean instanceRuns() {
		return LibVirtCloud.instanceRuns(getConnect(), getInstanceID());
	}

	public void newInstance(InstanceType type, String sImageId,
			KeyPairName keyPairName) throws LibVirtException {
		Instance i = LibVirtCloud.newInstance(getConnect(), type, sImageId,
				keyPairName);
		if (i == null) {
			throw new LibVirtException(Messages.bind(Messages.NewEx_FAILED,
					new Object[] { getRegion(), sImageId, type, keyPairName,
							getTargetNodeLocation() }));
		}
		setInstanceID(i.getInstanceId());
		setInstanceRelatedInfosToED(i);
	}

	public void deleteInstance() {
		LibVirtCloud.deleteInstance(getConnect(), getInstanceID());
		setInstanceID(null);
	}

	public DiskDeviceList getInstanceDiskDevices(Instance i) {
		return LibVirtCloud.getInstanceDiskDevices(i);
	}

	protected void detachAndDeleteDiskDevices(Instance i,
			DiskDeviceList disksToRemove, long detachTimeout)
			throws LibVirtException, InterruptedException {
		LibVirtCloud.detachAndDeleteDiskDevices(i, disksToRemove);
	}

	protected void createAndAttachDiskDevices(Instance i,
			DiskDeviceList disksToAdd, long createTimeout, long attachTimeout)
			throws LibVirtException, InterruptedException {
		LibVirtCloud.createAndAttachDiskDevices(i, disksToAdd);
	}

	protected void updateDeleteOnTerminationFlag(Instance i,
			DiskDeviceList diskList) {
		/*
		 * Not supported by LibVirt. Disk are always deleted on instance
		 * termination.
		 */
	}

	public NetworkDeviceList getInstanceNetworkDevices(Instance i) {
		return LibVirtCloud.getInstanceNetworkDevices(i);
	}

	protected void detachNetworkDevices(Instance i,
			NetworkDeviceList netDevivesToRemove, long detachTimeout)
			throws LibVirtException, InterruptedException {
		LibVirtCloud.detachNetworkDevices(i, netDevivesToRemove);
	}

	protected void attachNetworkDevices(Instance i,
			NetworkDeviceList netDevivesToAdd, long attachTimeout)
			throws LibVirtException, InterruptedException {
		LibVirtCloud.attachNetworkDevices(i, netDevivesToAdd);
	}

	protected void setInstanceRelatedInfosToED(Instance i)
			throws LibVirtException {
		if (i == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid Instance.");
		}
		setDataToED(getMelodyID(), Common.INSTANCE_ID_ATTR, i.getInstanceId());
		for (NetworkDevice netDevice : getInstanceNetworkDevices(i)) {
			NetworkDeviceDatas ndd = LibVirtCloud
					.getInstanceNetworkDeviceDatas(i, netDevice);
			DUNID dunid = getNetworkDeviceDUNID(netDevice);
			setDataToED(dunid, Common.IP_ATTR, ndd.getIP());
			setDataToED(dunid, Common.FQDN_ATTR, ndd.getFQDN());
		}
	}

	private void setDataToED(DUNID dunid, String sAttr, String sValue) {
		if (dunid == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid DUNID.");
		}
		if (sAttr == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (an XML Attribute Name).");
		}
		if (sValue == null || sValue.length() == 0) {
			return;
		}
		try {
			getED().setAttributeValue(dunid, sAttr, sValue);
		} catch (NoSuchDUNIDException Ex) {
			throw new RuntimeException("Unexpected error while setting the "
					+ "node's attribute '" + sAttr + "' via its DUNID. "
					+ "No node DUNID match " + dunid + ". "
					+ "Source code has certainly been modified and a bug "
					+ "have been introduced.", Ex);
		}
	}

	protected void removeInstanceRelatedInfosToED(boolean deleted)
			throws LibVirtException {
		if (deleted == true) {
			removeDataFromED(getMelodyID(), Common.INSTANCE_ID_ATTR);
		}
		NetworkDeviceList netDevices = null;
		try {
			NodeList nl = NetworkManagementHelper
					.findNetworkDevices(getTargetNode());
			NetworkDevicesLoader ndl = new NetworkDevicesLoader(getContext());
			netDevices = ndl.load(nl);
		} catch (ResourcesDescriptorException Ex) {
			throw new LibVirtException(Ex);
		}
		for (NetworkDevice netDev : netDevices) {
			DUNID dunid = getNetworkDeviceDUNID(netDev);
			removeDataFromED(dunid, Common.IP_ATTR);
			removeDataFromED(dunid, Common.FQDN_ATTR);
		}
	}

	private void removeDataFromED(DUNID dunid, String sAttr) {
		try {
			getED().removeAttribute(dunid, sAttr);
		} catch (NoSuchDUNIDException Ex) {
			throw new RuntimeException("Unexpected error while removing the "
					+ "node's attribute '" + sAttr + "' via the node DUNID. "
					+ "No node DUNID match " + dunid + ". "
					+ "Source code has certainly been modified and a bug "
					+ "have been introduced.", Ex);
		}
	}

	protected DUNID getNetworkDeviceDUNID(NetworkDevice nd)
			throws LibVirtException {
		try {
			Node netDevNode = NetworkManagementHelper.findNetworkDeviceByName(
					getTargetNode(), nd.getDeviceName());
			return getED().getMelodyID(netDevNode);
		} catch (ResourcesDescriptorException Ex) {
			throw new LibVirtException(Ex);
		}
	}

	@Override
	public ITaskContext getContext() {
		return moContext;
	}

	/**
	 * <p>
	 * Set the {@link ITaskContext} of this object with the given
	 * {@link ITaskContext}. Retrieve the LibVirt Plug-In
	 * {@link LibVirtPlugInConfiguration} and the Ssh Plug-In
	 * {@link SshPlugInConfiguration}.
	 * </p>
	 * 
	 * @param p
	 *            is the {@link ITaskContext} to set.
	 * 
	 * @throws LibVirtException
	 *             if an error occurred while retrieving the Libvirt Plug-In
	 *             {@link LibVirtPlugInConfiguration}.
	 * @throws LibVirtException
	 *             if an error occurred while retrieving the Ssh Plug-In
	 *             {@link SshPlugInConfiguration}.
	 * @throws IllegalArgumentException
	 *             if the given {@link ITaskContext} is <tt>null</tt>.
	 */
	@Override
	public void setContext(ITaskContext p) throws LibVirtException {
		if (p == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid ITaskContext.");
		}
		moContext = p;

		// Get the configuration at the very beginning
		try {
			setPluginConf(LibVirtPlugInConfiguration.get(getContext()
					.getProcessorManager()));
		} catch (PlugInConfigurationException Ex) {
			throw new LibVirtException(Ex);
		}

		// Get the Ssh Plug-In configuration at the very beginning
		try {
			setSshPluginConf(SshPlugInConfiguration.get(getContext()
					.getProcessorManager()));
		} catch (PlugInConfigurationException Ex) {
			throw new LibVirtException(Ex);
		}
	}

	protected LibVirtPlugInConfiguration getPluginConf() {
		return moPluginConf;
	}

	public LibVirtPlugInConfiguration setPluginConf(LibVirtPlugInConfiguration p) {
		if (p == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid Configuration.");
		}
		LibVirtPlugInConfiguration previous = getPluginConf();
		moPluginConf = p;
		return previous;
	}

	protected SshPlugInConfiguration getSshPluginConf() {
		return moSshPluginConf;
	}

	public SshPlugInConfiguration setSshPluginConf(SshPlugInConfiguration p) {
		if (p == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid Configuration.");
		}
		SshPlugInConfiguration previous = getSshPluginConf();
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
					Messages.MachineEx_INVALID_TIMEOUT_ATTR, timeout));
		}
		long previous = getTimeout();
		mlTimeout = timeout;
		return previous;
	}

}