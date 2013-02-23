package com.wat.cloud.libvirt;

import org.libvirt.Domain;
import org.libvirt.LibvirtException;

import com.wat.melody.cloud.disk.DiskDeviceList;
import com.wat.melody.cloud.instance.Instance;
import com.wat.melody.cloud.instance.InstanceType;
import com.wat.melody.cloud.instance.exception.OperationException;
import com.wat.melody.cloud.network.NetworkDeviceName;
import com.wat.melody.cloud.network.NetworkDeviceNameList;
import com.wat.melody.common.network.FwRulesDecomposed;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class LibVirtInstance extends Instance {

	private Domain moDomain;
	private String moMacAddress;
	private InstanceType moType;

	public LibVirtInstance(Domain d) {
		setDomain(d);
		setInstanceType(LibVirtCloud.getDomainType(d));
		setMacAddress(LibVirtCloud.getDomainMacAddress(d, null));
	}

	@Override
	public String getInstanceId() {
		try {
			return getDomain().getName();
		} catch (LibvirtException Ex) {
			throw new RuntimeException(Ex);
		}
	}

	public String getPrivateIpAddress() {
		return LibVirtCloud.getDomainIpAddress(getMacAddress());
	}

	public String getPrivateDnsName() {
		return LibVirtCloud.getDomainDnsName(getMacAddress());
	}

	public InstanceType getInstanceType() {
		return moType;
	}

	private void setInstanceType(InstanceType type) {
		if (type == null) {
			throw new IllegalArgumentException("null: Not accepted."
					+ "Must be a valid "
					+ InstanceType.class.getCanonicalName() + ".");
		}
		moType = type;
	}

	public String getMacAddress() {
		return moMacAddress;
	}

	private void setMacAddress(String mac) {
		if (mac == null) {
			throw new IllegalArgumentException("null: Not accepted."
					+ "Must be a valid " + String.class.getCanonicalName()
					+ ".");
		}
		moMacAddress = mac;
	}

	public Domain getDomain() {
		return moDomain;
	}

	private void setDomain(Domain d) {
		if (d == null) {
			throw new IllegalArgumentException("null: Not accepted."
					+ "Must be a valid " + Domain.class.getCanonicalName()
					+ ".");
		}
		moDomain = d;
	}

	@Override
	public DiskDeviceList getDiskDevices() {
		return LibVirtCloud.getDiskDevices(this);
	}

	@Override
	public void detachAndDeleteDiskDevices(DiskDeviceList disksToRemove,
			long detachTimeout) throws OperationException, InterruptedException {
		LibVirtCloud.detachAndDeleteDiskDevices(this, disksToRemove);
	}

	@Override
	public void createAndAttachDiskDevices(DiskDeviceList disksToAdd,
			long createTimeout, long attachTimeout) throws OperationException,
			InterruptedException {
		LibVirtCloud.createAndAttachDiskDevices(this, disksToAdd);
	}

	@Override
	public void updateDeleteOnTerminationFlag(DiskDeviceList diskList) {
		/*
		 * Not supported by LibVirt. Disk are always deleted on instance
		 * termination.
		 */
	}

	@Override
	public NetworkDeviceNameList getNetworkDevices() {
		return LibVirtCloud.getNetworkDevices(this);
	}

	@Override
	public void detachNetworkDevices(NetworkDeviceNameList netDevivesToRemove,
			long detachTimeout) throws OperationException, InterruptedException {
		for (NetworkDeviceName netDev : netDevivesToRemove) {
			LibVirtCloud.detachNetworkDevice(this, netDev);
		}
	}

	@Override
	public void attachNetworkDevices(NetworkDeviceNameList netDevivesToAdd,
			long attachTimeout) throws OperationException, InterruptedException {
		for (NetworkDeviceName netDev : netDevivesToAdd) {
			LibVirtCloud.attachNetworkDevice(this, netDev);
		}
	}

	public FwRulesDecomposed getFireWallRules(NetworkDeviceName netDev) {
		return LibVirtCloud.getFireWallRules(this, netDev);
	}

	public void revokeFireWallRules(NetworkDeviceName netDev,
			FwRulesDecomposed toRevoke) throws OperationException {
		LibVirtCloud.revokeFireWallRules(this, netDev, toRevoke);
	}

	public void authorizeFireWallRules(NetworkDeviceName netDev,
			FwRulesDecomposed toAutorize) throws OperationException {
		LibVirtCloud.authorizeFireWallRules(this, netDev, toAutorize);
	}

}
