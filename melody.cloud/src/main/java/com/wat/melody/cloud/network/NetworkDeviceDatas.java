package com.wat.melody.cloud.network;

public class NetworkDeviceDatas {

	private String msIP;
	private String msFQDN;
	private String msMacAddr;

	public NetworkDeviceDatas() {

	}

	public String getIP() {
		return msIP;
	}

	public String setIP(String ip) {
		String previous = getIP();
		msIP = ip;
		return previous;
	}

	public String getFQDN() {
		return msFQDN;
	}

	public String setFQDN(String fqdn) {
		String previous = getFQDN();
		msFQDN = fqdn;
		return previous;
	}

	public String getMacAddress() {
		return msMacAddr;
	}

	public String setMacAddress(String sMacAddress) {
		String previous = getMacAddress();
		msMacAddr = sMacAddress;
		return previous;
	}

}
