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
public interface InstanceDatasValidator {

	public String validateRegion(InstanceDatas datas, String region)
			throws IllegalInstanceDatasException;

	public String validateSite(InstanceDatas datas, String site)
			throws IllegalInstanceDatasException;

	public String validateImageId(InstanceDatas datas, String imageId)
			throws IllegalInstanceDatasException;

	public InstanceType validateInstanceType(InstanceDatas datas,
			InstanceType instanceType) throws IllegalInstanceDatasException;

	public KeyPairRepositoryPath validateKeyPairRepositoryPath(
			InstanceDatas datas, KeyPairRepositoryPath keyPairRepositoryPath)
			throws IllegalInstanceDatasException;

	public KeyPairName validateKeyPairName(InstanceDatas datas,
			KeyPairName keyPairName) throws IllegalInstanceDatasException;

	public String validatePassphrase(InstanceDatas datas, String passphrase)
			throws IllegalInstanceDatasException;

	public KeyPairSize validateKeyPairSize(InstanceDatas datas,
			KeyPairSize keyPairSize) throws IllegalInstanceDatasException;

	public GenericTimeout validateCreateTimeout(InstanceDatas datas,
			GenericTimeout createTimeout) throws IllegalInstanceDatasException;

	public GenericTimeout validateDeleteTimeout(InstanceDatas datas,
			GenericTimeout destroyTimeout) throws IllegalInstanceDatasException;

	public GenericTimeout validateStartTimeout(InstanceDatas datas,
			GenericTimeout startTimeout) throws IllegalInstanceDatasException;

	public GenericTimeout validateStopTimeout(InstanceDatas datas,
			GenericTimeout stopTimeout) throws IllegalInstanceDatasException;

}