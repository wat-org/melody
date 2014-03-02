package com.wat.cloud.libvirt;

import org.libvirt.Connect;
import org.libvirt.Domain;

import com.wat.melody.cloud.disk.DiskDeviceList;
import com.wat.melody.cloud.instance.DefaultInstanceController;
import com.wat.melody.cloud.instance.InstanceState;
import com.wat.melody.cloud.instance.InstanceType;
import com.wat.melody.cloud.instance.exception.OperationException;
import com.wat.melody.cloud.network.NetworkDevice;
import com.wat.melody.cloud.network.NetworkDeviceList;
import com.wat.melody.cloud.protectedarea.ProtectedAreaIds;
import com.wat.melody.common.firewall.FireWallRules;
import com.wat.melody.common.firewall.NetworkDeviceName;
import com.wat.melody.common.keypair.KeyPairName;
import com.wat.melody.common.messages.Msg;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class LibVirtInstanceController extends DefaultInstanceController {

	private Connect _cnx;
	private Domain _domain;

	public LibVirtInstanceController(Connect connection, String instanceId) {
		setConnection(connection);
		setInstanceId(instanceId);
	}

	@Override
	public boolean instanceExists() {
		return LibVirtCloud.instanceExists(getConnection(), getInstanceId());
	}

	@Override
	public boolean instanceLives() {
		return LibVirtCloud.instanceLives(getConnection(), getInstanceId());
	}

	@Override
	public boolean instanceRuns() {
		return LibVirtCloud.instanceRuns(getConnection(), getInstanceId());
	}

	@Override
	public InstanceState getInstanceState() {
		return LibVirtCloud.getInstanceState(getConnection(), getInstanceId());
	}

	@Override
	public InstanceType getInstanceType() {
		return LibVirtCloud.getDomainType(getInstance());
	}

	@Override
	public boolean waitUntilInstanceStatusBecomes(InstanceState state,
			long timeout, long sleepfirst) throws InterruptedException {
		return LibVirtCloud.waitUntilInstanceStatusBecomes(getConnection(),
				getInstanceId(), state, timeout, sleepfirst);
	}

	@Override
	public String createInstance(InstanceType type, String site,
			String imageId, KeyPairName keyPairName,
			ProtectedAreaIds protectedAreaIds, long createTimeout)
			throws OperationException, InterruptedException {
		return LibVirtCloud.newInstance(getConnection(), type, imageId,
				keyPairName, protectedAreaIds);
	}

	@Override
	public void destroyInstance(long destroyTimeout) throws OperationException,
			InterruptedException {
		LibVirtCloud.deleteInstance(getInstance());
	}

	@Override
	public void startInstance(long startTimeout) throws OperationException,
			InterruptedException {
		if (!LibVirtCloud.startInstance(getInstance(), startTimeout)) {
			throw new OperationException(Msg.bind(Messages.StartEx_TIMEOUT,
					getInstanceId(), startTimeout));
		}
	}

	@Override
	public void stopInstance(long stopTimeout) throws OperationException,
			InterruptedException {
		if (!LibVirtCloud.stopInstance(getInstance(), stopTimeout)) {
			throw new OperationException(Msg.bind(Messages.StopEx_TIMEOUT,
					getInstanceId(), stopTimeout));
		}
	}

	@Override
	public void resizeInstance(InstanceType targetType)
			throws OperationException, InterruptedException {
		if (!LibVirtCloud.resizeInstance(getInstance(), targetType)) {
			throw new OperationException(Msg.bind(Messages.ResizeEx_FAILED,
					getInstanceId(), getInstanceType(), targetType));
		}
	}

	@Override
	public DiskDeviceList getInstanceDiskDevices() {
		return LibVirtCloudDisk.getDiskDevices(getInstance());
	}

	@Override
	public void detachAndDeleteInstanceDiskDevices(DiskDeviceList toRemove)
			throws OperationException, InterruptedException {
		LibVirtCloudDisk.detachAndDeleteDiskDevices(getInstance(), toRemove);
	}

	@Override
	public void createAndAttachDiskInstanceDevices(DiskDeviceList toAdd)
			throws OperationException, InterruptedException {
		LibVirtCloudDisk.createAndAttachDiskDevices(getInstance(), toAdd);
	}

	@Override
	public void updateInstanceDiskDevicesDeleteOnTerminationFlag(
			DiskDeviceList diskList) {
		/*
		 * Not supported by LibVirt. Disk are always deleted on instance
		 * termination.
		 */
	}

	@Override
	public NetworkDeviceList getInstanceNetworkDevices() {
		return LibVirtCloudNetwork.getNetworkDevices(getInstance());
	}

	@Override
	public void detachInstanceNetworkDevices(NetworkDeviceList toRemove)
			throws OperationException, InterruptedException {
		for (NetworkDevice netdev : toRemove) {
			LibVirtCloudNetwork.detachNetworkDevice(getInstance(), netdev);
		}
	}

	@Override
	public void attachInstanceNetworkDevices(NetworkDeviceList toAdd)
			throws OperationException, InterruptedException {
		for (NetworkDevice netdev : toAdd) {
			LibVirtCloudNetwork.attachNetworkDevice(getInstance(), netdev);
		}
	}

	@Override
	public FireWallRules getInstanceFireWallRules(NetworkDeviceName netdev) {
		return LibVirtCloudFireWall.getFireWallRules(getInstance(), netdev);
	}

	@Override
	public void revokeInstanceFireWallRules(NetworkDeviceName netdev,
			FireWallRules toRevoke) throws OperationException {
		LibVirtCloudFireWall.revokeFireWallRules(getInstance(), netdev,
				toRevoke);
	}

	@Override
	public void authorizeInstanceFireWallRules(NetworkDeviceName netdev,
			FireWallRules toAuthorize) throws OperationException {
		LibVirtCloudFireWall.authorizeFireWallRules(getInstance(), netdev,
				toAuthorize);
	}

	private void refreshInternalDatas() {
		setInstance(LibVirtCloud.getDomain(getConnection(), getInstanceId()));
	}

	public Connect getConnection() {
		return _cnx;
	}

	private Connect setConnection(Connect connection) {
		if (connection == null) {
			throw new IllegalArgumentException("null: Not accepted."
					+ "Must be a valid " + Connect.class.getCanonicalName()
					+ ".");
		}
		Connect previous = getConnection();
		_cnx = connection;
		return previous;
	}

	private Domain getInstance() {
		return _domain;
	}

	/**
	 * Can be <tt>null</tt>, when the current instance has no underlying LibVirt
	 * Domain.
	 * 
	 * @param instance
	 * @return
	 */
	private Domain setInstance(Domain instance) {
		Domain previous = getInstance();
		_domain = instance;
		return previous;
	}

	@Override
	public String setInstanceId(String instanceId) {
		String previous = super.setInstanceId(instanceId);
		refreshInternalDatas();
		return previous;
	}

}