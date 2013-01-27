package com.wat.cloud.libvirt;

import org.libvirt.Domain;
import org.libvirt.LibvirtException;

import com.wat.melody.cloud.instance.InstanceType;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class Instance {

	private Domain moDomain;
	private String moMacAddress;
	private InstanceType moType;

	public Instance(Domain d) {
		setDomain(d);
		setInstanceType(LibVirtCloud.getDomainType(d));
		setMacAddress(LibVirtCloud.getDomainMacAddress(d));
	}

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
}
