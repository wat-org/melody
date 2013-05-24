package com.wat.melody.cloud.instance;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.wat.melody.api.exception.ResourcesDescriptorException;
import com.wat.melody.cloud.instance.exception.IllegalInstanceTypeException;
import com.wat.melody.common.keypair.KeyPairName;
import com.wat.melody.common.keypair.KeyPairRepositoryPath;
import com.wat.melody.common.keypair.exception.IllegalKeyPairNameException;
import com.wat.melody.common.keypair.exception.KeyPairRepositoryPathException;
import com.wat.melody.common.xml.FilteredDocHelper;
import com.wat.melody.xpathextensions.XPathHelper;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class InstanceDatasLoader {

	/**
	 * XML attribute of an Instance Node, which define the instance id of the
	 * Instance.
	 */
	public static final String INSTANCE_ID_ATTR = "instance-id";

	/**
	 * XML attribute of an Instance Node, which define the region where the
	 * Instance is located.
	 */
	public static final String REGION_ATTR = "region";

	/**
	 * XML attribute of an Instance Node, which define the type of the Instance.
	 */
	public static final String INSTANCETYPE_ATTR = "instance-type";

	/**
	 * XML attribute of an Instance Node, which define the image of the
	 * Instance.
	 */
	public static final String IMAGEID_ATTR = "image-id";

	/**
	 * XML attribute of an Instance Node, which define the key pair repository
	 * where is located the key pair to associate to the super admin account of
	 * the Instance.
	 */
	public static final String KEYPAIR_REPO_ATTR = "keypair-repository";

	/**
	 * XML attribute of an Instance Node, which define the key pair to associate
	 * to the super admin account of the Instance.
	 */
	public static final String KEYPAIR_NAME_ATTR = "keypair-name";

	/**
	 * XML attribute of an Instance Node, which define the passphrase of the key
	 * pair which was associate to the super admin account of the Instance.
	 */
	public static final String PASSPHRASE_ATTR = "passphrase";

	/**
	 * XML attribute of an Instance Node, which define the site where the
	 * Instance is located.
	 */
	public static final String SITE_ATTR = "site";

	/**
	 * XML attribute of an Instance Node, which define the timeout of new
	 * instance operation.
	 */
	public static final String TIMEOUT_CREATE_ATTR = "timeout-new";

	/**
	 * XML attribute of an Instance Node, which define the timeout of delete
	 * instance operation.
	 */
	public static final String TIMEOUT_DELETE_ATTR = "timeout-delete";

	/**
	 * XML attribute of an Instance Node, which define the timeout of start
	 * instance operation.
	 */
	public static final String TIMEOUT_START_ATTR = "timeout-start";

	/**
	 * XML attribute of an Instance Node, which define the timeout of stop
	 * instance operation.
	 */
	public static final String TIMEOUT_STOP_ATTR = "timeout-stop";

	public InstanceDatasLoader() {
	}

	private String loadRegion(Element n) throws ResourcesDescriptorException {
		String v = XPathHelper.getHeritedAttributeValue(n, REGION_ATTR);
		if (v == null || v.length() == 0) {
			return null;
		}
		return v;
	}

	private InstanceType loadInstanceType(Element n)
			throws ResourcesDescriptorException {
		String v = XPathHelper.getHeritedAttributeValue(n, INSTANCETYPE_ATTR);
		if (v == null || v.length() == 0) {
			return null;
		}
		try {
			return InstanceType.parseString(v);
		} catch (IllegalInstanceTypeException Ex) {
			Node attr = FilteredDocHelper.getHeritedAttribute(n,
					INSTANCETYPE_ATTR);
			throw new ResourcesDescriptorException(attr, Ex);
		}
	}

	private String loadImageId(Element n) throws ResourcesDescriptorException {
		String v = XPathHelper.getHeritedAttributeValue(n, IMAGEID_ATTR);
		if (v == null || v.length() == 0) {
			return null;
		}
		return v;
	}

	private KeyPairRepositoryPath loadKeyPairRepositoryPath(Element n)
			throws ResourcesDescriptorException {
		String v = XPathHelper.getHeritedAttributeValue(n, KEYPAIR_REPO_ATTR);
		if (v == null || v.length() == 0) {
			return null;
		}
		try {
			return new KeyPairRepositoryPath(v);
		} catch (KeyPairRepositoryPathException Ex) {
			Node attr = FilteredDocHelper.getHeritedAttribute(n,
					KEYPAIR_REPO_ATTR);
			throw new ResourcesDescriptorException(attr, Ex);
		}
	}

	private KeyPairName loadKeyPairName(Element n)
			throws ResourcesDescriptorException {
		String v = XPathHelper.getHeritedAttributeValue(n, KEYPAIR_NAME_ATTR);
		if (v == null || v.length() == 0) {
			return null;
		}
		try {
			return KeyPairName.parseString(v);
		} catch (IllegalKeyPairNameException Ex) {
			Node attr = FilteredDocHelper.getHeritedAttribute(n,
					KEYPAIR_NAME_ATTR);
			throw new ResourcesDescriptorException(attr, Ex);
		}
	}

	private String loadPassphrase(Element n)
			throws ResourcesDescriptorException {
		String v = XPathHelper.getHeritedAttributeValue(n, PASSPHRASE_ATTR);
		if (v == null || v.length() == 0) {
			return null;
		}
		return v;
	}

	private String loadSite(Element n) throws ResourcesDescriptorException {
		String v = XPathHelper.getHeritedAttributeValue(n, SITE_ATTR);
		if (v == null || v.length() == 0) {
			return null;
		}
		return v;
	}

	private Long loadCreateTimeout(Element n)
			throws ResourcesDescriptorException {
		String v = XPathHelper.getHeritedAttributeValue(n, TIMEOUT_CREATE_ATTR);
		if (v == null || v.length() == 0) {
			return null;
		}
		try {
			return Long.parseLong(v);
		} catch (NumberFormatException Ex) {
			Node attr = FilteredDocHelper.getHeritedAttribute(n,
					KEYPAIR_NAME_ATTR);
			throw new ResourcesDescriptorException(attr, Ex);
		}
	}

	private Long loadDeleteTimeout(Element n)
			throws ResourcesDescriptorException {
		String v = XPathHelper.getHeritedAttributeValue(n, TIMEOUT_DELETE_ATTR);
		if (v == null || v.length() == 0) {
			return null;
		}
		try {
			return Long.parseLong(v);
		} catch (NumberFormatException Ex) {
			Node attr = FilteredDocHelper.getHeritedAttribute(n,
					KEYPAIR_NAME_ATTR);
			throw new ResourcesDescriptorException(attr, Ex);
		}
	}

	private Long loadStopTimeout(Element n) throws ResourcesDescriptorException {
		String v = XPathHelper.getHeritedAttributeValue(n, TIMEOUT_STOP_ATTR);
		if (v == null || v.length() == 0) {
			return null;
		}
		try {
			return Long.parseLong(v);
		} catch (NumberFormatException Ex) {
			Node attr = FilteredDocHelper.getHeritedAttribute(n,
					KEYPAIR_NAME_ATTR);
			throw new ResourcesDescriptorException(attr, Ex);
		}
	}

	private Long loadStartTimeout(Element n)
			throws ResourcesDescriptorException {
		String v = XPathHelper.getHeritedAttributeValue(n, TIMEOUT_START_ATTR);
		if (v == null || v.length() == 0) {
			return null;
		}
		try {
			return Long.parseLong(v);
		} catch (NumberFormatException Ex) {
			Node attr = FilteredDocHelper.getHeritedAttribute(n,
					KEYPAIR_NAME_ATTR);
			throw new ResourcesDescriptorException(attr, Ex);
		}
	}

	public InstanceDatas load(Element instanceNode)
			throws ResourcesDescriptorException {
		String region = loadRegion(instanceNode);
		InstanceType type = loadInstanceType(instanceNode);
		String imageId = loadImageId(instanceNode);
		KeyPairRepositoryPath kprp = loadKeyPairRepositoryPath(instanceNode);
		KeyPairName kpn = loadKeyPairName(instanceNode);
		String passphrase = loadPassphrase(instanceNode);
		String site = loadSite(instanceNode);
		Long createTimeout = loadCreateTimeout(instanceNode);
		Long deleteTimeout = loadDeleteTimeout(instanceNode);
		Long startTimeout = loadStartTimeout(instanceNode);
		Long stopTimeout = loadStopTimeout(instanceNode);
		return new InstanceDatas(region, type, imageId, kprp, kpn, passphrase,
				site, createTimeout, deleteTimeout, startTimeout, stopTimeout);
	}

}
