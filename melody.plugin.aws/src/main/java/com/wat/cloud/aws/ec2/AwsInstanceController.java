package com.wat.cloud.aws.ec2;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.Instance;
import com.wat.cloud.aws.ec2.exception.WaitVolumeAttachmentStatusException;
import com.wat.cloud.aws.ec2.exception.WaitVolumeStatusException;
import com.wat.melody.cloud.disk.DiskDeviceList;
import com.wat.melody.cloud.instance.DefaultInstanceController;
import com.wat.melody.cloud.instance.InstanceController;
import com.wat.melody.cloud.instance.InstanceState;
import com.wat.melody.cloud.instance.InstanceType;
import com.wat.melody.cloud.instance.exception.OperationException;
import com.wat.melody.cloud.network.NetworkDeviceList;
import com.wat.melody.common.firewall.FireWallRules;
import com.wat.melody.common.firewall.NetworkDeviceName;
import com.wat.melody.common.keypair.KeyPairName;
import com.wat.melody.common.messages.Msg;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class AwsInstanceController extends DefaultInstanceController implements
		InstanceController {

	private AmazonEC2 _cnx;
	private Instance _instance;

	public AwsInstanceController(AmazonEC2 connection, String instanceId) {
		setConnection(connection);
		setInstanceId(instanceId);
	}

	@Override
	public boolean instanceExists() {
		return AwsEc2Cloud.instanceExists(getConnection(), getInstanceId());
	}

	@Override
	public boolean instanceLives() {
		return AwsEc2Cloud.instanceLives(getConnection(), getInstanceId());
	}

	@Override
	public boolean instanceRuns() {
		return AwsEc2Cloud.instanceRuns(getConnection(), getInstanceId());
	}

	@Override
	public InstanceState getInstanceState() {
		return AwsEc2Cloud.getInstanceState(getConnection(), getInstanceId());
	}

	@Override
	public InstanceType getInstanceType() {
		return AwsEc2Cloud.getInstanceType(getInstance());
	}

	@Override
	public boolean waitUntilInstanceStatusBecomes(InstanceState state,
			long timeout, long sleepfirst) throws InterruptedException {
		return AwsEc2Cloud.waitUntilInstanceStatusBecomes(getConnection(),
				getInstanceId(), state, timeout, sleepfirst);
	}

	@Override
	public String createInstance(InstanceType type, String site,
			String imageId, KeyPairName keyPairName, long createTimeout)
			throws OperationException, InterruptedException {
		String instanceId = AwsEc2Cloud.newAwsInstance(getConnection(), type,
				imageId, site, keyPairName);
		// Immediately assign the instanceId
		setInstanceId(instanceId);
		if (!AwsEc2Cloud.waitUntilInstanceStatusBecomes(getConnection(),
				instanceId, InstanceState.RUNNING, createTimeout, 10000)) {
			throw new OperationException(Msg.bind(Messages.CreateEx_TIMEOUT,
					instanceId, createTimeout));
		}
		return instanceId;
	}

	@Override
	public void destroyInstance(long destroyTimeout) throws OperationException,
			InterruptedException {
		if (!AwsEc2Cloud.deleteAwsInstance(getConnection(), getInstance(),
				destroyTimeout)) {
			throw new OperationException(Msg.bind(Messages.DestroyEx_TIMEOUT,
					getInstanceId(), destroyTimeout));
		}
	}

	@Override
	public void startInstance(long startTimeout) throws OperationException,
			InterruptedException {
		if (!AwsEc2Cloud.startAwsInstance(getConnection(), getInstanceId(),
				startTimeout)) {
			throw new OperationException(Msg.bind(Messages.StartEx_TIMEOUT,
					getInstanceId(), startTimeout));
		}
	}

	@Override
	public void stopInstance(long stopTimeout) throws OperationException,
			InterruptedException {
		if (!AwsEc2Cloud.stopAwsInstance(getConnection(), getInstanceId(),
				stopTimeout)) {
			throw new OperationException(Msg.bind(Messages.StopEx_TIMEOUT,
					getInstanceId(), stopTimeout));
		}
	}

	@Override
	public void resizeInstance(InstanceType targetType)
			throws OperationException, InterruptedException {
		if (!AwsEc2Cloud.resizeAwsInstance(getConnection(), getInstanceId(),
				targetType)) {
			throw new OperationException(Msg.bind(Messages.ResizeEx_FAILED,
					getInstanceId(), getInstanceType(), targetType));
		}
	}

	@Override
	public DiskDeviceList getInstanceDiskDevices() {
		return AwsEc2CloudDisk.getInstanceDisks(getConnection(), getInstance());
	}

	@Override
	public void detachAndDeleteInstanceDiskDevices(DiskDeviceList toRemove)
			throws OperationException, InterruptedException {
		try {
			AwsEc2CloudDisk.detachAndDeleteDiskDevices(getConnection(),
					getInstance(), toRemove);
		} catch (WaitVolumeStatusException Ex) {
			throw new OperationException(Msg.bind(
					Messages.UpdateDiskDevEx_DETACH, Ex.getVolumeId(),
					Ex.getDisk(), Ex.getTimeout()), Ex);
		}
	}

	@Override
	public void createAndAttachDiskInstanceDevices(DiskDeviceList toAdd)
			throws OperationException, InterruptedException {
		String sAZ = getInstance().getPlacement().getAvailabilityZone();
		try {
			AwsEc2CloudDisk.createAndAttachDiskDevices(getConnection(),
					getInstanceId(), sAZ, toAdd);
		} catch (WaitVolumeStatusException Ex) {
			throw new OperationException(Msg.bind(
					Messages.UpdateDiskDevEx_CREATE, Ex.getVolumeId(),
					Ex.getDisk(), Ex.getTimeout()), Ex);
		} catch (WaitVolumeAttachmentStatusException Ex) {
			throw new OperationException(Msg.bind(
					Messages.UpdateDiskDevEx_ATTACH, Ex.getVolumeId(),
					Ex.getDisk(), Ex.getTimeout()), Ex);
		}
	}

	@Override
	public void updateInstanceDiskDevicesDeleteOnTerminationFlag(
			DiskDeviceList diskList) {
		AwsEc2CloudDisk.updateDeleteOnTerminationFlag(getConnection(),
				getInstanceId(), diskList);
	}

	@Override
	public NetworkDeviceList getInstanceNetworkDevices() {
		return AwsEc2CloudNetwork.getNetworkDevices(getInstance());
	}

	@Override
	public void detachInstanceNetworkDevices(NetworkDeviceList toRemove)
			throws OperationException, InterruptedException {
		AwsEc2CloudNetwork.detachNetworkDevices(getConnection(), getInstance(),
				toRemove);
	}

	@Override
	public void attachInstanceNetworkDevices(NetworkDeviceList toAdd)
			throws OperationException, InterruptedException {
		AwsEc2CloudNetwork.attachNetworkDevices(getConnection(), getInstance(),
				toAdd);
	}

	@Override
	public FireWallRules getInstanceFireWallRules(NetworkDeviceName netDev) {
		return AwsEc2CloudFireWall.getFireWallRules(getConnection(),
				getInstance(), netDev);
	}

	@Override
	public void revokeInstanceFireWallRules(NetworkDeviceName netDev,
			FireWallRules toRevoke) throws OperationException {
		AwsEc2CloudFireWall.revokeFireWallRules(getConnection(), getInstance(),
				netDev, toRevoke);
	}

	@Override
	public void authorizeInstanceFireWallRules(NetworkDeviceName netDev,
			FireWallRules toAutorize) throws OperationException {
		AwsEc2CloudFireWall.authorizeFireWallRules(getConnection(),
				getInstance(), netDev, toAutorize);
	}

	public void refreshInternalDatas() {
		setInstance(AwsEc2Cloud.getInstance(getConnection(), getInstanceId()));
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
	 * Can be <tt>null</tt>, when the current instance has no underlying 'Amazon
	 * EC2 Instance'.
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
	public String setInstanceId(String instanceId) {
		String previous = super.setInstanceId(instanceId);
		refreshInternalDatas();
		return previous;
	}

}