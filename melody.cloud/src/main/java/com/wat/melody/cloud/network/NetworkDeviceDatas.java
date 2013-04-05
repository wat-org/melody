package com.wat.melody.cloud.network;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class NetworkDeviceDatas {

	private String msMac;
	private String msIP;
	private String msFQDN;
	private String msNatIP;
	private String msNatFQDN;

	public NetworkDeviceDatas(String mac, String ip, String fqdn, String natip,
			String natfqdn) {
		setMac(mac);
		setIp(ip);
		setFQDN(fqdn);
		setNatIp(natip);
		setNatFQDN(natfqdn);
	}

	public String getMac() {
		return msMac;
	}

	private String setMac(String mac) {
		String previous = getMac();
		msMac = mac;
		return previous;
	}

	public String getIP() {
		return msIP;
	}

	private String setIp(String ip) {
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

	public String getNatIP() {
		return msNatIP;
	}

	private String setNatIp(String ip) {
		String previous = getNatIP();
		msNatIP = ip;
		return previous;
	}

	public String getNatFQDN() {
		return msNatFQDN;
	}

	private String setNatFQDN(String fqdn) {
		String previous = getNatFQDN();
		msNatFQDN = fqdn;
		return previous;
	}

}