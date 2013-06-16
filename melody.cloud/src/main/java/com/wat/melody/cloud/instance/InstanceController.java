package com.wat.melody.cloud.instance;

import com.wat.melody.cloud.disk.DiskDeviceList;
import com.wat.melody.cloud.instance.exception.OperationException;
import com.wat.melody.cloud.network.NetworkDeviceList;
import com.wat.melody.common.firewall.FireWallRules;
import com.wat.melody.common.firewall.FireWallRulesPerDevice;
import com.wat.melody.common.firewall.NetworkDeviceName;
import com.wat.melody.common.keypair.KeyPairName;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public interface InstanceController {

	public void addListener(InstanceControllerListener listener);

	public void removeListener(InstanceControllerListener listener);

	public boolean isInstanceDefined();

	public String getInstanceId();

	public boolean instanceExists();

	public boolean instanceLives();

	public boolean instanceRuns();

	public InstanceState getInstanceState();

	public InstanceType getInstanceType();

	public void ensureInstanceIsCreated(InstanceType type, String site,
			String imageId, KeyPairName keyPairName, long createTimeout)
			throws OperationException, InterruptedException;

	public void ensureInstanceIsDestroyed(long destroyTimeout)
			throws OperationException, InterruptedException;

	public void ensureInstanceIsStarted(long startTimeout)
			throws OperationException, InterruptedException;

	public void ensureInstanceIsStoped(long stopTimeout)
			throws OperationException, InterruptedException;

	public void ensureInstanceSizing(InstanceType targetType)
			throws OperationException, InterruptedException;

	public void ensureInstanceDiskDevicesAreUpToDate(DiskDeviceList list)
			throws OperationException, InterruptedException;

	public DiskDeviceList getInstanceDiskDevices();

	public void ensureInstanceNetworkDevicesAreUpToDate(NetworkDeviceList list)
			throws OperationException, InterruptedException;

	public NetworkDeviceList getInstanceNetworkDevices();

	public void ensureInstanceFireWallRulesAreUpToDate(
			FireWallRulesPerDevice list) throws OperationException,
			InterruptedException;

	public void revokeInstanceFireWallRules(NetworkDeviceName netDev,
			FireWallRules toRevoke) throws OperationException,
			InterruptedException;

	public void authorizeInstanceFireWallRules(NetworkDeviceName netDev,
			FireWallRules toAutorize) throws OperationException,
			InterruptedException;

	public FireWallRules getInstanceFireWallRules(NetworkDeviceName netDev);

}