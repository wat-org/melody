package com.wat.melody.cloud.instance;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Node;

import com.wat.melody.cloud.disk.DiskDeviceList;
import com.wat.melody.cloud.instance.exception.OperationException;
import com.wat.melody.cloud.network.NetworkDeviceDatas;
import com.wat.melody.cloud.network.NetworkDeviceName;
import com.wat.melody.cloud.network.NetworkDeviceNameList;
import com.wat.melody.cloud.network.NetworkManager;
import com.wat.melody.cloud.network.NetworkManagerFactory;
import com.wat.melody.cloud.network.NetworkManagerFactoryConfigurationCallback;
import com.wat.melody.cloud.network.exception.NetworkManagementException;
import com.wat.melody.common.keypair.KeyPairName;
import com.wat.melody.common.network.Access;
import com.wat.melody.common.network.Direction;
import com.wat.melody.common.network.FwRuleDecomposed;
import com.wat.melody.common.network.FwRulesDecomposed;
import com.wat.melody.common.network.Interface;
import com.wat.melody.common.network.IpRange;
import com.wat.melody.common.network.Port;
import com.wat.melody.common.network.PortRange;
import com.wat.melody.common.network.Protocol;
import com.wat.melody.common.network.exception.IllegalInterfaceException;
import com.wat.melody.common.network.exception.IllegalPortRangeException;

/**
 * <p>
 * Decorate the given {@link InstanceController} Instance with management
 * features.
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public class InstanceControllerWithNetworkManagement extends
		BaseInstanceController implements InstanceControllerListener {

	private static Log log = LogFactory
			.getLog(InstanceControllerWithNetworkManagement.class);

	private InstanceController _instance;
	private Node _relatedNode;
	private NetworkManagerFactoryConfigurationCallback _confCB;

	public InstanceControllerWithNetworkManagement(InstanceController instance,
			NetworkManagerFactoryConfigurationCallback confCB, Node relatedNode) {
		setInstance(instance);
		setNetworkManagerFactoryConfigurationCallback(confCB);
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

	private NetworkManagerFactoryConfigurationCallback getNetworkManagerFactoryConfigurationCallback() {
		return _confCB;
	}

	private NetworkManagerFactoryConfigurationCallback setNetworkManagerFactoryConfigurationCallback(
			NetworkManagerFactoryConfigurationCallback confCB) {
		if (confCB == null) {
			throw new IllegalArgumentException(
					"null: Not accepted."
							+ "Must be a valid "
							+ NetworkManagerFactoryConfigurationCallback.class
									.getCanonicalName() + ".");
		}
		NetworkManagerFactoryConfigurationCallback previous = getNetworkManagerFactoryConfigurationCallback();
		_confCB = confCB;
		return previous;
	}

	private Node getRelatedNode() {
		return _relatedNode;
	}

	private Node setRelatedNode(Node rd) {
		/*
		 * TODO : this class shouldn't be linked to the instance node. It should
		 * be useable without RD (like [Default]InstanceController)
		 */
		if (rd == null) {
			throw new IllegalArgumentException("null: Not accepted."
					+ "Must be a valid " + Node.class.getCanonicalName() + ".");
		}
		Node previous = getRelatedNode();
		_relatedNode = rd;
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
			FwRulesDecomposed fireWallRules) throws OperationException {
		getInstance().ensureInstanceFireWallRulesAreUpToDate(fireWallRules);
	}

	@Override
	public void revokeInstanceFireWallRules(NetworkDeviceName netDev,
			FwRulesDecomposed toRevoke) throws OperationException {
		getInstance().revokeInstanceFireWallRules(netDev, toRevoke);
	}

	@Override
	public void authorizeInstanceFireWallRules(NetworkDeviceName netDev,
			FwRulesDecomposed toAutorize) throws OperationException {
		getInstance().authorizeInstanceFireWallRules(netDev, toAutorize);
	}

	@Override
	public FwRulesDecomposed getInstanceFireWallRules(NetworkDeviceName netDev) {
		return getInstance().getInstanceFireWallRules(netDev);
	}

	@Override
	public void onInstanceCreated() throws OperationException,
			InterruptedException {
		fireInstanceCreated();
	}

	@Override
	public void onInstanceDestroyed() throws OperationException,
			InterruptedException {
		fireInstanceDestroyed();
	}

	@Override
	public void onInstanceStopped() throws OperationException,
			InterruptedException {
		disableNetworkManagement();
		fireInstanceStopped();
	}

	@Override
	public void onInstanceStarted() throws OperationException,
			InterruptedException {
		enableNetworkManagement();
		fireInstanceStarted();
	}

	/**
	 * <p>
	 * Based on the underlying operating system of this Instance, this method
	 * will perform actions to facilitates the management of the Instance :
	 * <ul>
	 * <li>If the operating system is Unix/Linux : will add the instance's
	 * HostKey from the Ssh Plug-In KnownHost file ;</li>
	 * <li>If the operating system is Windows : will add the instance's
	 * certificate in the local WinRM Plug-In repo ;</li>
	 * </ul>
	 * </p>
	 * 
	 * @throws OperationException
	 * @throws InterruptedException
	 */
	public void enableNetworkManagement() throws OperationException,
			InterruptedException {
		log.debug(Messages.bind(Messages.InstanceMsg_MANAGEMENT_ENABLE_BEGIN,
				getInstanceId()));

		NetworkManager nm = getNetworkManager();
		NetworkDeviceName netdev = nm.getManagementDatas()
				.getNetworkDeviceName();
		Port p = nm.getManagementDatas().getPort();
		Interface inter = null;
		PortRange toPorts = null;
		try {
			inter = Interface.parseString(netdev.getValue());
			toPorts = new PortRange(p, p);
		} catch (IllegalInterfaceException | IllegalPortRangeException Ex) {
			throw new RuntimeException("BUG ! Cannot happened !", Ex);
		}
		FwRuleDecomposed rule = new FwRuleDecomposed(inter, IpRange.ALL,
				PortRange.ALL, IpRange.ALL, toPorts, Protocol.TCP,
				Direction.IN, Access.ALLOW);
		FwRulesDecomposed rules = new FwRulesDecomposed();
		FwRulesDecomposed currentRules = getInstanceFireWallRules(netdev);
		if (!currentRules.contains(rule)) {
			rules.add(rule);
		}

		authorizeInstanceFireWallRules(netdev, rules);
		try {
			nm.enableNetworkManagement();
		} catch (NetworkManagementException Ex) {
			throw new OperationException(Messages.bind(
					Messages.InstanceEx_MANAGEMENT_ENABLE_FAILED,
					getInstanceId()), Ex);
		} finally {
			revokeInstanceFireWallRules(netdev, rules);
		}
		log.info(Messages.bind(Messages.InstanceMsg_MANAGEMENT_ENABLE_SUCCESS,
				getInstanceId()));
	}

	/**
	 * <p>
	 * Based on the underlying operating system of this Instance, this method
	 * will perform actions to facilitates the management of the Instance :
	 * <ul>
	 * <li>If the operating system is Unix/Linux : will remove the instance's
	 * HostKey from the Ssh Plug-In KnownHost file ;</li>
	 * <li>If the operating system is Windows : will remove the instance's
	 * certificate in the local WinRM Plug-In repo ;</li>
	 * </ul>
	 * </p>
	 * 
	 * @throws OperationException
	 * @throws InterruptedException
	 */
	public void disableNetworkManagement() throws OperationException,
			InterruptedException {
		log.debug(Messages.bind(Messages.InstanceMsg_MANAGEMENT_DISABLE_BEGIN,
				getInstanceId()));
		try {
			getNetworkManager().disableNetworkManagement();
		} catch (NetworkManagementException Ex) {
			throw new OperationException(Messages.bind(
					Messages.InstanceEx_MANAGEMENT_DISABLE_FAILED,
					getInstanceId()), Ex);
		}
		log.info(Messages.bind(Messages.InstanceMsg_MANAGEMENT_DISABLE_SUCCESS,
				getInstanceId()));
	}

	private NetworkManager getNetworkManager() {
		return NetworkManagerFactory.createNetworkManager(
				getNetworkManagerFactoryConfigurationCallback(),
				getRelatedNode());
	}

}