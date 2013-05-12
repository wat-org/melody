package com.wat.melody.cloud.instance;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.wat.melody.cloud.disk.DiskDeviceHelper;
import com.wat.melody.cloud.disk.DiskDeviceList;
import com.wat.melody.cloud.disk.exception.DiskDeviceException;
import com.wat.melody.cloud.firewall.FireWallRulesHelper;
import com.wat.melody.cloud.instance.exception.OperationException;
import com.wat.melody.cloud.network.NetworkDeviceHelper;
import com.wat.melody.cloud.network.NetworkDeviceName;
import com.wat.melody.cloud.network.NetworkDeviceNameList;
import com.wat.melody.common.firewall.FireWallRules;
import com.wat.melody.common.firewall.FireWallRulesPerDevice;
import com.wat.melody.common.firewall.Interface;
import com.wat.melody.common.firewall.exception.IllegalInterfaceException;
import com.wat.melody.common.keypair.KeyPairName;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class DefaultInstanceController extends BaseInstanceController {

	private static Log log = LogFactory.getLog(DefaultInstanceController.class);

	private String _instanceId;

	public DefaultInstanceController() {
		super();
	}

	@Override
	public String getInstanceId() {
		return _instanceId;
	}

	/**
	 * Can be <tt>null</tt>, when the instance is not created.
	 * 
	 * @param instance
	 * @return
	 * @throws InterruptedException
	 * @throws OperationException
	 */
	protected String setInstanceId(String instance) {
		String previous = getInstanceId();
		_instanceId = instance;
		return previous;
	}

	@Override
	public boolean isInstanceDefined() {
		return getInstanceId() != null;
	}

	@Override
	public void ensureInstanceIsCreated(InstanceType type, String site,
			String imageId, KeyPairName keyPairName, long createTimeout)
			throws OperationException, InterruptedException {
		if (instanceLives()) {
			log.warn(Messages.bind(Messages.CreateMsg_LIVES, getInstanceId(),
					"LIVE"));
		} else {
			String instanceId = createInstance(type, site, imageId,
					keyPairName, createTimeout);
			setInstanceId(instanceId);
		}
		fireInstanceCreated();
		if (instanceRuns()) {
			// will apply network updates performed in listeners
			fireInstanceStarted();
		}
	}

	public abstract String createInstance(InstanceType type, String site,
			String imageId, KeyPairName keyPairName, long createTimeout)
			throws OperationException, InterruptedException;

	@Override
	public void ensureInstanceIsDestroyed(long destroyTimeout)
			throws OperationException, InterruptedException {
		if (!isInstanceDefined()) {
			log.warn(Messages.DestroyMsg_NO_INSTANCE);
		} else if (!instanceLives()) {
			log.warn(Messages.bind(Messages.DestroyMsg_TERMINATED,
					getInstanceId(), "DEAD"));
		} else {
			destroyInstance(destroyTimeout);
			fireInstanceStopped();
			fireInstanceDestroyed();
			setInstanceId(null);
		}
	}

	public abstract void destroyInstance(long destroyTimeout)
			throws OperationException, InterruptedException;

	@Override
	public void ensureInstanceIsStarted(long startTimeout)
			throws OperationException, InterruptedException {
		InstanceState is = getInstanceState();
		if (!isInstanceDefined()) {
			throw new OperationException(Messages.StartEx_NO_INSTANCE);
		} else if (is == InstanceState.PENDING) {
			log.warn(Messages.bind(Messages.StartMsg_PENDING, getInstanceId(),
					InstanceState.PENDING, InstanceState.RUNNING));
			if (!waitUntilInstanceStatusBecomes(InstanceState.RUNNING,
					startTimeout)) {
				throw new OperationException(Messages.bind(
						Messages.StartEx_WAIT_TO_START_TIMEOUT,
						getInstanceId(), startTimeout));
			}
			fireInstanceStarted();
		} else if (is == InstanceState.RUNNING) {
			fireInstanceStarted();
			log.info(Messages.bind(Messages.StartMsg_RUNNING, getInstanceId(),
					InstanceState.RUNNING));
		} else if (is == InstanceState.STOPPING) {
			log.warn(Messages.bind(Messages.StartMsg_STOPPING, getInstanceId(),
					InstanceState.STOPPING, InstanceState.STOPPED));
			if (!waitUntilInstanceStatusBecomes(InstanceState.STOPPED,
					startTimeout)) {
				throw new OperationException(Messages.bind(
						Messages.StartEx_WAIT_TO_RESTART_TIMEOUT,
						getInstanceId(), startTimeout));
			}
			fireInstanceStopped();
			startInstance(startTimeout);
			fireInstanceStarted();
		} else if (is == InstanceState.SHUTTING_DOWN) {
			fireInstanceStopped();
			throw new OperationException(Messages.bind(
					Messages.StartEx_SHUTTING_DOWN, getInstanceId(),
					InstanceState.SHUTTING_DOWN));
		} else if (is == InstanceState.TERMINATED) {
			fireInstanceStopped();
			fireInstanceDestroyed();
			throw new OperationException(Messages.bind(
					Messages.StartEx_TERMINATED, getInstanceId(),
					InstanceState.TERMINATED));
		} else {
			startInstance(startTimeout);
			fireInstanceStarted();
		}
	}

	public abstract void startInstance(long startTimeout)
			throws OperationException, InterruptedException;

	@Override
	public void ensureInstanceIsStoped(long stopTimeout)
			throws OperationException, InterruptedException {
		if (!isInstanceDefined()) {
			throw new OperationException(Messages.StopEx_NO_INSTANCE);
		} else if (!instanceRuns()) {
			fireInstanceStopped();
			log.warn(Messages.bind(Messages.StopMsg_ALREADY_STOPPED,
					getInstanceId(), InstanceState.STOPPED));
		} else {
			fireInstanceStopped();
			stopInstance(stopTimeout);
		}
	}

	public abstract void stopInstance(long stopTimeout)
			throws OperationException, InterruptedException;

	@Override
	public void ensureInstanceSizing(InstanceType targetType)
			throws OperationException, InterruptedException {
		InstanceState is = getInstanceState();
		if (!isInstanceDefined()) {
			log.warn(Messages.ResizeMsg_NO_INSTANCE);
		} else if (!instanceExists()) {
			fireInstanceStopped();
			fireInstanceDestroyed();
			String sInstanceId = setInstanceId(null);
			throw new OperationException(Messages.bind(
					Messages.ResizeEx_INVALID_INSTANCE_ID, sInstanceId));
		} else if (getInstanceType() == targetType) {
			log.info(Messages.bind(Messages.ResizeMsg_NO_NEED, getInstanceId(),
					targetType));
		} else if (is != InstanceState.STOPPED) {
			throw new OperationException(Messages.bind(
					Messages.ResizeEx_NOT_STOPPED, getInstanceId(),
					InstanceState.STOPPED, is));
		} else {
			resizeInstance(targetType);
		}
	}

	public abstract void resizeInstance(InstanceType targetType)
			throws OperationException, InterruptedException;

	public boolean waitUntilInstanceStatusBecomes(InstanceState state,
			long timeout) throws InterruptedException {
		return waitUntilInstanceStatusBecomes(state, timeout, 0);
	}

	public abstract boolean waitUntilInstanceStatusBecomes(InstanceState state,
			long timeout, long sleepfirst) throws InterruptedException;

	@Override
	public void ensureInstanceDiskDevicesAreUpToDate(
			DiskDeviceList diskDeviceList, long createTimeout,
			long attachTimeout, long detachTimeout) throws OperationException,
			InterruptedException {
		if (!isInstanceDefined()) {
			log.warn(Messages.UpdateDiskDevMsg_NO_INSTANCE);
		} else if (!instanceExists()) {
			fireInstanceStopped();
			fireInstanceDestroyed();
			String sInstanceId = setInstanceId(null);
			throw new OperationException(Messages.bind(
					Messages.UpdateDiskDevEx_INVALID_INSTANCE_ID, sInstanceId));
		} else {
			updateInstanceDiskDevices(diskDeviceList, createTimeout,
					attachTimeout, detachTimeout);
		}
	}

	public void updateInstanceDiskDevices(DiskDeviceList target,
			long createTimeout, long attachTimeout, long detachTimeout)
			throws OperationException, InterruptedException {
		DiskDeviceList current = getInstanceDiskDevices();
		try {
			DiskDeviceHelper.ensureDiskDevicesUpdateIsPossible(current, target);
		} catch (DiskDeviceException Ex) {
			throw new OperationException(Ex);
		}

		DiskDeviceList disksToAdd = null;
		DiskDeviceList disksToRemove = null;
		disksToAdd = DiskDeviceHelper.computeDiskDevicesToAdd(current, target);
		disksToRemove = DiskDeviceHelper.computeDiskDevicesToRemove(current,
				target);

		log.info(Messages.bind(Messages.UpdateDiskDevMsg_DISK_DEVICES_RESUME,
				getInstanceId(), target, disksToAdd, disksToRemove));

		detachAndDeleteInstanceDiskDevices(disksToRemove, detachTimeout);
		createAndAttachDiskInstanceDevices(disksToAdd, createTimeout,
				attachTimeout);

		updateInstanceDiskDevicesDeleteOnTerminationFlag(target);
	}

	public abstract void detachAndDeleteInstanceDiskDevices(
			DiskDeviceList disksToRemove, long detachTimeout)
			throws OperationException, InterruptedException;

	public abstract void createAndAttachDiskInstanceDevices(
			DiskDeviceList disksToAdd, long createTimeout, long attachTimeout)
			throws OperationException, InterruptedException;

	public abstract void updateInstanceDiskDevicesDeleteOnTerminationFlag(
			DiskDeviceList diskList);

	@Override
	public void ensureInstanceNetworkDevicesAreUpToDate(
			NetworkDeviceNameList networkDeviceList, long attachTimeout,
			long detachTimeout) throws OperationException, InterruptedException {
		if (!isInstanceDefined()) {
			fireInstanceStopped();
			log.warn(Messages.UpdateNetDevMsg_NO_INSTANCE);
		} else if (!instanceExists()) {
			fireInstanceStopped();
			fireInstanceDestroyed();
			String sInstanceId = setInstanceId(null);
			throw new OperationException(Messages.bind(
					Messages.UpdateNetDevEx_INVALID_INSTANCE_ID, sInstanceId));
		} else {
			updateInstanceNetworkDevices(networkDeviceList, attachTimeout,
					detachTimeout);
			if (instanceRuns()) {
				fireInstanceStarted();
			}
		}
	}

	public void updateInstanceNetworkDevices(NetworkDeviceNameList target,
			long detachTimeout, long attachTimeout) throws OperationException,
			InterruptedException {
		NetworkDeviceNameList current = getInstanceNetworkDevices();
		NetworkDeviceNameList toAdd = null;
		NetworkDeviceNameList toRemove = null;
		toAdd = NetworkDeviceHelper.computeNetworkDevicesToAdd(current, target);
		toRemove = NetworkDeviceHelper.computeNetworkDevicesToRemove(current,
				target);

		log.info(Messages.bind(Messages.UpdateNetDevMsg_NETWORK_DEVICES_RESUME,
				getInstanceId(), target, toAdd, toRemove));

		detachInstanceNetworkDevices(toRemove, detachTimeout);
		attachInstanceNetworkDevices(toAdd, attachTimeout);
	}

	public abstract void detachInstanceNetworkDevices(
			NetworkDeviceNameList netDevivesToRemove, long detachTimeout)
			throws OperationException, InterruptedException;

	public abstract void attachInstanceNetworkDevices(
			NetworkDeviceNameList netDevivesToAdd, long attachTimeout)
			throws OperationException, InterruptedException;

	@Override
	public void ensureInstanceFireWallRulesAreUpToDate(
			FireWallRulesPerDevice fireWallRules) throws OperationException,
			InterruptedException {
		if (!isInstanceDefined()) {
			log.warn(Messages.UpdateFireWallMsg_NO_INSTANCE);
		} else if (!instanceExists()) {
			fireInstanceStopped();
			fireInstanceDestroyed();
			String sInstanceId = setInstanceId(null);
			throw new OperationException(Messages.bind(
					Messages.UpdateFireWallEx_INVALID_INSTANCE_ID, sInstanceId));
		} else {
			updateInstanceFireWallRules(fireWallRules);
		}
	}

	public void updateInstanceFireWallRules(
			FireWallRulesPerDevice expectedRulesPerDevice)
			throws OperationException, InterruptedException {
		NetworkDeviceNameList netdevs = getInstanceNetworkDevices();
		for (NetworkDeviceName netdev : netdevs) {
			FireWallRules current = getInstanceFireWallRules(netdev);
			FireWallRules expected = getExpectedFireWallRules(
					expectedRulesPerDevice, netdev);
			FireWallRules toAdd = FireWallRulesHelper
					.computeFireWallRulesToAdd(current, expected);
			FireWallRules toRemove = FireWallRulesHelper
					.computeFireWallRulesToRemove(current, expected);

			log.info(Messages.bind(Messages.UpdateFireWallMsg_FWRULES_RESUME,
					getInstanceId(), netdev, expected, toAdd, toRemove));

			revokeInstanceFireWallRules(netdev, toRemove);
			authorizeInstanceFireWallRules(netdev, toAdd);
		}
	}

	private FireWallRules getExpectedFireWallRules(
			FireWallRulesPerDevice rulesPerDevice, NetworkDeviceName netDev) {
		FireWallRules rules = new FireWallRules();
		FireWallRules tmp = rulesPerDevice.get(Interface.ALL);
		if (tmp != null) {
			rules.addAll(tmp);
		}
		try {
			tmp = rulesPerDevice.get(Interface.parseString(netDev.getValue()));
			if (tmp != null) {
				rules.addAll(tmp);
			}
		} catch (IllegalInterfaceException e) {
			throw new RuntimeException("Unexecpted error while creating an "
					+ "Interface from a NetworkDeviceName equals to '" + netDev
					+ "'. "
					+ "Source code has certainly been modified and a bug "
					+ "have been introduced.");
		}
		return rules;
	}

}