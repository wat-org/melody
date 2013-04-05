package com.wat.melody.cloud.instance;

import com.wat.melody.common.keypair.KeyPairName;
import com.wat.melody.common.keypair.KeyPairRepository;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class InstanceDatas {

	private String msRegion;
	private InstanceType msInstanceType;
	private String msImageId;
	private KeyPairRepository moKeyPairRepository;
	private KeyPairName msKeyPairName;
	private String msPassphrase;
	private String msSite;
	private Long mlCreateTimeout;
	private Long mlDeleteTimeout;
	private Long mlStartTimeout;
	private Long mlStopTimeout;

	public InstanceDatas(String region, InstanceType type, String imageId,
			KeyPairRepository kpr, KeyPairName kpn, String passphrase,
			String site, Long createTimeout, Long deleteTimeout,
			Long startTimeout, Long stopTimeout) {
		setRegion(region);
		setInstanceType(type);
		setImageId(imageId);
		setKeyPairRepository(kpr);
		setKeyPairName(kpn);
		setPassphrase(passphrase);
		setSite(site);
		setCreateTimeout(createTimeout);
		setDeleteTimeout(deleteTimeout);
		setStartTimeout(startTimeout);
		setStopTimeout(stopTimeout);
	}

	public String getRegion() {
		return msRegion;
	}

	private String setRegion(String region) {
		String previous = getRegion();
		msRegion = region;
		return previous;
	}

	public InstanceType getInstanceType() {
		return msInstanceType;
	}

	private InstanceType setInstanceType(InstanceType instanceType) {
		InstanceType previous = getInstanceType();
		msInstanceType = instanceType;
		return previous;
	}

	public String getImageId() {
		return msImageId;
	}

	private String setImageId(String imageId) {
		String previous = getImageId();
		msImageId = imageId;
		return previous;
	}

	public KeyPairRepository getKeyPairRepository() {
		return moKeyPairRepository;
	}

	private KeyPairRepository setKeyPairRepository(
			KeyPairRepository keyPairRepository) {
		KeyPairRepository previous = getKeyPairRepository();
		moKeyPairRepository = keyPairRepository;
		return previous;
	}

	public KeyPairName getKeyPairName() {
		return msKeyPairName;
	}

	private KeyPairName setKeyPairName(KeyPairName keyPairName) {
		KeyPairName previous = getKeyPairName();
		msKeyPairName = keyPairName;
		return previous;
	}

	public String getPassphrase() {
		return msPassphrase;
	}

	private String setPassphrase(String passphrase) {
		String previous = getPassphrase();
		msPassphrase = passphrase;
		return previous;
	}

	public String getSite() {
		return msSite;
	}

	private String setSite(String site) {
		String previous = getSite();
		msSite = site;
		return previous;
	}

	public Long getCreateTimeout() {
		return mlCreateTimeout;
	}

	private Long setCreateTimeout(Long timeout) {
		Long previous = getCreateTimeout();
		mlCreateTimeout = timeout;
		return previous;
	}

	public Long getDeleteTimeout() {
		return mlDeleteTimeout;
	}

	private Long setDeleteTimeout(Long timeout) {
		Long previous = getDeleteTimeout();
		mlDeleteTimeout = timeout;
		return previous;
	}

	public Long getStartTimeout() {
		return mlStartTimeout;
	}

	private Long setStartTimeout(Long timeout) {
		Long previous = getStartTimeout();
		mlStartTimeout = timeout;
		return previous;
	}

	public Long getStopTimeout() {
		return mlStopTimeout;
	}

	private Long setStopTimeout(Long timeout) {
		Long previous = getStopTimeout();
		mlStopTimeout = timeout;
		return previous;
	}

}