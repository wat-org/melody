package com.wat.melody.cloud.instance;

import org.w3c.dom.Element;

import com.wat.melody.cloud.disk.DiskDeviceList;
import com.wat.melody.cloud.instance.exception.OperationException;
import com.wat.melody.cloud.instance.exception.OperationTimeoutException;
import com.wat.melody.cloud.instance.xml.InstanceDatasLoader;
import com.wat.melody.cloud.network.NetworkDevice;
import com.wat.melody.cloud.network.NetworkDeviceList;
import com.wat.melody.cloud.network.xml.NetworkDevicesHelper;
import com.wat.melody.cloud.network.xml.NetworkDevicesLoader;
import com.wat.melody.cloud.protectedarea.ProtectedAreaIds;
import com.wat.melody.common.firewall.FireWallRules;
import com.wat.melody.common.firewall.FireWallRulesPerDevice;
import com.wat.melody.common.firewall.NetworkDeviceName;
import com.wat.melody.common.keypair.KeyPairName;
import com.wat.melody.common.xml.exception.NodeRelatedException;

/**
 * <p>
 * Decorate the given {@link InstanceController}. Add the ability to update the
 * related Instance {@link Element}'s datas when the related
 * {@link InstanceController} changes.
 * 
 * On stop/start, Cloud Providers dynamically allocates ip address and fqdn.
 * This class will update the related Instance {@link Element}'s datas
 * accordingly.
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public class InstanceControllerRelatedToAnInstanceElement extends
		BaseInstanceController implements InstanceControllerListener {

	private InstanceController _instanceController;
	private Element _instanceElement;

	public InstanceControllerRelatedToAnInstanceElement(
			InstanceController instance, Element relatedInstanceElmt) {
		setInstanceController(instance);
		setInstanceElement(relatedInstanceElmt);
		instance.addListener(this);
	}

	private InstanceController getInstanceController() {
		return _instanceController;
	}

	private InstanceController setInstanceController(
			InstanceController instanceController) {
		if (instanceController == null) {
			throw new IllegalArgumentException("null: Not accepted."
					+ "Must be a valid "
					+ InstanceController.class.getCanonicalName() + ".");
		}
		InstanceController previous = getInstanceController();
		_instanceController = instanceController;
		return previous;
	}

	private Element getInstanceElement() {
		return _instanceElement;
	}

	private Element setInstanceElement(Element relatedElmt) {
		if (relatedElmt == null) {
			throw new IllegalArgumentException("null: Not accepted."
					+ "Must be a valid " + Element.class.getCanonicalName()
					+ ".");
		}
		Element previous = getInstanceElement();
		_instanceElement = relatedElmt;
		return previous;
	}

	@Override
	public String getInstanceId() {
		return getInstanceController().getInstanceId();
	}

	@Override
	public boolean isInstanceDefined() {
		return getInstanceController().isInstanceDefined();
	}

	@Override
	public boolean instanceExists() {
		return getInstanceController().instanceExists();
	}

	@Override
	public boolean instanceLives() {
		return getInstanceController().instanceLives();
	}

	@Override
	public boolean instanceRuns() {
		return getInstanceController().instanceRuns();
	}

	@Override
	public InstanceState getInstanceState() {
		return getInstanceController().getInstanceState();
	}

	@Override
	public InstanceType getInstanceType() {
		return getInstanceController().getInstanceType();
	}

	@Override
	public void ensureInstanceIsCreated(InstanceType type, String site,
			String imageId, KeyPairName keyPairName,
			ProtectedAreaIds protectedAreaIds, long createTimeout)
			throws OperationException, OperationTimeoutException,
			InterruptedException {
		getInstanceController().ensureInstanceIsCreated(type, site, imageId,
				keyPairName, protectedAreaIds, createTimeout);
	}

	@Override
	public void ensureInstanceIsDestroyed(long timeout)
			throws OperationException, OperationTimeoutException,
			InterruptedException {
		getInstanceController().ensureInstanceIsDestroyed(timeout);
	}

	@Override
	public void ensureInstanceIsStarted(long startTimeout)
			throws OperationException, OperationTimeoutException,
			InterruptedException {
		getInstanceController().ensureInstanceIsStarted(startTimeout);
	}

	@Override
	public void ensureInstanceIsStoped(long stopTimeout)
			throws OperationException, OperationTimeoutException,
			InterruptedException {
		getInstanceController().ensureInstanceIsStoped(stopTimeout);
	}

	@Override
	public void ensureInstanceSizing(InstanceType targetType)
			throws OperationException, InterruptedException {
		getInstanceController().ensureInstanceSizing(targetType);
	}

	@Override
	public void ensureInstanceDiskDevicesAreUpToDate(DiskDeviceList list)
			throws OperationException, InterruptedException {
		getInstanceController().ensureInstanceDiskDevicesAreUpToDate(list);
	}

	@Override
	public DiskDeviceList getInstanceDiskDevices() {
		return getInstanceController().getInstanceDiskDevices();
	}

	@Override
	public void ensureInstanceNetworkDevicesAreUpToDate(NetworkDeviceList list)
			throws OperationException, InterruptedException {
		getInstanceController().ensureInstanceNetworkDevicesAreUpToDate(list);
	}

	@Override
	public NetworkDeviceList getInstanceNetworkDevices() {
		return getInstanceController().getInstanceNetworkDevices();
	}

	@Override
	public void ensureInstanceFireWallRulesAreUpToDate(
			FireWallRulesPerDevice list) throws OperationException,
			InterruptedException {
		getInstanceController().ensureInstanceFireWallRulesAreUpToDate(list);
	}

	@Override
	public void revokeInstanceFireWallRules(NetworkDeviceName netDev,
			FireWallRules toRevoke) throws OperationException,
			InterruptedException {
		getInstanceController().revokeInstanceFireWallRules(netDev, toRevoke);
	}

	@Override
	public void authorizeInstanceFireWallRules(NetworkDeviceName netDev,
			FireWallRules toAuthorize) throws OperationException,
			InterruptedException {
		getInstanceController().authorizeInstanceFireWallRules(netDev,
				toAuthorize);
	}

	@Override
	public FireWallRules getInstanceFireWallRules(NetworkDeviceName netDev) {
		return getInstanceController().getInstanceFireWallRules(netDev);
	}

	@Override
	public void onInstanceCreated() throws OperationException,
			InterruptedException {
		setData(getInstanceElement(), InstanceDatasLoader.INSTANCE_ID_ATTR,
				getInstanceId());
		fireInstanceCreated();
	}

	@Override
	public void onInstanceDestroyed() throws OperationException,
			InterruptedException {
		fireInstanceDestroyed();
		removeData(getInstanceElement(), InstanceDatasLoader.INSTANCE_ID_ATTR);
	}

	@Override
	public void onInstanceStopped() throws OperationException,
			InterruptedException {
		fireInstanceStopped();
		NetworkDeviceList netDevices = null;
		try {
			netDevices = new NetworkDevicesLoader().load(getInstanceElement());
		} catch (NodeRelatedException Ex) {
			throw new OperationException(Ex);
		}
		for (NetworkDevice nd : netDevices) {
			Element d = getNetworkDeviceElement(nd);
			synchronized (getInstanceElement().getOwnerDocument()) {
				removeData(d, NetworkDevicesLoader.IP_ATTR);
				removeData(d, NetworkDevicesLoader.FQDN_ATTR);
				removeData(d, NetworkDevicesLoader.NAT_IP_ATTR);
				removeData(d, NetworkDevicesLoader.NAT_FQDN_ATTR);
			}
		}
	}

	@Override
	public void onInstanceStarted() throws OperationException,
			InterruptedException {
		for (NetworkDevice nd : getInstanceNetworkDevices()) {
			Element d = getNetworkDeviceElement(nd);
			if (d == null) {
				// The instance node could have no such network device node
				continue;
			}
			synchronized (getInstanceElement().getOwnerDocument()) {
				setData(d, NetworkDevicesLoader.IP_ATTR, nd.getIP());
				setData(d, NetworkDevicesLoader.FQDN_ATTR, nd.getFQDN());
				setData(d, NetworkDevicesLoader.NAT_IP_ATTR, nd.getNatIP());
				setData(d, NetworkDevicesLoader.NAT_FQDN_ATTR, nd.getNatFQDN());
			}
		}
		fireInstanceStarted();
	}

	protected void setData(Element elmt, String attr, String value) {
		if (value == null || value.length() == 0) {
			return;
		}
		elmt.setAttribute(attr, value);
	}

	protected void removeData(Element elmt, String attr) {
		elmt.removeAttribute(attr);
	}

	private Element getNetworkDeviceElement(NetworkDevice netdev)
			throws OperationException {
		try {
			return NetworkDevicesHelper.findNetworkDeviceElementByName(
					getInstanceElement(), netdev.getNetworkDeviceName()
							.getValue());
		} catch (NodeRelatedException Ex) {
			throw new OperationException(Ex);
		}
	}

}