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
import com.wat.melody.cloud.network.NetworkManager;
import com.wat.melody.cloud.network.exception.NetworkManagementException;
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

public abstract class Instance {

	private static Log log = LogFactory.getLog(Instance.class);

	public abstract String getInstanceId();

	public abstract DiskDeviceList getDiskDevices();

	public abstract void detachAndDeleteDiskDevices(
			DiskDeviceList disksToRemove, long detachTimeout)
			throws OperationException, InterruptedException;

	public abstract void createAndAttachDiskDevices(DiskDeviceList disksToAdd,
			long createTimeout, long attachTimeout) throws OperationException,
			InterruptedException;

	public abstract void updateDeleteOnTerminationFlag(DiskDeviceList diskList);

	public void updateDiskDevices(DiskDeviceList target, long detachTimeout,
			long createTimeout, long attachTimeout) throws OperationException,
			InterruptedException {
		DiskDeviceList current = getDiskDevices();
		try {
			DiskDeviceHelper.ensureDiskDevicesUpdateIsPossible(current, target);
		} catch (DiskDeviceException Ex) {
			throw new OperationException(Messages.UpdateDiskDevEx_IMPOSSIBLE,
					Ex);
		}

		DiskDeviceList disksToAdd = null;
		DiskDeviceList disksToRemove = null;
		disksToAdd = DiskDeviceHelper.computeDiskDevicesToAdd(current, target);
		disksToRemove = DiskDeviceHelper.computeDiskDevicesToRemove(current,
				target);

		log.info(Messages.bind(Messages.UpdateDiskDevMsg_DISK_DEVICES_RESUME,
				new Object[] { getInstanceId(), target, disksToAdd,
						disksToRemove }));

		detachAndDeleteDiskDevices(disksToRemove, detachTimeout);
		createAndAttachDiskDevices(disksToAdd, createTimeout, attachTimeout);

		updateDeleteOnTerminationFlag(target);
	}

	public abstract NetworkDeviceNameList getNetworkDevices();

	public abstract void detachNetworkDevices(
			NetworkDeviceNameList netDevivesToRemove, long detachTimeout)
			throws OperationException, InterruptedException;

	public abstract void attachNetworkDevices(
			NetworkDeviceNameList netDevivesToAdd, long attachTimeout)
			throws OperationException, InterruptedException;

	public void updateNetworkDevices(NetworkDeviceNameList target,
			long detachTimeout, long attachTimeout) throws OperationException,
			InterruptedException {
		NetworkDeviceNameList current = getNetworkDevices();
		NetworkDeviceNameList toAdd = null;
		NetworkDeviceNameList toRemove = null;
		toAdd = NetworkDeviceHelper.computeNetworkDevicesToAdd(current, target);
		toRemove = NetworkDeviceHelper.computeNetworkDevicesToRemove(current,
				target);

		log.info(Messages.bind(Messages.UpdateNetDevMsg_NETWORK_DEVICES_RESUME,
				new Object[] { getInstanceId(), target, toAdd, toRemove }));

		detachNetworkDevices(toRemove, detachTimeout);
		attachNetworkDevices(toAdd, attachTimeout);
	}

	public abstract FwRulesDecomposed getFireWallRules(NetworkDeviceName netDev);

	public abstract void revokeFireWallRules(NetworkDeviceName netDev,
			FwRulesDecomposed toRevoke) throws OperationException;

	public abstract void authorizeFireWallRules(NetworkDeviceName netDev,
			FwRulesDecomposed toAutorize) throws OperationException;

	public void updateFireWallRules(FwRulesDecomposed target)
			throws OperationException {
		NetworkDeviceNameList netdevs = getNetworkDevices();
		for (NetworkDeviceName netdev : netdevs) {
			FwRulesDecomposed current = getFireWallRules(netdev);
			FwRulesDecomposed devcurrent = getFwRules(target, netdev);
			FwRulesDecomposed toAdd = FireWallRulesHelper
					.computeFireWallRulesToAdd(current, devcurrent);
			FwRulesDecomposed toRemove = FireWallRulesHelper
					.computeFireWallRulesToRemove(current, devcurrent);

			log.info(Messages.bind(Messages.IngressMsg_FWRULES_RESUME,
					new Object[] { getInstanceId(), netdev, devcurrent, toAdd,
							toRemove }));

			revokeFireWallRules(netdev, toRemove);
			authorizeFireWallRules(netdev, toAdd);
		}
	}

	private FwRulesDecomposed getFwRules(FwRulesDecomposed target,
			NetworkDeviceName netDev) {
		FwRulesDecomposed rules = new FwRulesDecomposed();
		for (FwRuleDecomposed rule : target) {
			if (rule.getInterface().getValue().equals(netDev.getValue())
					|| rule.getInterface().equals(Interface.ALL)) {
				rules.add(rule);
			}
		}
		return rules;
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
	public void enableNetworkManagement(NetworkManager mh)
			throws OperationException, InterruptedException {
		if (mh == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ NetworkManager.class.getCanonicalName() + ".");
		}

		if (!mh.getManagementDatas().isManagementEnabled()) {
			return;
		}

		log.debug(Messages.bind(Messages.InstanceMsg_MANAGEMENT_ENABLE_BEGIN,
				getInstanceId()));

		NetworkDeviceName netdev = mh.getManagementDatas()
				.getNetworkDeviceName();
		Port p = mh.getManagementDatas().getPort();
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
		FwRulesDecomposed currentRules = getFireWallRules(netdev);
		if (!currentRules.contains(rule)) {
			rules.add(rule);
		}

		authorizeFireWallRules(netdev, rules);
		try {
			mh.enableNetworkManagement();
		} catch (NetworkManagementException Ex) {
			throw new OperationException(Messages.bind(
					Messages.InstanceEx_MANAGEMENT_ENABLE_FAILED,
					getInstanceId()), Ex);
		} finally {
			revokeFireWallRules(netdev, rules);
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
	public void disableNetworkManagement(NetworkManager mh)
			throws OperationException, InterruptedException {
		if (mh == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ NetworkManager.class.getCanonicalName() + ".");
		}

		if (!mh.getManagementDatas().isManagementEnabled()) {
			return;
		}

		log.debug(Messages.bind(Messages.InstanceMsg_MANAGEMENT_DISABLE_BEGIN,
				getInstanceId()));
		try {
			mh.disableNetworkManagement();
		} catch (NetworkManagementException Ex) {
			throw new OperationException(Messages.bind(
					Messages.InstanceEx_MANAGEMENT_DISABLE_FAILED,
					getInstanceId()), Ex);
		}
		log.info(Messages.bind(Messages.InstanceMsg_MANAGEMENT_DISABLE_SUCCESS,
				getInstanceId()));
	}
}
