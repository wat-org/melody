package com.wat.melody.cloud.instance;

import com.wat.melody.cloud.instance.exception.IllegalInstanceDatasException;
import com.wat.melody.cloud.protectedarea.ProtectedAreaIds;
import com.wat.melody.common.keypair.KeyPairName;
import com.wat.melody.common.keypair.KeyPairRepositoryPath;
import com.wat.melody.common.keypair.KeyPairSize;
import com.wat.melody.common.timeout.GenericTimeout;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class InstanceDatas {

	private String _region;
	private String _site;
	private String _imageId;
	private InstanceType _instanceType;
	private KeyPairRepositoryPath _keyPairRepositoryPath;
	private KeyPairName _keyPairName;
	private String _passphrase;
	private KeyPairSize _keyPairSize;
	private ProtectedAreaIds _protectedAreaIds;
	private GenericTimeout _createTimeout;
	private GenericTimeout _deleteTimeout;
	private GenericTimeout _startTimeout;
	private GenericTimeout _stopTimeout;

	public InstanceDatas(InstanceDatasValidator validator, String region,
			String site, String imageId, InstanceType type,
			KeyPairRepositoryPath kprp, KeyPairName kpn, String passphrase,
			KeyPairSize kps, ProtectedAreaIds protectedAreaIds,
			GenericTimeout createTimeout, GenericTimeout deleteTimeout,
			GenericTimeout startTimeout, GenericTimeout stopTimeout)
			throws IllegalInstanceDatasException {
		setRegion(region);
		setSite(site);
		setImageId(imageId);
		setInstanceType(type);
		setKeyPairRepositoryPath(kprp);
		setKeyPairName(kpn);
		setPassphrase(passphrase);
		setKeyPairSize(kps);
		setProtectedAreaIds(protectedAreaIds);
		setCreateTimeout(createTimeout);
		setDeleteTimeout(deleteTimeout);
		setStartTimeout(startTimeout);
		setStopTimeout(stopTimeout);
		validator.validateAndTransform(this);
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder("{ ");
		str.append("region:");
		str.append(getRegion());
		str.append(", site:");
		str.append(getSite());
		str.append(", image-id:");
		str.append(getImageId());
		str.append(", instance-type:");
		str.append(getInstanceType());
		str.append(", keypair-reposiroty:");
		str.append(getKeyPairRepositoryPath());
		str.append(", keypair-name:");
		str.append(getKeyPairName());
		str.append(", passphrase:");
		str.append(getPassphrase());
		str.append(", keypair-size:");
		str.append(getKeyPairSize());
		str.append(", protected-areas:");
		str.append(getProtectedAreaIds());
		str.append(" }");
		return str.toString();
	}

	public String getRegion() {
		return _region;
	}

	public void setRegion(String region) {
		_region = region;
	}

	public String getSite() {
		return _site;
	}

	public void setSite(String site) {
		_site = site;
	}

	public String getImageId() {
		return _imageId;
	}

	public void setImageId(String imageId) {
		_imageId = imageId;
	}

	public InstanceType getInstanceType() {
		return _instanceType;
	}

	public void setInstanceType(InstanceType instanceType) {
		_instanceType = instanceType;
	}

	public KeyPairRepositoryPath getKeyPairRepositoryPath() {
		return _keyPairRepositoryPath;
	}

	public void setKeyPairRepositoryPath(KeyPairRepositoryPath keyPairRepository) {
		_keyPairRepositoryPath = keyPairRepository;
	}

	public KeyPairName getKeyPairName() {
		return _keyPairName;
	}

	public void setKeyPairName(KeyPairName keyPairName) {
		_keyPairName = keyPairName;
	}

	public String getPassphrase() {
		return _passphrase;
	}

	public void setPassphrase(String passphrase) {
		_passphrase = passphrase;
	}

	public KeyPairSize getKeyPairSize() {
		return _keyPairSize;
	}

	public void setKeyPairSize(KeyPairSize keyPairSize) {
		_keyPairSize = keyPairSize;
	}

	public ProtectedAreaIds getProtectedAreaIds() {
		return _protectedAreaIds;
	}

	public void setProtectedAreaIds(ProtectedAreaIds protectedAreaIds) {
		_protectedAreaIds = protectedAreaIds;
	}

	public GenericTimeout getCreateTimeout() {
		return _createTimeout;
	}

	public void setCreateTimeout(GenericTimeout timeout) {
		_createTimeout = timeout;
	}

	public GenericTimeout getDeleteTimeout() {
		return _deleteTimeout;
	}

	public void setDeleteTimeout(GenericTimeout timeout) {
		_deleteTimeout = timeout;
	}

	public GenericTimeout getStartTimeout() {
		return _startTimeout;
	}

	public void setStartTimeout(GenericTimeout timeout) {
		_startTimeout = timeout;
	}

	public GenericTimeout getStopTimeout() {
		return _stopTimeout;
	}

	public void setStopTimeout(GenericTimeout timeout) {
		_stopTimeout = timeout;
	}

}