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
import com.wat.melody.common.network.FwRuleDecomposed;
import com.wat.melody.common.network.FwRulesDecomposed;
import com.wat.melody.common.network.Interface;

public abstract class Instance {

	private static Log log = LogFactory.getLog(Instance.class);

	public abstract String getInstanceId();

	public abstract DiskDeviceList getInstanceDiskDevices();

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
		DiskDeviceList iDisks = getInstanceDiskDevices();
		try {
			DiskDeviceHelper.ensureDiskDevicesUpdateIsPossible(iDisks, target);
		} catch (DiskDeviceException Ex) {
			throw new OperationException(Messages.UpdateDiskDevEx_IMPOSSIBLE,
					Ex);
		}

		DiskDeviceList disksToAdd = null;
		DiskDeviceList disksToRemove = null;
		disksToAdd = DiskDeviceHelper.computeDiskDevicesToAdd(iDisks, target);
		disksToRemove = DiskDeviceHelper.computeDiskDevicesToRemove(iDisks,
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
		NetworkDeviceNameList nds = getNetworkDevices();
		NetworkDeviceNameList toAdd = null;
		NetworkDeviceNameList toRemove = null;
		toAdd = NetworkDeviceHelper.computeNetworkDevicesToAdd(nds, target);
		toRemove = NetworkDeviceHelper.computeNetworkDevicesToRemove(nds,
				target);

		log.info(Messages.bind(Messages.UpdateNetDevMsg_NETWORK_DEVICES_RESUME,
				new Object[] { getInstanceId(), target, toAdd, toRemove }));

		detachNetworkDevices(toRemove, detachTimeout);
		attachNetworkDevices(toAdd, attachTimeout);
	}

	public abstract FwRulesDecomposed getFireWallRules(NetworkDeviceName netDev);

	public abstract void revokeFireWallRules(NetworkDeviceName netdev,
			FwRulesDecomposed toRevoke) throws OperationException;

	public abstract void authorizeFireWallRules(NetworkDeviceName netdev,
			FwRulesDecomposed toAutorize) throws OperationException;

	public void updateFireWallRules(FwRulesDecomposed target)
			throws OperationException {
		NetworkDeviceNameList netdevs = getNetworkDevices();
		for (NetworkDeviceName netdev : netdevs) {
			FwRulesDecomposed currentrules = getFireWallRules(netdev);
			FwRulesDecomposed newrules = getFwRules(target, netdev);
			FwRulesDecomposed toAdd = FireWallRulesHelper
					.computeFireWallRulesToAdd(currentrules, newrules);
			FwRulesDecomposed toRemove = FireWallRulesHelper
					.computeFireWallRulesToRemove(currentrules, newrules);

			log.info(Messages.bind(Messages.IngressMsg_FWRULES_RESUME,
					new Object[] { getInstanceId(), netdev, newrules, toAdd,
							toRemove }));

			revokeFireWallRules(netdev, toRemove);
			authorizeFireWallRules(netdev, toAdd);
		}
	}

	private FwRulesDecomposed getFwRules(FwRulesDecomposed target,
			NetworkDeviceName netdev) {
		FwRulesDecomposed rules = new FwRulesDecomposed();
		for (FwRuleDecomposed rule : target) {
			if (rule.getInterface().getValue().equals(netdev.getValue())
					|| rule.getInterface().equals(Interface.ALL)) {
				rules.add(rule);
			}
		}
		return rules;
	}

}
