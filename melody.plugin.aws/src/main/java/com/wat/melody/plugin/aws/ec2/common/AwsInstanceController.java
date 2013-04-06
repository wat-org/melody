package com.wat.melody.plugin.aws.ec2.common;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.Instance;
import com.wat.melody.cloud.disk.DiskDeviceList;
import com.wat.melody.cloud.instance.DefaultInstanceController;
import com.wat.melody.cloud.instance.InstanceController;
import com.wat.melody.cloud.instance.InstanceState;
import com.wat.melody.cloud.instance.InstanceType;
import com.wat.melody.cloud.instance.exception.OperationException;
import com.wat.melody.cloud.network.NetworkDeviceDatas;
import com.wat.melody.cloud.network.NetworkDeviceName;
import com.wat.melody.cloud.network.NetworkDeviceNameList;
import com.wat.melody.common.keypair.KeyPairName;
import com.wat.melody.common.network.FwRulesDecomposed;
import com.wat.melody.plugin.aws.ec2.common.exception.WaitVolumeAttachmentStatusException;
import com.wat.melody.plugin.aws.ec2.common.exception.WaitVolumeStatusException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class AwsInstanceController extends DefaultInstanceController implements
		InstanceController {

	private AmazonEC2 _cnx;
	private Instance _instance;

	public AwsInstanceController(AmazonEC2 connection, String instanceId)
			throws OperationException {
		setConnection(connection);
		setInstanceId(instanceId);
	}

	@Override
	public boolean instanceExists() {
		return Common.instanceExists(getConnection(), getInstanceId());
	}

	@Override
	public boolean instanceLives() {
		return Common.instanceLives(getConnection(), getInstanceId());
	}

	@Override
	public boolean instanceRuns() {
		return Common.instanceRuns(getConnection(), getInstanceId());
	}

	@Override
	public InstanceState getInstanceState() {
		return Common.getInstanceState(getConnection(), getInstanceId());
	}

	@Override
	public InstanceType getInstanceType() {
		return Common.getInstanceType(getInstance());
	}

	@Override
	public boolean waitUntilInstanceStatusBecomes(InstanceState state,
			long timeout, long sleepfirst) throws InterruptedException {
		return Common.waitUntilInstanceStatusBecomes(getConnection(),
				getInstanceId(), state, timeout, sleepfirst);
	}

	@Override
	public String createInstance(InstanceType type, String site,
			String imageId, KeyPairName keyPairName, long createTimeout)
			throws OperationException, InterruptedException {
		String instanceId = Common.newAwsInstance(getConnection(), type,
				imageId, site, keyPairName);
		// Immediately assign the instanceId
		setInstanceId(instanceId);
		if (!Common.waitUntilInstanceStatusBecomes(getConnection(), instanceId,
				InstanceState.RUNNING, createTimeout, 10000)) {
			throw new OperationException(Messages.bind(
					Messages.CreateEx_TIMEOUT, instanceId, createTimeout));
		}
		return instanceId;
	}

	@Override
	public void destroyInstance(long destroyTimeout) throws OperationException,
			InterruptedException {
		if (!Common.deleteAwsInstance(getConnection(), getInstance(),
				destroyTimeout)) {
			throw new OperationException(
					Messages.bind(Messages.DestroyEx_TIMEOUT, getInstanceId(),
							destroyTimeout));
		}
	}

	@Override
	public void startInstance(long startTimeout) throws OperationException,
			InterruptedException {
		if (!Common.startAwsInstance(getConnection(), getInstanceId(),
				startTimeout)) {
			throw new OperationException(Messages.bind(
					Messages.StartEx_TIMEOUT, getInstanceId(), startTimeout));
		}
	}

	@Override
	public void stopInstance(long stopTimeout) throws OperationException,
			InterruptedException {
		if (!Common.stopAwsInstance(getConnection(), getInstanceId(),
				stopTimeout)) {
			throw new OperationException(Messages.bind(Messages.StopEx_TIMEOUT,
					getInstanceId(), stopTimeout));
		}
	}

	@Override
	public DiskDeviceList getInstanceDiskDevices() {
		return Common.getInstanceDisks(getConnection(), getInstance());
	}

	@Override
	public void detachAndDeleteInstanceDiskDevices(
			DiskDeviceList disksToRemove, long detachTimeout)
			throws OperationException, InterruptedException {
		try {
			Common.detachAndDeleteDiskDevices(getConnection(), getInstance(),
					disksToRemove, detachTimeout);
		} catch (WaitVolumeStatusException Ex) {
			throw new OperationException(Messages.bind(
					Messages.UpdateDiskDevEx_DETACH,
					new Object[] { Ex.getVolumeId(), Ex.getDisk(),
							Ex.getTimeout() }), Ex);
		}
	}

	@Override
	public void createAndAttachDiskInstanceDevices(DiskDeviceList disksToAdd,
			long createTimeout, long attachTimeout) throws OperationException,
			InterruptedException {
		String sAZ = getInstance().getPlacement().getAvailabilityZone();
		try {
			Common.createAndAttachDiskDevices(getConnection(), getInstanceId(),
					sAZ, disksToAdd, createTimeout, attachTimeout);
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
	public void updateInstanceDiskDevicesDeleteOnTerminationFlag(
			DiskDeviceList diskList) {
		Common.updateDeleteOnTerminationFlag(getConnection(), getInstanceId(),
				diskList);
	}

	@Override
	public NetworkDeviceNameList getInstanceNetworkDevices() {
		return Common.getNetworkDevices(getConnection(), getInstance());
	}

	@Override
	public void detachInstanceNetworkDevices(
			NetworkDeviceNameList netDevivesToRemove, long detachTimeout)
			throws OperationException, InterruptedException {
		Common.detachNetworkDevices(getConnection(), getInstance(),
				netDevivesToRemove, detachTimeout);
	}

	@Override
	public void attachInstanceNetworkDevices(
			NetworkDeviceNameList netDevivesToAdd, long attachTimeout)
			throws OperationException, InterruptedException {
		Common.attachNetworkDevices(getConnection(), getInstance(),
				netDevivesToAdd, attachTimeout);
	}

	@Override
	public NetworkDeviceDatas getInstanceNetworkDeviceDatas(
			NetworkDeviceName netdev) {
		return Common.getNetworkDeviceDatas(getConnection(),
				this.getInstance(), netdev);
	}

	@Override
	public FwRulesDecomposed getInstanceFireWallRules(NetworkDeviceName netDev) {
		return Common.getFireWallRules(getConnection(), getInstance(), netDev);
	}

	@Override
	public void revokeInstanceFireWallRules(NetworkDeviceName netDev,
			FwRulesDecomposed toRevoke) throws OperationException {
		Common.revokeFireWallRules(getConnection(), getInstance(), netDev,
				toRevoke);
	}

	@Override
	public void authorizeInstanceFireWallRules(NetworkDeviceName netDev,
			FwRulesDecomposed toAutorize) throws OperationException {
		Common.authorizeFireWallRules(getConnection(), getInstance(), netDev,
				toAutorize);
	}

	public void refreshInternalDatas() {
		setInstance(Common.getInstance(getConnection(), getInstanceId()));
	}

	public AmazonEC2 getConnection() {
		return _cnx;
	}

	private AmazonEC2 setConnection(AmazonEC2 connection) {
		if (connection == null) {
			throw new IllegalArgumentException("null: Not accepted."
					+ "Must be a valid " + AmazonEC2.class.getCanonicalName()
					+ ".");
		}
		AmazonEC2 previous = getConnection();
		_cnx = connection;
		return previous;
	}

	private Instance getInstance() {
		return _instance;
	}

	/**
	 * Can be <tt>null</tt>, when the current instance has no underlying LibVirt
	 * Domain.
	 * 
	 * @param instance
	 * @return
	 */
	private Instance setInstance(Instance instance) {
		Instance previous = getInstance();
		_instance = instance;
		return previous;
	}

	@Override
	public String setInstanceId(String instanceId) throws OperationException {
		String previous = super.setInstanceId(instanceId);
		refreshInternalDatas();
		return previous;
	}

}
