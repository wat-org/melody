package com.wat.melody.common.cifs.transfer;

import java.util.List;

import com.wat.melody.common.threads.MelodyThreadFactory;
import com.wat.melody.common.transfer.TemplatingHandler;
import com.wat.melody.common.transfer.TransferMultiThread;
import com.wat.melody.common.transfer.resources.ResourcesSpecification;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class CifsBaseTransferMultiThread extends TransferMultiThread {

	private String _domain;
	private String _username;
	private String _password;
	private String _location;

	public CifsBaseTransferMultiThread(String location, String domain,
			String username, String password, List<ResourcesSpecification> rss,
			int maxPar, TemplatingHandler th, MelodyThreadFactory tf) {
		super(rss, maxPar, th, tf);

		if (location == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ ".");
		}
		_domain = domain;
		_username = username;
		_password = password;
		_location = location;
	}

	@Override
	public String getTransferProtocolDescription() {
		return "cifs";
	}

	protected String getDomain() {
		return _domain;
	}

	protected String getUserName() {
		return _username;
	}

	protected String getPassword() {
		return _password;
	}

	protected String getLocation() {
		return _location;
	}

}