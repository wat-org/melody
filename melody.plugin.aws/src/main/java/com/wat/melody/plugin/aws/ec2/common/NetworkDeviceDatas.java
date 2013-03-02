package com.wat.melody.plugin.aws.ec2.common;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class NetworkDeviceDatas {

	private String msIP;
	private String msFQDN;
	private String msNatIP;
	private String msNatFQDN;

	public NetworkDeviceDatas(String ip, String fqdn, String natip,
			String natfqdn) {
		setIP(ip);
		setFQDN(fqdn);
		setNatIP(natip);
		setNatFQDN(natfqdn);
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

	public String getNatIP() {
		return msNatIP;
	}

	private String setNatIP(String ip) {
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