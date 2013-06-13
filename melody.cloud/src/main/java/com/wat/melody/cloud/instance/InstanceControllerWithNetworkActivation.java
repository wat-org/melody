package com.wat.melody.cloud.instance;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.wat.melody.cloud.disk.DiskDeviceList;
import com.wat.melody.cloud.instance.exception.OperationException;
import com.wat.melody.cloud.network.NetworkDeviceList;
import com.wat.melody.cloud.network.activation.NetworkActivationDatas;
import com.wat.melody.cloud.network.activation.NetworkActivator;
import com.wat.melody.cloud.network.activation.exception.NetworkActivationException;
import com.wat.melody.common.firewall.Access;
import com.wat.melody.common.firewall.Direction;
import com.wat.melody.common.firewall.FireWallRules;
import com.wat.melody.common.firewall.FireWallRulesPerDevice;
import com.wat.melody.common.firewall.NetworkDeviceName;
import com.wat.melody.common.firewall.SimpleFireWallRule;
import com.wat.melody.common.firewall.SimpleTcpFireWallRule;
import com.wat.melody.common.keypair.KeyPairName;
import com.wat.melody.common.network.IpRange;
import com.wat.melody.common.network.Port;
import com.wat.melody.common.network.PortRange;

/**
 * <p>
 * Decorate the given {@link InstanceController} with Network Activation
 * features.
 * 
 * Will perform OS specific action to facilitate the establishment of network
 * connection on the remote system.
 * 
 * On start :
 * <ul>
 * <li>for Linux : will wait until an SSH connection can be established and will
 * register the remote system's host key in a KnowHosts file, for ip address and
 * fqdn ;</li>
 * <li>for Windows : ... ;</li>
 * </ul>
 * 
 * On stop :
 * <ul>
 * <li>for Linux : will remove the previously registered remote system's host
 * key ;</li>
 * <li>for Windows : ... ;</li>
 * </ul>
 * 
 * @author Guillaume Cornet
 * 
 */
public class InstanceControllerWithNetworkActivation extends
		BaseInstanceController implements InstanceControllerListener {

	private static Log log = LogFactory
			.getLog(InstanceControllerWithNetworkActivation.class);

	private InstanceController _instanceController;
	private NetworkActivator _networkActivator;

	public InstanceControllerWithNetworkActivation(
			InstanceController instanceController,
			NetworkActivator networkActivator) {
		setInstanceController(instanceController);
		setNetworkActivator(networkActivator);
		instanceController.addListener(this);
	}

	private InstanceController getInstanceController() {
		return _instanceController;
	}

	private InstanceController setInstanceController(InstanceController instance) {
		if (instance == null) {
			throw new IllegalArgumentException("null: Not accepted."
					+ "Must be a valid "
					+ InstanceController.class.getCanonicalName() + ".");
		}
		InstanceController previous = getInstanceController();
		_instanceController = instance;
		return previous;
	}

	private NetworkActivator getNetworkActivator() {
		return _networkActivator;
	}

	private NetworkActivator setNetworkActivator(
			NetworkActivator networkActivator) {
		if (networkActivator == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ NetworkActivator.class.getCanonicalName() + ".");
		}
		NetworkActivator previous = getNetworkActivator();
		_networkActivator = networkActivator;
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
			String imageId, KeyPairName keyPairName, long createTimeout)
			throws OperationException, InterruptedException {
		getInstanceController().ensureInstanceIsCreated(type, site, imageId,
				keyPairName, createTimeout);
	}

	@Override
	public void ensureInstanceIsDestroyed(long timeout)
			throws OperationException, InterruptedException {
		getInstanceController().ensureInstanceIsDestroyed(timeout);
	}

	@Override
	public void ensureInstanceIsStarted(long startTimeout)
			throws OperationException, InterruptedException {
		getInstanceController().ensureInstanceIsStarted(startTimeout);
	}

	@Override
	public void ensureInstanceIsStoped(long stopTimeout)
			throws OperationException, InterruptedException {
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
			FireWallRules toAutorize) throws OperationException,
			InterruptedException {
		getInstanceController().authorizeInstanceFireWallRules(netDev,
				toAutorize);
	}

	@Override
	public FireWallRules getInstanceFireWallRules(NetworkDeviceName netDev) {
		return getInstanceController().getInstanceFireWallRules(netDev);
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
	private void enableNetworkManagement() throws OperationException,
			InterruptedException {
		NetworkActivator na = getNetworkActivator();
		NetworkActivationDatas nad = na.getNetworkActivationDatas();
		if (nad == null) {
			return;
		}
		log.debug(Messages.bind(Messages.InstanceMsg_MANAGEMENT_ENABLE_BEGIN,
				getInstanceId()));

		NetworkDeviceName netdev = na.getNetworkActivationDatas()
				.getNetworkDeviceName();
		Port p = na.getNetworkActivationDatas().getPort();
		PortRange toPorts = new PortRange(p);
		SimpleFireWallRule rule = new SimpleTcpFireWallRule(IpRange.ALL,
				PortRange.ALL, IpRange.ALL, toPorts, Direction.IN, Access.ALLOW);
		FireWallRules rules = new FireWallRules();
		FireWallRules currentRules = getInstanceFireWallRules(netdev);
		if (!currentRules.contains(rule)) {
			rules.add(rule);
		}

		authorizeInstanceFireWallRules(netdev, rules);
		try {
			na.enableNetworkActivation();
		} catch (NetworkActivationException Ex) {
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
	private void disableNetworkManagement() throws OperationException,
			InterruptedException {
		NetworkActivator na = getNetworkActivator();
		NetworkActivationDatas nad = na.getNetworkActivationDatas();
		if (nad == null) {
			return;
		}
		log.debug(Messages.bind(Messages.InstanceMsg_MANAGEMENT_DISABLE_BEGIN,
				getInstanceId()));
		try {
			na.disableNetworkActivation();
		} catch (NetworkActivationException Ex) {
			throw new OperationException(Messages.bind(
					Messages.InstanceEx_MANAGEMENT_DISABLE_FAILED,
					getInstanceId()), Ex);
		}
		log.info(Messages.bind(Messages.InstanceMsg_MANAGEMENT_DISABLE_SUCCESS,
				getInstanceId()));
	}

}