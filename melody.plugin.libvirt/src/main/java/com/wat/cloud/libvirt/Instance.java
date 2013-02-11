package com.wat.cloud.libvirt;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import org.libvirt.Domain;
import org.libvirt.LibvirtException;
import org.libvirt.NetworkFilter;
import org.w3c.dom.NodeList;

import com.wat.melody.cloud.instance.InstanceType;
import com.wat.melody.common.ex.MelodyException;
import com.wat.melody.common.xml.Doc;

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

	public List<String> getSecurityGroups() {
		List<String> result = new ArrayList<String>();
		NodeList nl = null;
		try {
			Doc doc = LibVirtCloud.getDomainXMLDesc(getDomain());
			nl = doc.evaluateAsNodeList("/domain/devices/interface[@type='network']/filterref/@filter");
			for (int i = 0; i < nl.getLength(); i++) {
				String filterref = nl.item(i).getNodeValue();
				NetworkFilter nf = getDomain().getConnect()
						.networkFilterLookupByName(filterref);
				Doc filter = new Doc();
				filter.loadFromXML(nf.getXMLDesc());
				result.add(filter.evaluateAsString("//filterref[1]/@filter"));
			}
		} catch (MelodyException | XPathExpressionException | LibvirtException
				| IOException Ex) {
			throw new RuntimeException(Ex);
		}
		return result;
	}
}
