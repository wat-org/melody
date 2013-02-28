package com.wat.melody.plugin.aws.ec2.common;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.Instance;
import com.wat.melody.api.IResourcesDescriptor;
import com.wat.melody.api.ITask;
import com.wat.melody.api.ITaskContext;
import com.wat.melody.api.annotation.Attribute;
import com.wat.melody.api.exception.PlugInConfigurationException;
import com.wat.melody.api.exception.ResourcesDescriptorException;
import com.wat.melody.cloud.instance.InstanceState;
import com.wat.melody.cloud.instance.InstanceType;
import com.wat.melody.cloud.network.NetworkDeviceName;
import com.wat.melody.cloud.network.NetworkDeviceNameList;
import com.wat.melody.cloud.network.NetworkDevicesLoader;
import com.wat.melody.cloud.network.NetworkManagementHelper;
import com.wat.melody.cloud.network.NetworkManagerFactoryConfigurationCallback;
import com.wat.melody.common.xml.DUNID;
import com.wat.melody.common.xml.Doc;
import com.wat.melody.common.xml.exception.NoSuchDUNIDException;
import com.wat.melody.plugin.aws.ec2.common.exception.AwsException;
import com.wat.melody.plugin.ssh.common.SshPlugInConfiguration;
import com.wat.melody.xpath.XPathHelper;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
abstract public class AbstractAwsOperation implements ITask,
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

	private ITaskContext moContext;
	private AwsPlugInConfiguration moPluginConf;
	private SshPlugInConfiguration moSshPluginConf;
	private AmazonEC2 moEC2;
	private String msAwsInstanceID;
	private Node moTargetNode;
	private DUNID msMelodyId;
	private String msRegion;
	private String msTarget;
	private long mlTimeout;

	public AbstractAwsOperation() {
		initContext();
		initPluginConf();
		initEC2();
		initMelodyId();
		initTargetNode();
		initAwsInstanceId();
		initRegion();
		initTimeout();
	}

	private void initContext() {
		moContext = null;
	}

	private void initPluginConf() {
		moPluginConf = null;
	}

	private void initEC2() {
		moEC2 = null;
	}

	private void initMelodyId() {
		msMelodyId = null;
	}

	private void initTargetNode() {
		moTargetNode = null;
	}

	private void initAwsInstanceId() {
		msAwsInstanceID = null;
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
			v = XPathHelper.getHeritedAttributeValue(getTargetNode(),
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
	}

	public IResourcesDescriptor getED() {
		return getContext().getProcessorManager().getResourcesDescriptor();
	}

	public String getTargetNodeLocation() {
		return Doc.getNodeLocation(getTargetNode()).toFullString();
	}

	public boolean instanceExists() {
		return Common.instanceExists(getEc2(), getAwsInstanceID());
	}

	public Instance getInstance() {
		return Common.getInstance(getEc2(), getAwsInstanceID());
	}

	public AwsInstance getAwsInstance() {
		Instance i = Common.getInstance(getEc2(), getAwsInstanceID());
		return i == null ? null : new AwsInstance(getEc2(), i);
	}

	public InstanceState getInstanceState() {
		return Common.getInstanceState(getEc2(), getAwsInstanceID());
	}

	public boolean instanceLives() {
		return Common.instanceLives(getEc2(), getAwsInstanceID());
	}

	public boolean instanceRuns() {
		return Common.instanceRuns(getEc2(), getAwsInstanceID());
	}

	protected boolean waitUntilInstanceStatusBecomes(InstanceState state,
			long timeout) throws InterruptedException {
		return waitUntilInstanceStatusBecomes(state, timeout, 0);
	}

	protected boolean waitUntilInstanceStatusBecomes(InstanceState state,
			long timeout, long sleepfirst) throws InterruptedException {
		return Common.waitUntilInstanceStatusBecomes(getEc2(),
				getAwsInstanceID(), state, timeout, sleepfirst);
	}

	protected boolean resizeInstance(InstanceType instanceType) {
		return Common.resizeAwsInstance(getEc2(), getAwsInstanceID(),
				instanceType);
	}

	protected void setInstanceRelatedInfosToED(Instance i) throws AwsException {
		if (i == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid Instance.");
		}
		setDataToED(getMelodyID(), Common.INSTANCE_ID_ATTR, i.getInstanceId());
		for (NetworkDeviceName netDevice : Common
				.getNetworkDevices(getEc2(), i)) {
			DUNID dunid = getNetworkDeviceDUNID(netDevice);
			if (dunid == null) {
				// The instance node could have no such network device node
				continue;
			}
			setDataToED(dunid, Common.IP_ATTR, i.getPrivateIpAddress());
			setDataToED(dunid, Common.FQDN_ATTR, i.getPrivateDnsName());
			setDataToED(dunid, Common.NAT_IP_ATTR, i.getPublicIpAddress());
			setDataToED(dunid, Common.NAT_FQDN_ATTR, i.getPublicDnsName());
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
			throws AwsException {
		if (deleted == true) {
			removeDataFromED(getMelodyID(), Common.INSTANCE_ID_ATTR);
		}
		NetworkDeviceNameList netDevices = null;
		try {
			NodeList nl = NetworkManagementHelper
					.findNetworkDevices(getTargetNode());
			NetworkDevicesLoader ndl = new NetworkDevicesLoader(getContext());
			netDevices = ndl.load(nl);
		} catch (ResourcesDescriptorException Ex) {
			throw new AwsException(Ex);
		}
		for (NetworkDeviceName netDev : netDevices) {
			DUNID dunid = getNetworkDeviceDUNID(netDev);
			removeDataFromED(dunid, Common.IP_ATTR);
			removeDataFromED(dunid, Common.FQDN_ATTR);
			removeDataFromED(dunid, Common.NAT_IP_ATTR);
			removeDataFromED(dunid, Common.NAT_FQDN_ATTR);
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

	protected DUNID getNetworkDeviceDUNID(NetworkDeviceName nd)
			throws AwsException {
		Node netDevNode = null;
		try {
			netDevNode = NetworkManagementHelper.findNetworkDeviceNodeByName(
					getTargetNode(), nd.getValue());
		} catch (ResourcesDescriptorException Ex) {
			throw new AwsException(Ex);
		}
		return netDevNode == null ? null : getED().getMelodyID(netDevNode);
	}

	@Override
	public ITaskContext getContext() {
		return moContext;
	}

	/**
	 * <p>
	 * Set the {@link ITaskContext} of this object with the given
	 * {@link ITaskContext}. Retrieve the Aws Plug-In
	 * {@link AwsPlugInConfiguration} and the Ssh Plug-In
	 * {@link SshPlugInConfiguration}.
	 * </p>
	 * 
	 * @param p
	 *            is the {@link ITaskContext} to set.
	 * 
	 * @throws AwsException
	 *             if an error occurred while retrieving the Aws Plug-In
	 *             {@link AwsPlugInConfiguration}.
	 * @throws AwsException
	 *             if an error occurred while retrieving the Ssh Plug-In
	 *             {@link SshPlugInConfiguration}.
	 * @throws IllegalArgumentException
	 *             if the given {@link ITaskContext} is <tt>null</tt>.
	 */
	@Override
	public void setContext(ITaskContext p) throws AwsException {
		if (p == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid ITaskContext.");
		}
		moContext = p;

		// Get the configuration at the very beginning
		try {
			setPluginConf(AwsPlugInConfiguration.get(getContext()
					.getProcessorManager()));
		} catch (PlugInConfigurationException Ex) {
			throw new AwsException(Ex);
		}

		// Get the Ssh Plug-In configuration at the very beginning
		try {
			setSshPluginConf(SshPlugInConfiguration.get(getContext()
					.getProcessorManager()));
		} catch (PlugInConfigurationException Ex) {
			throw new AwsException(Ex);
		}
	}

	protected AwsPlugInConfiguration getPluginConf() {
		return moPluginConf;
	}

	public AwsPlugInConfiguration setPluginConf(AwsPlugInConfiguration p) {
		if (p == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid Configuration.");
		}
		AwsPlugInConfiguration previous = getPluginConf();
		moPluginConf = p;
		return previous;
	}

	@Override
	public SshPlugInConfiguration getSshConfiguration() {
		return moSshPluginConf;
	}

	public SshPlugInConfiguration setSshPluginConf(SshPlugInConfiguration p) {
		if (p == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid Configuration.");
		}
		SshPlugInConfiguration previous = getSshConfiguration();
		moSshPluginConf = p;
		return previous;
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
		setAwsInstanceID(getED().getAttributeValue(melodyID,
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
	protected String getAwsInstanceID() {
		return msAwsInstanceID;
	}

	protected String setAwsInstanceID(String awsInstanceID) {
		// can be null, if no AWS instance have been created yet
		String previous = getAwsInstanceID();
		msAwsInstanceID = awsInstanceID;
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
			nl = getED().evaluateAsNodeList(target);
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