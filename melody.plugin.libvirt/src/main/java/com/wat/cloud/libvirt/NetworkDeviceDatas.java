package com.wat.cloud.libvirt;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class NetworkDeviceDatas {

	private String msIP;
	private String msFQDN;
	private String msMacAddr;

	public NetworkDeviceDatas(String ip, String fqdn, String mac) {
		setIP(ip);
		setFQDN(fqdn);
		setMacAddress(mac);
	}

	public String getIP() {
		return msIP;
	}

	private String setIP(String ip) {
		String previous = getIP();
		msIP = ip;
		return previous;
	}

	public String getFQDN() {
		return msFQDN;
	}

	private String setFQDN(String fqdn) {
		String previous = getFQDN();
		msFQDN = fqdn;
		return previous;
	}

	public String getMacAddress() {
		return msMacAddr;
	}

	private String setMacAddress(String sMacAddress) {
		String previous = getMacAddress();
		msMacAddr = sMacAddress;
		return previous;
	}

}
