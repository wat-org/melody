package com.wat.melody.cloud.instance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wat.melody.cloud.disk.DiskDeviceList;
import com.wat.melody.cloud.instance.exception.OperationException;
import com.wat.melody.cloud.instance.exception.OperationTimeoutException;
import com.wat.melody.cloud.network.NetworkDeviceList;
import com.wat.melody.cloud.network.activation.NetworkActivationDatas;
import com.wat.melody.cloud.network.activation.NetworkActivator;
import com.wat.melody.cloud.network.activation.exception.NetworkActivationException;
import com.wat.melody.cloud.network.activation.exception.NetworkActivationHostUndefined;
import com.wat.melody.cloud.protectedarea.ProtectedAreaIds;
import com.wat.melody.common.firewall.Access;
import com.wat.melody.common.firewall.Direction;
import com.wat.melody.common.firewall.FireWallRules;
import com.wat.melody.common.firewall.FireWallRulesPerDevice;
import com.wat.melody.common.firewall.NetworkDeviceName;
import com.wat.melody.common.firewall.SimpleFireWallRule;
import com.wat.melody.common.firewall.SimpleTcpFireWallRule;
import com.wat.melody.common.keypair.KeyPairName;
import com.wat.melody.common.messages.Msg;
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

	private static Logger log = LoggerFactory
			.getLogger(InstanceControllerWithNetworkActivation.class);

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

		NetworkActivationDatas nad;
		try {
			nad = na.getNetworkActivationDatas();
		} catch (NetworkActivationException Ex) {
			throw new OperationException(Ex);
		}

		log.debug(Msg.bind(Messages.NewtworkActivationMsg_ENABLE_BEGIN,
				getInstanceId()));

		NetworkDeviceName netdev = nad.getNetworkDeviceName();
		Port p = nad.getPort();
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
			throw new OperationException(Msg.bind(
					Messages.NewtworkActivationEx_ENABLE_FAILED,
					getInstanceId()), Ex);
		} finally {
			revokeInstanceFireWallRules(netdev, rules);
		}
		log.info(Msg.bind(Messages.NewtworkActivationMsg_ENABLE_SUCCESS,
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

		try {
			na.getNetworkActivationDatas();
		} catch (NetworkActivationHostUndefined Ex) {
			/*
			 * TODO : when the instance has just been stopped, Activation Host
			 * must be defined. This is an error.
			 * 
			 * when the instance was already stopped, Activation Host is not
			 * defined. This is an not error.
			 * 
			 * when the Network Activation Host points to a non existing
			 * attribute, this is an error.
			 */
			log.debug(Msg.bind(
					Messages.NewtworkActivationMsg_NO_NEED_TO_DISABLE,
					getInstanceId()));
			return;
		} catch (NetworkActivationException Ex) {
			throw new OperationException(Ex);
		}

		log.debug(Msg.bind(Messages.NewtworkActivationMsg_DISABLE_BEGIN,
				getInstanceId()));
		try {
			na.disableNetworkActivation();
		} catch (NetworkActivationException Ex) {
			throw new OperationException(Msg.bind(
					Messages.NewtworkActivationEx_DISABLE_FAILED,
					getInstanceId()), Ex);
		}
		log.info(Msg.bind(Messages.NewtworkActivationMsg_DISABLE_SUCCESS,
				getInstanceId()));
	}

}