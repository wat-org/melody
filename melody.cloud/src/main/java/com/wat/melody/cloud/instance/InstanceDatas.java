package com.wat.melody.cloud.instance;

import com.wat.melody.cloud.instance.exception.IllegalInstanceDatasException;
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

	private InstanceDatasValidator _validator;
	private String _region;
	private String _site;
	private String _imageId;
	private InstanceType _instanceType;
	private KeyPairRepositoryPath _keyPairRepositoryPath;
	private KeyPairName _keyPairName;
	private String _passphrase;
	private KeyPairSize _keyPairSize;
	private GenericTimeout _createTimeout;
	private GenericTimeout _deleteTimeout;
	private GenericTimeout _startTimeout;
	private GenericTimeout _stopTimeout;

	public InstanceDatas(InstanceDatasValidator validator, String region,
			String site, String imageId, InstanceType type,
			KeyPairRepositoryPath kprp, KeyPairName kpn, String passphrase,
			KeyPairSize kps, GenericTimeout createTimeout,
			GenericTimeout deleteTimeout, GenericTimeout startTimeout,
			GenericTimeout stopTimeout) throws IllegalInstanceDatasException {
		setValidator(validator);
		// first set 'region', because members may need it to perform validation
		setRegion(region);
		setSite(site);
		setImageId(imageId);
		setInstanceType(type);
		setKeyPairRepositoryPath(kprp);
		setKeyPairName(kpn);
		setPassphrase(passphrase);
		setKeyPairSize(kps);
		setCreateTimeout(createTimeout);
		setDeleteTimeout(deleteTimeout);
		setStartTimeout(startTimeout);
		setStopTimeout(stopTimeout);
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
		str.append(" }");
		return str.toString();
	}

	public InstanceDatasValidator getValidator() {
		return _validator;
	}

	private InstanceDatasValidator setValidator(InstanceDatasValidator validator) {
		if (validator == null) {
			throw new IllegalArgumentException("null: Not accpeted. "
					+ "Must be a valid "
					+ InstanceDatasValidator.class.getCanonicalName() + ".");
		}
		InstanceDatasValidator previous = getValidator();
		_validator = validator;
		return previous;
	}

	public String getRegion() {
		return _region;
	}

	private String setRegion(String region)
			throws IllegalInstanceDatasException {
		String previous = getRegion();
		_region = getValidator().validateRegion(this, region);
		return previous;
	}

	public String getSite() {
		return _site;
	}

	private String setSite(String site) throws IllegalInstanceDatasException {
		String previous = getSite();
		_site = getValidator().validateSite(this, site);
		return previous;
	}

	public String getImageId() {
		return _imageId;
	}

	private String setImageId(String imageId)
			throws IllegalInstanceDatasException {
		String previous = getImageId();
		_imageId = getValidator().validateImageId(this, imageId);
		return previous;
	}

	public InstanceType getInstanceType() {
		return _instanceType;
	}

	private InstanceType setInstanceType(InstanceType instanceType)
			throws IllegalInstanceDatasException {
		InstanceType previous = getInstanceType();
		_instanceType = getValidator().validateInstanceType(this, instanceType);
		return previous;
	}

	public KeyPairRepositoryPath getKeyPairRepositoryPath() {
		return _keyPairRepositoryPath;
	}

	private KeyPairRepositoryPath setKeyPairRepositoryPath(
			KeyPairRepositoryPath keyPairRepository)
			throws IllegalInstanceDatasException {
		KeyPairRepositoryPath previous = getKeyPairRepositoryPath();
		_keyPairRepositoryPath = getValidator().validateKeyPairRepositoryPath(
				this, keyPairRepository);
		return previous;
	}

	public KeyPairName getKeyPairName() {
		return _keyPairName;
	}

	private KeyPairName setKeyPairName(KeyPairName keyPairName)
			throws IllegalInstanceDatasException {
		KeyPairName previous = getKeyPairName();
		_keyPairName = getValidator().validateKeyPairName(this, keyPairName);
		return previous;
	}

	public String getPassphrase() {
		return _passphrase;
	}

	private String setPassphrase(String passphrase)
			throws IllegalInstanceDatasException {
		String previous = getPassphrase();
		_passphrase = getValidator().validatePassphrase(this, passphrase);
		return previous;
	}

	public KeyPairSize getKeyPairSize() {
		return _keyPairSize;
	}

	private KeyPairSize setKeyPairSize(KeyPairSize keyPairSize)
			throws IllegalInstanceDatasException {
		KeyPairSize previous = getKeyPairSize();
		_keyPairSize = getValidator().validateKeyPairSize(this, keyPairSize);
		return previous;
	}

	public GenericTimeout getCreateTimeout() {
		return _createTimeout;
	}

	private GenericTimeout setCreateTimeout(GenericTimeout timeout)
			throws IllegalInstanceDatasException {
		GenericTimeout previous = getCreateTimeout();
		_createTimeout = getValidator().validateCreateTimeout(this, timeout);
		return previous;
	}

	public GenericTimeout getDeleteTimeout() {
		return _deleteTimeout;
	}

	private GenericTimeout setDeleteTimeout(GenericTimeout timeout)
			throws IllegalInstanceDatasException {
		GenericTimeout previous = getDeleteTimeout();
		_deleteTimeout = getValidator().validateDeleteTimeout(this, timeout);
		return previous;
	}

	public GenericTimeout getStartTimeout() {
		return _startTimeout;
	}

	private GenericTimeout setStartTimeout(GenericTimeout timeout)
			throws IllegalInstanceDatasException {
		GenericTimeout previous = getStartTimeout();
		_startTimeout = getValidator().validateStartTimeout(this, timeout);
		return previous;
	}

	public GenericTimeout getStopTimeout() {
		return _stopTimeout;
	}

	private GenericTimeout setStopTimeout(GenericTimeout timeout)
			throws IllegalInstanceDatasException {
		GenericTimeout previous = getStopTimeout();
		_stopTimeout = getValidator().validateStopTimeout(this, timeout);
		return previous;
	}

}