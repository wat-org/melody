package com.wat.melody.cloud.instance;

import com.wat.melody.common.keypair.KeyPairName;
import com.wat.melody.common.keypair.KeyPairRepositoryPath;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class InstanceDatas {

	private String _region;
	private InstanceType _instanceType;
	private String _imageId;
	private KeyPairRepositoryPath _keyPairRepositoryPath;
	private KeyPairName _keyPairName;
	private String _passphrase;
	private String _site;
	private Long _createTimeout;
	private Long _deleteTimeout;
	private Long _startTimeout;
	private Long _stopTimeout;

	public InstanceDatas(String region, InstanceType type, String imageId,
			KeyPairRepositoryPath kprp, KeyPairName kpn, String passphrase,
			String site, Long createTimeout, Long deleteTimeout,
			Long startTimeout, Long stopTimeout) {
		setRegion(region);
		setInstanceType(type);
		setImageId(imageId);
		setKeyPairRepositoryPath(kprp);
		setKeyPairName(kpn);
		setPassphrase(passphrase);
		setSite(site);
		setCreateTimeout(createTimeout);
		setDeleteTimeout(deleteTimeout);
		setStartTimeout(startTimeout);
		setStopTimeout(stopTimeout);
	}

	public String getRegion() {
		return _region;
	}

	private String setRegion(String region) {
		String previous = getRegion();
		_region = region;
		return previous;
	}

	public InstanceType getInstanceType() {
		return _instanceType;
	}

	private InstanceType setInstanceType(InstanceType instanceType) {
		InstanceType previous = getInstanceType();
		_instanceType = instanceType;
		return previous;
	}

	public String getImageId() {
		return _imageId;
	}

	private String setImageId(String imageId) {
		String previous = getImageId();
		_imageId = imageId;
		return previous;
	}

	public KeyPairRepositoryPath getKeyPairRepositoryPath() {
		return _keyPairRepositoryPath;
	}

	private KeyPairRepositoryPath setKeyPairRepositoryPath(
			KeyPairRepositoryPath keyPairRepository) {
		KeyPairRepositoryPath previous = getKeyPairRepositoryPath();
		_keyPairRepositoryPath = keyPairRepository;
		return previous;
	}

	public KeyPairName getKeyPairName() {
		return _keyPairName;
	}

	private KeyPairName setKeyPairName(KeyPairName keyPairName) {
		KeyPairName previous = getKeyPairName();
		_keyPairName = keyPairName;
		return previous;
	}

	public String getPassphrase() {
		return _passphrase;
	}

	private String setPassphrase(String passphrase) {
		String previous = getPassphrase();
		_passphrase = passphrase;
		return previous;
	}

	public String getSite() {
		return _site;
	}

	private String setSite(String site) {
		String previous = getSite();
		_site = site;
		return previous;
	}

	public Long getCreateTimeout() {
		return _createTimeout;
	}

	private Long setCreateTimeout(Long timeout) {
		Long previous = getCreateTimeout();
		_createTimeout = timeout;
		return previous;
	}

	public Long getDeleteTimeout() {
		return _deleteTimeout;
	}

	private Long setDeleteTimeout(Long timeout) {
		Long previous = getDeleteTimeout();
		_deleteTimeout = timeout;
		return previous;
	}

	public Long getStartTimeout() {
		return _startTimeout;
	}

	private Long setStartTimeout(Long timeout) {
		Long previous = getStartTimeout();
		_startTimeout = timeout;
		return previous;
	}

	public Long getStopTimeout() {
		return _stopTimeout;
	}

	private Long setStopTimeout(Long timeout) {
		Long previous = getStopTimeout();
		_stopTimeout = timeout;
		return previous;
	}

}