package com.wat.melody.cloud.instance;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.wat.melody.cloud.disk.DiskDeviceList;
import com.wat.melody.cloud.instance.exception.OperationException;
import com.wat.melody.cloud.network.NetworkDeviceDatas;
import com.wat.melody.cloud.network.NetworkDeviceNameList;
import com.wat.melody.cloud.network.NetworkDeviceNamesLoader;
import com.wat.melody.cloud.network.NetworkManagementHelper;
import com.wat.melody.common.firewall.FireWallRules;
import com.wat.melody.common.firewall.FireWallRulesPerDevice;
import com.wat.melody.common.firewall.NetworkDeviceName;
import com.wat.melody.common.keypair.KeyPairName;
import com.wat.melody.common.xml.exception.NodeRelatedException;

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
	private Element _relatedElement;

	public InstanceControllerWithRelatedNode(InstanceController instance,
			Element relatedElmt) {
		setInstance(instance);
		setRelatedElement(relatedElmt);
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

	private Element getRelatedElement() {
		return _relatedElement;
	}

	private Element setRelatedElement(Element relatedNode) {
		if (relatedNode == null) {
			throw new IllegalArgumentException("null: Not accepted."
					+ "Must be a valid " + Element.class.getCanonicalName()
					+ ".");
		}
		Element previous = getRelatedElement();
		_relatedElement = relatedNode;
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
			FireWallRulesPerDevice fireWallRules) throws OperationException,
			InterruptedException {
		getInstance().ensureInstanceFireWallRulesAreUpToDate(fireWallRules);
	}

	@Override
	public void revokeInstanceFireWallRules(NetworkDeviceName netDev,
			FireWallRules toRevoke) throws OperationException,
			InterruptedException {
		getInstance().revokeInstanceFireWallRules(netDev, toRevoke);
	}

	@Override
	public void authorizeInstanceFireWallRules(NetworkDeviceName netDev,
			FireWallRules toAutorize) throws OperationException,
			InterruptedException {
		getInstance().authorizeInstanceFireWallRules(netDev, toAutorize);
	}

	@Override
	public FireWallRules getInstanceFireWallRules(NetworkDeviceName netDev) {
		return getInstance().getInstanceFireWallRules(netDev);
	}

	@Override
	public void onInstanceCreated() throws OperationException,
			InterruptedException {
		setData(getRelatedElement(), InstanceDatasLoader.INSTANCE_ID_ATTR,
				getInstanceId());
		fireInstanceCreated();
	}

	@Override
	public void onInstanceDestroyed() throws OperationException,
			InterruptedException {
		fireInstanceDestroyed();
		removeData(getRelatedElement(), InstanceDatasLoader.INSTANCE_ID_ATTR);
	}

	@Override
	public void onInstanceStopped() throws OperationException,
			InterruptedException {
		fireInstanceStopped();
		NetworkDeviceNameList netDevices = null;
		try {
			netDevices = new NetworkDeviceNamesLoader()
					.load(getRelatedElement());
		} catch (NodeRelatedException Ex) {
			throw new OperationException(Ex);
		}
		for (NetworkDeviceName netDev : netDevices) {
			Element d = getNetworkDeviceNode(netDev);
			if (d == null) {
				// The instance node could have no such network device node
				continue;
			}
			synchronized (getRelatedElement().getOwnerDocument()) {
				removeData(d, NetworkDeviceNamesLoader.IP_ATTR);
				removeData(d, NetworkDeviceNamesLoader.FQDN_ATTR);
				removeData(d, NetworkDeviceNamesLoader.NAT_IP_ATTR);
				removeData(d, NetworkDeviceNamesLoader.NAT_FQDN_ATTR);
			}
		}
	}

	@Override
	public void onInstanceStarted() throws OperationException,
			InterruptedException {
		for (NetworkDeviceName netDevice : getInstanceNetworkDevices()) {
			Element d = getNetworkDeviceNode(netDevice);
			if (d == null) {
				// The instance node could have no such network device node
				continue;
			}
			NetworkDeviceDatas ndd = getInstanceNetworkDeviceDatas(netDevice);
			synchronized (getRelatedElement().getOwnerDocument()) {
				setData(d, NetworkDeviceNamesLoader.IP_ATTR, ndd.getIP());
				setData(d, NetworkDeviceNamesLoader.FQDN_ATTR, ndd.getFQDN());
				setData(d, NetworkDeviceNamesLoader.NAT_IP_ATTR, ndd.getNatIP());
				setData(d, NetworkDeviceNamesLoader.NAT_FQDN_ATTR,
						ndd.getNatFQDN());
			}
		}
		fireInstanceStarted();
	}

	protected void setData(Element netdev, String sAttr, String sValue) {
		if (sValue == null || sValue.length() == 0) {
			return;
		}
		netdev.setAttribute(sAttr, sValue);
	}

	protected void removeData(Element netdev, String sAttr) {
		netdev.removeAttribute(sAttr);
	}

	private Element getNetworkDeviceNode(NetworkDeviceName nd)
			throws OperationException {
		NodeList netDevs = null;
		try {
			netDevs = NetworkManagementHelper.findNetworkDeviceNodeByName(
					getRelatedElement(), nd.getValue());
		} catch (NodeRelatedException Ex) {
			throw new OperationException(Ex);
		}
		return (netDevs == null || netDevs.getLength() == 0) ? null
				: (Element) netDevs.item(0);
	}

}
