package com.wat.melody.plugin.aws.ec2.common;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.Instance;
import com.wat.melody.cloud.disk.DiskDeviceList;
import com.wat.melody.cloud.instance.exception.OperationException;
import com.wat.melody.cloud.network.NetworkDeviceName;
import com.wat.melody.cloud.network.NetworkDeviceNameList;
import com.wat.melody.common.network.FwRulesDecomposed;
import com.wat.melody.plugin.aws.ec2.common.exception.AwsException;
import com.wat.melody.plugin.aws.ec2.common.exception.WaitVolumeAttachmentStatusException;
import com.wat.melody.plugin.aws.ec2.common.exception.WaitVolumeStatusException;

public class AwsInstance extends com.wat.melody.cloud.instance.Instance {

	private Instance _instance;
	private AmazonEC2 _ec2;

	public AwsInstance(AmazonEC2 ec2, Instance instance) {
		if (ec2 == null) {
			throw new IllegalArgumentException("null: Not accepted."
					+ "Must be a valid " + AmazonEC2.class.getCanonicalName()
					+ ".");
		}
		if (instance == null) {
			throw new IllegalArgumentException("null: Not accepted."
					+ "Must be a valid " + Instance.class.getCanonicalName()
					+ ".");
		}
		_ec2 = ec2;
		_instance = instance;
	}

	public Instance getInstance() {
		return _instance;
	}

	@Override
	public String getInstanceId() {
		return _instance.getInstanceId();
	}

	@Override
	public DiskDeviceList getDiskDevices() {
		return Common.getInstanceDisks(_ec2, _instance);
	}

	@Override
	public void detachAndDeleteDiskDevices(DiskDeviceList disksToRemove,
			long detachTimeout) throws OperationException, InterruptedException {
		try {
			Common.detachAndDeleteDiskDevices(_ec2, _instance, disksToRemove,
					detachTimeout);
		} catch (WaitVolumeStatusException Ex) {
			throw new OperationException(Messages.bind(
					Messages.UpdateDiskDevEx_DETACH,
					new Object[] { Ex.getVolumeId(), Ex.getDisk(),
							Ex.getTimeout() }), Ex);
		}
	}

	@Override
	public void createAndAttachDiskDevices(DiskDeviceList disksToAdd,
			long createTimeout, long attachTimeout) throws OperationException,
			InterruptedException {
		String sAZ = _instance.getPlacement().getAvailabilityZone();
		try {
			Common.createAndAttachDiskDevices(_ec2, getInstanceId(), sAZ,
					disksToAdd, createTimeout, attachTimeout);
		} catch (WaitVolumeStatusException Ex) {
			throw new OperationException(Messages.bind(
					Messages.UpdateDiskDevEx_CREATE,
					new Object[] { Ex.getVolumeId(), Ex.getDisk(),
							Ex.getTimeout() }), Ex);
		} catch (WaitVolumeAttachmentStatusException Ex) {
			throw new OperationException(Messages.bind(
					Messages.UpdateDiskDevEx_ATTACH,
					new Object[] { Ex.getVolumeId(), Ex.getDisk(),
							Ex.getTimeout() }), Ex);
		}
	}

	@Override
	public void updateDeleteOnTerminationFlag(DiskDeviceList diskList) {
		Common.updateDeleteOnTerminationFlag(_ec2, getInstanceId(), diskList);
	}

	@Override
	public NetworkDeviceNameList getNetworkDevices() {
		return Common.getNetworkDevices(_ec2, _instance);
	}

	@Override
	public void detachNetworkDevices(NetworkDeviceNameList netDevivesToRemove,
			long detachTimeout) throws OperationException, InterruptedException {
		try {
			Common.detachNetworkDevices(_ec2, _instance, netDevivesToRemove,
					detachTimeout);
		} catch (AwsException Ex) {
			throw new OperationException(Ex);
		}
	}

	@Override
	public void attachNetworkDevices(NetworkDeviceNameList netDevivesToAdd,
			long attachTimeout) throws OperationException, InterruptedException {
		try {
			Common.attachNetworkDevices(_ec2, _instance, netDevivesToAdd,
					attachTimeout);
		} catch (AwsException Ex) {
			throw new OperationException(Ex);
		}
	}

	@Override
	public FwRulesDecomposed getFireWallRules(NetworkDeviceName netDev) {
		return Common.getFireWallRules(_ec2, _instance, netDev);
	}

	@Override
	public void revokeFireWallRules(NetworkDeviceName netDev,
			FwRulesDecomposed toRevoke) throws OperationException {
		Common.revokeFireWallRules(_ec2, _instance, netDev, toRevoke);
	}

	@Override
	public void authorizeFireWallRules(NetworkDeviceName netDev,
			FwRulesDecomposed toAutorize) throws OperationException {
		Common.authorizeFireWallRules(_ec2, _instance, netDev, toAutorize);
	}

}
