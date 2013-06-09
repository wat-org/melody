package com.wat.melody.cloud.instance;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;

import com.wat.melody.cloud.disk.DiskDeviceList;
import com.wat.melody.cloud.instance.exception.OperationException;
import com.wat.melody.cloud.network.NetworkDeviceList;
import com.wat.melody.cloud.network.activation.NetworkActivator;
import com.wat.melody.cloud.network.activation.NetworkActivatorConfigurationCallback;
import com.wat.melody.cloud.network.activation.NetworkActivatorFactory;
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
public class InstanceControllerWithNetworkActivation extends
		BaseInstanceController implements InstanceControllerListener {

	private static Log log = LogFactory
			.getLog(InstanceControllerWithNetworkActivation.class);

	private InstanceController _instance;
	private Element _relatedElement;
	private NetworkActivatorConfigurationCallback _confCB;

	public InstanceControllerWithNetworkActivation(InstanceController instance,
			NetworkActivatorConfigurationCallback confCB, Element relatedElement) {
		setInstance(instance);
		setNetworkManagerFactoryConfigurationCallback(confCB);
		setRelatedElement(relatedElement);
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

	private NetworkActivatorConfigurationCallback getNetworkManagerFactoryConfigurationCallback() {
		return _confCB;
	}

	private NetworkActivatorConfigurationCallback setNetworkManagerFactoryConfigurationCallback(
			NetworkActivatorConfigurationCallback confCB) {
		if (confCB == null) {
			throw new IllegalArgumentException(
					"null: Not accepted. Must be a valid "
							+ NetworkActivatorConfigurationCallback.class
									.getCanonicalName() + ".");
		}
		NetworkActivatorConfigurationCallback previous = getNetworkManagerFactoryConfigurationCallback();
		_confCB = confCB;
		return previous;
	}

	private Element getRelatedElement() {
		return _relatedElement;
	}

	private Element setRelatedElement(Element rd) {
		/*
		 * TODO : this class shouldn't be linked to the instance node. It should
		 * be useable without RD (like [Default]InstanceController)
		 */
		if (rd == null) {
			throw new IllegalArgumentException("null: Not accepted."
					+ "Must be a valid " + Element.class.getCanonicalName()
					+ ".");
		}
		Element previous = getRelatedElement();
		_relatedElement = rd;
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
	public void ensureInstanceDiskDevicesAreUpToDate(DiskDeviceList list)
			throws OperationException, InterruptedException {
		getInstance().ensureInstanceDiskDevicesAreUpToDate(list);
	}

	@Override
	public DiskDeviceList getInstanceDiskDevices() {
		return getInstance().getInstanceDiskDevices();
	}

	@Override
	public void ensureInstanceNetworkDevicesAreUpToDate(NetworkDeviceList list)
			throws OperationException, InterruptedException {
		getInstance().ensureInstanceNetworkDevicesAreUpToDate(list);
	}

	@Override
	public NetworkDeviceList getInstanceNetworkDevices() {
		return getInstance().getInstanceNetworkDevices();
	}

	@Override
	public void ensureInstanceFireWallRulesAreUpToDate(
			FireWallRulesPerDevice list) throws OperationException,
			InterruptedException {
		getInstance().ensureInstanceFireWallRulesAreUpToDate(list);
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
		NetworkActivator nm = getNetworkManager();
		if (nm == null) {
			return;
		}
		log.debug(Messages.bind(Messages.InstanceMsg_MANAGEMENT_ENABLE_BEGIN,
				getInstanceId()));

		NetworkDeviceName netdev = nm.getManagementDatas()
				.getNetworkDeviceName();
		Port p = nm.getManagementDatas().getPort();
		PortRange toPorts = null;
		try {
			toPorts = new PortRange(p, p);
		} catch (IllegalPortRangeException Ex) {
			throw new RuntimeException("BUG ! Cannot happened !", Ex);
		}
		SimpleFireWallRule rule = new SimpleTcpFireWallRule(IpRange.ALL,
				PortRange.ALL, IpRange.ALL, toPorts, Direction.IN, Access.ALLOW);
		FireWallRules rules = new FireWallRules();
		FireWallRules currentRules = getInstanceFireWallRules(netdev);
		if (!currentRules.contains(rule)) {
			rules.add(rule);
		}

		authorizeInstanceFireWallRules(netdev, rules);
		try {
			nm.enableNetworkManagement();
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
	public void disableNetworkManagement() throws OperationException,
			InterruptedException {
		NetworkActivator nm = getNetworkManager();
		if (nm == null) {
			return;
		}
		log.debug(Messages.bind(Messages.InstanceMsg_MANAGEMENT_DISABLE_BEGIN,
				getInstanceId()));
		try {
			nm.disableNetworkManagement();
		} catch (NetworkActivationException Ex) {
			throw new OperationException(Messages.bind(
					Messages.InstanceEx_MANAGEMENT_DISABLE_FAILED,
					getInstanceId()), Ex);
		}
		log.info(Messages.bind(Messages.InstanceMsg_MANAGEMENT_DISABLE_SUCCESS,
				getInstanceId()));
	}

	private NetworkActivator getNetworkManager() {
		return NetworkActivatorFactory.createNetworkManager(
				getNetworkManagerFactoryConfigurationCallback(),
				getRelatedElement());
	}

}