package com.wat.melody.cloud.instance;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.wat.melody.api.IResourcesDescriptor;
import com.wat.melody.api.exception.ResourcesDescriptorException;
import com.wat.melody.cloud.disk.DiskDeviceList;
import com.wat.melody.cloud.instance.exception.OperationException;
import com.wat.melody.cloud.network.NetworkDeviceDatas;
import com.wat.melody.cloud.network.NetworkDeviceName;
import com.wat.melody.cloud.network.NetworkDeviceNameList;
import com.wat.melody.cloud.network.NetworkDeviceNamesLoader;
import com.wat.melody.cloud.network.NetworkManagementHelper;
import com.wat.melody.common.firewall.FwRulesDecomposed;
import com.wat.melody.common.keypair.KeyPairName;
import com.wat.melody.common.xml.DUNID;
import com.wat.melody.common.xml.DUNIDDoc;
import com.wat.melody.common.xml.exception.NoSuchDUNIDException;

/**
 * <p>
 * Decorate the given {@link InstanceController} Instance with the ability to
 * update the Resource Descriptor.
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public class InstanceControllerWithRelatedNode extends BaseInstanceController
		implements InstanceControllerListener {

	private InstanceController _instance;
	private IResourcesDescriptor _rd;
	private Node _relatedNode;
	private DUNID _relatedNodeDunid;

	public InstanceControllerWithRelatedNode(InstanceController instance,
			IResourcesDescriptor rd, Node relatedNode) {
		setInstance(instance);
		setRD(rd);
		setRelatedNode(relatedNode);
		instance.addListener(this);
	}

	private InstanceController getInstance() {
		return _instance;
	}

	private InstanceController setInstance(InstanceController instance) {
		if (instance == null) {
			throw new IllegalArgumentException("null: Not accepted."
					+ "Must be a valid "
					+ InstanceController.class.getCanonicalName() + ".");
		}
		InstanceController previous = getInstance();
		_instance = instance;
		return previous;
	}

	public IResourcesDescriptor getRD() {
		return _rd;
	}

	private IResourcesDescriptor setRD(IResourcesDescriptor instance) {
		if (instance == null) {
			throw new IllegalArgumentException("null: Not accepted."
					+ "Must be a valid "
					+ IResourcesDescriptor.class.getCanonicalName() + ".");
		}
		IResourcesDescriptor previous = getRD();
		_rd = instance;
		return previous;
	}

	private Node getRelatedNode() {
		return _relatedNode;
	}

	private Node setRelatedNode(Node relatedNode) {
		if (relatedNode == null) {
			throw new IllegalArgumentException("null: Not accepted."
					+ "Must be a valid " + Node.class.getCanonicalName() + ".");
		}
		setRelatedNodeDunid(DUNIDDoc.getDUNID(relatedNode));
		Node previous = getRelatedNode();
		_relatedNode = relatedNode;
		return previous;
	}

	private DUNID getRelatedNodeDunid() {
		return _relatedNodeDunid;
	}

	private DUNID setRelatedNodeDunid(DUNID relatedNodeDunid) {
		if (relatedNodeDunid == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid String (a MelodyID).");
		}
		DUNID previous = getRelatedNodeDunid();
		_relatedNodeDunid = relatedNodeDunid;
		return previous;
	}

	@Override
	public String getInstanceId() {
		return getInstance().getInstanceId();
	}

	@Override
	public boolean isInstanceDefined() {
		return getInstance().isInstanceDefined();
	}

	@Override
	public boolean instanceExists() {
		return getInstance().instanceExists();
	}

	@Override
	public boolean instanceLives() {
		return getInstance().instanceLives();
	}

	@Override
	public boolean instanceRuns() {
		return getInstance().instanceRuns();
	}

	@Override
	public InstanceState getInstanceState() {
		return getInstance().getInstanceState();
	}

	@Override
	public InstanceType getInstanceType() {
		return getInstance().getInstanceType();
	}

	@Override
	public void ensureInstanceIsCreated(InstanceType type, String site,
			String imageId, KeyPairName keyPairName, long createTimeout)
			throws OperationException, InterruptedException {
		getInstance().ensureInstanceIsCreated(type, site, imageId, keyPairName,
				createTimeout);
	}

	@Override
	public void ensureInstanceIsDestroyed(long timeout)
			throws OperationException, InterruptedException {
		getInstance().ensureInstanceIsDestroyed(timeout);
	}

	@Override
	public void ensureInstanceIsStarted(long startTimeout)
			throws OperationException, InterruptedException {
		getInstance().ensureInstanceIsStarted(startTimeout);
	}

	@Override
	public void ensureInstanceIsStoped(long stopTimeout)
			throws OperationException, InterruptedException {
		getInstance().ensureInstanceIsStoped(stopTimeout);
	}

	@Override
	public void ensureInstanceSizing(InstanceType targetType)
			throws OperationException, InterruptedException {
		getInstance().ensureInstanceSizing(targetType);
	}

	@Override
	public void ensureInstanceDiskDevicesAreUpToDate(
			DiskDeviceList diskDeviceList, long createTimeout,
			long attachTimeout, long detachTimeout) throws OperationException,
			InterruptedException {
		getInstance().ensureInstanceDiskDevicesAreUpToDate(diskDeviceList,
				createTimeout, attachTimeout, detachTimeout);
	}

	@Override
	public DiskDeviceList getInstanceDiskDevices() {
		return getInstance().getInstanceDiskDevices();
	}

	@Override
	public void ensureInstanceNetworkDevicesAreUpToDate(
			NetworkDeviceNameList networkDeviceList, long attachTimeout,
			long detachTimeout) throws OperationException, InterruptedException {
		getInstance().ensureInstanceNetworkDevicesAreUpToDate(
				networkDeviceList, attachTimeout, detachTimeout);
	}

	@Override
	public NetworkDeviceNameList getInstanceNetworkDevices() {
		return getInstance().getInstanceNetworkDevices();
	}

	@Override
	public NetworkDeviceDatas getInstanceNetworkDeviceDatas(
			NetworkDeviceName netdev) {
		return getInstance().getInstanceNetworkDeviceDatas(netdev);
	}

	@Override
	public void ensureInstanceFireWallRulesAreUpToDate(
			FwRulesDecomposed fireWallRules) throws OperationException,
			InterruptedException {
		getInstance().ensureInstanceFireWallRulesAreUpToDate(fireWallRules);
	}

	@Override
	public void revokeInstanceFireWallRules(NetworkDeviceName netDev,
			FwRulesDecomposed toRevoke) throws OperationException,
			InterruptedException {
		getInstance().revokeInstanceFireWallRules(netDev, toRevoke);
	}

	@Override
	public void authorizeInstanceFireWallRules(NetworkDeviceName netDev,
			FwRulesDecomposed toAutorize) throws OperationException,
			InterruptedException {
		getInstance().authorizeInstanceFireWallRules(netDev, toAutorize);
	}

	@Override
	public FwRulesDecomposed getInstanceFireWallRules(NetworkDeviceName netDev) {
		return getInstance().getInstanceFireWallRules(netDev);
	}

	@Override
	public void onInstanceCreated() throws OperationException,
			InterruptedException {
		setDataToRD(getRelatedNodeDunid(),
				InstanceDatasLoader.INSTANCE_ID_ATTR, getInstanceId());
		fireInstanceCreated();
	}

	@Override
	public void onInstanceDestroyed() throws OperationException,
			InterruptedException {
		fireInstanceDestroyed();
		removeDataFromRD(getRelatedNodeDunid(),
				InstanceDatasLoader.INSTANCE_ID_ATTR);
	}

	@Override
	public void onInstanceStopped() throws OperationException,
			InterruptedException {
		fireInstanceStopped();
		NetworkDeviceNameList netDevices = null;
		try {
			netDevices = new NetworkDeviceNamesLoader().load(getRelatedNode());
		} catch (ResourcesDescriptorException Ex) {
			throw new OperationException(Ex);
		}
		for (NetworkDeviceName netDev : netDevices) {
			DUNID d = getNetworkDeviceDUNID(netDev);
			removeDataFromRD(d, NetworkDeviceNamesLoader.IP_ATTR);
			removeDataFromRD(d, NetworkDeviceNamesLoader.FQDN_ATTR);
			removeDataFromRD(d, NetworkDeviceNamesLoader.NAT_IP_ATTR);
			removeDataFromRD(d, NetworkDeviceNamesLoader.NAT_FQDN_ATTR);
		}
	}

	@Override
	public void onInstanceStarted() throws OperationException,
			InterruptedException {
		for (NetworkDeviceName netDevice : getInstanceNetworkDevices()) {
			DUNID d = getNetworkDeviceDUNID(netDevice);
			if (d == null) {
				// The instance node could have no such network device node
				continue;
			}
			NetworkDeviceDatas ndd = getInstanceNetworkDeviceDatas(netDevice);
			setDataToRD(d, NetworkDeviceNamesLoader.IP_ATTR, ndd.getIP());
			setDataToRD(d, NetworkDeviceNamesLoader.FQDN_ATTR, ndd.getFQDN());
			setDataToRD(d, NetworkDeviceNamesLoader.NAT_IP_ATTR, ndd.getNatIP());
			setDataToRD(d, NetworkDeviceNamesLoader.NAT_FQDN_ATTR,
					ndd.getNatFQDN());
		}
		fireInstanceStarted();
	}

	protected void setDataToRD(DUNID dunid, String sAttr, String sValue) {
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
			/*
			 * TODO : Is it possible to use nodes event/listener for the whole
			 * RD modification, instead of using crzay method
			 * gerRD().setAttributeValue ?
			 */
			getRD().setAttributeValue(dunid, sAttr, sValue);
		} catch (NoSuchDUNIDException Ex) {
			throw new RuntimeException("Unexpected error while setting the "
					+ "node's attribute '" + sAttr + "' via its DUNID. "
					+ "No node DUNID match " + dunid + ". "
					+ "Source code has certainly been modified and a bug "
					+ "have been introduced.", Ex);
		}
	}

	protected void removeDataFromRD(DUNID dunid, String sAttr) {
		try {
			getRD().removeAttribute(dunid, sAttr);
		} catch (NoSuchDUNIDException Ex) {
			throw new RuntimeException("Unexpected error while removing the "
					+ "node's attribute '" + sAttr + "' via the node DUNID. "
					+ "No node DUNID match " + dunid + ". "
					+ "Source code has certainly been modified and a bug "
					+ "have been introduced.", Ex);
		}
	}

	private DUNID getNetworkDeviceDUNID(NetworkDeviceName nd)
			throws OperationException {
		NodeList netDevs = null;
		try {
			netDevs = NetworkManagementHelper.findNetworkDeviceNodeByName(
					getRelatedNode(), nd.getValue());
		} catch (ResourcesDescriptorException Ex) {
			throw new OperationException(Ex);
		}
		Node netDevNode = netDevs == null || netDevs.getLength() == 0 ? null
				: netDevs.item(0);
		return netDevNode == null ? null : getRD().getMelodyID(netDevNode);
	}

}
