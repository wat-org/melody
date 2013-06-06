package com.wat.melody.cloud.instance;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;

import com.wat.melody.cloud.instance.exception.IllegalInstanceDatasException;
import com.wat.melody.cloud.instance.exception.IllegalInstanceTypeException;
import com.wat.melody.common.keypair.KeyPairName;
import com.wat.melody.common.keypair.KeyPairRepositoryPath;
import com.wat.melody.common.keypair.KeyPairSize;
import com.wat.melody.common.keypair.exception.IllegalKeyPairNameException;
import com.wat.melody.common.keypair.exception.IllegalKeyPairSizeException;
import com.wat.melody.common.keypair.exception.KeyPairRepositoryPathException;
import com.wat.melody.common.timeout.GenericTimeout;
import com.wat.melody.common.timeout.exception.IllegalTimeoutException;
import com.wat.melody.common.xml.FilteredDocHelper;
import com.wat.melody.common.xml.exception.NodeRelatedException;
import com.wat.melody.xpathextensions.XPathHelper;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class InstanceDatasLoader {

	/**
	 * XML attribute of an Instance Element Node, which define the instance id
	 * of the Instance.
	 */
	public static final String INSTANCE_ID_ATTR = "instance-id";

	/**
	 * XML attribute of an Instance Element Node, which define the region where
	 * the Instance is located.
	 */
	public static final String REGION_ATTR = "region";

	/**
	 * XML attribute of an Instance Element Node, which define the site where
	 * the Instance is located.
	 */
	public static final String SITE_ATTR = "site";

	/**
	 * XML attribute of an Instance Element Node, which define the image of the
	 * Instance.
	 */
	public static final String IMAGEID_ATTR = "image-id";

	/**
	 * XML attribute of an Instance Element Node, which define the type of the
	 * Instance.
	 */
	public static final String INSTANCETYPE_ATTR = "instance-type";

	/**
	 * XML attribute of an Instance Element Node, which define the key pair
	 * repository where is located the key pair to associate to the super admin
	 * account of the Instance.
	 */
	public static final String KEYPAIR_REPO_ATTR = "keypair-repository";

	/**
	 * XML attribute of an Instance Element Node, which define the key pair to
	 * associate to the super admin account of the Instance.
	 */
	public static final String KEYPAIR_NAME_ATTR = "keypair-name";

	/**
	 * XML attribute of an Instance Element Node, which define the passphrase of
	 * the key pair which was associate to the super admin account of the
	 * Instance.
	 */
	public static final String PASSPHRASE_ATTR = "passphrase";

	/**
	 * XML attribute of an Instance Element Node, which define the size of the
	 * key pair to associate to the super admin account of the Instance.
	 */
	public static final String KEYPAIR_SIZE_ATTR = "keypair-size";

	/**
	 * XML attribute of an Instance Element Node, which define the timeout of
	 * new instance operation.
	 */
	public static final String TIMEOUT_CREATE_ATTR = "timeout-new";

	/**
	 * XML attribute of an Instance Element Node, which define the timeout of
	 * delete instance operation.
	 */
	public static final String TIMEOUT_DELETE_ATTR = "timeout-delete";

	/**
	 * XML attribute of an Instance Element Node, which define the timeout of
	 * start instance operation.
	 */
	public static final String TIMEOUT_START_ATTR = "timeout-start";

	/**
	 * XML attribute of an Instance Element Node, which define the timeout of
	 * stop instance operation.
	 */
	public static final String TIMEOUT_STOP_ATTR = "timeout-stop";

	public InstanceDatasLoader() {
	}

	private String loadRegion(Element e) throws NodeRelatedException {
		String v = XPathHelper.getHeritedAttributeValue(e, REGION_ATTR);
		if (v == null || v.length() == 0) {
			return null;
		}
		return v;
	}

	private String loadSite(Element e) throws NodeRelatedException {
		String v = XPathHelper.getHeritedAttributeValue(e, SITE_ATTR);
		if (v == null || v.length() == 0) {
			return null;
		}
		return v;
	}

	private String loadImageId(Element e) throws NodeRelatedException {
		String v = XPathHelper.getHeritedAttributeValue(e, IMAGEID_ATTR);
		if (v == null || v.length() == 0) {
			return null;
		}
		return v;
	}

	private InstanceType loadInstanceType(Element e)
			throws NodeRelatedException {
		String v = XPathHelper.getHeritedAttributeValue(e, INSTANCETYPE_ATTR);
		if (v == null || v.length() == 0) {
			return null;
		}
		try {
			return InstanceType.parseString(v);
		} catch (IllegalInstanceTypeException Ex) {
			Attr attr = FilteredDocHelper.getHeritedAttribute(e,
					INSTANCETYPE_ATTR);
			throw new NodeRelatedException(attr, Ex);
		}
	}

	private KeyPairRepositoryPath loadKeyPairRepositoryPath(Element e)
			throws NodeRelatedException {
		String v = XPathHelper.getHeritedAttributeValue(e, KEYPAIR_REPO_ATTR);
		if (v == null || v.length() == 0) {
			return null;
		}
		try {
			return new KeyPairRepositoryPath(v);
		} catch (KeyPairRepositoryPathException Ex) {
			Attr attr = FilteredDocHelper.getHeritedAttribute(e,
					KEYPAIR_REPO_ATTR);
			throw new NodeRelatedException(attr, Ex);
		}
	}

	private KeyPairName loadKeyPairName(Element e) throws NodeRelatedException {
		String v = XPathHelper.getHeritedAttributeValue(e, KEYPAIR_NAME_ATTR);
		if (v == null || v.length() == 0) {
			return null;
		}
		try {
			return KeyPairName.parseString(v);
		} catch (IllegalKeyPairNameException Ex) {
			Attr attr = FilteredDocHelper.getHeritedAttribute(e,
					KEYPAIR_NAME_ATTR);
			throw new NodeRelatedException(attr, Ex);
		}
	}

	private String loadPassphrase(Element e) throws NodeRelatedException {
		String v = XPathHelper.getHeritedAttributeValue(e, PASSPHRASE_ATTR);
		if (v == null || v.length() == 0) {
			return null;
		}
		return v;
	}

	private KeyPairSize loadKeyPairSize(Element e) throws NodeRelatedException {
		String v = XPathHelper.getHeritedAttributeValue(e, KEYPAIR_SIZE_ATTR);
		if (v == null || v.length() == 0) {
			return null;
		}
		try {
			return KeyPairSize.parseString(v);
		} catch (IllegalKeyPairSizeException Ex) {
			Attr attr = FilteredDocHelper.getHeritedAttribute(e,
					KEYPAIR_SIZE_ATTR);
			throw new NodeRelatedException(attr, Ex);
		}
	}

	private GenericTimeout loadCreateTimeout(Element e)
			throws NodeRelatedException {
		String v = XPathHelper.getHeritedAttributeValue(e, TIMEOUT_CREATE_ATTR);
		if (v == null || v.length() == 0) {
			return null;
		}
		try {
			return GenericTimeout.parseString(v);
		} catch (IllegalTimeoutException Ex) {
			Attr attr = FilteredDocHelper.getHeritedAttribute(e,
					TIMEOUT_CREATE_ATTR);
			throw new NodeRelatedException(attr, Ex);
		}
	}

	private GenericTimeout loadDeleteTimeout(Element e)
			throws NodeRelatedException {
		String v = XPathHelper.getHeritedAttributeValue(e, TIMEOUT_DELETE_ATTR);
		if (v == null || v.length() == 0) {
			return null;
		}
		try {
			return GenericTimeout.parseString(v);
		} catch (IllegalTimeoutException Ex) {
			Attr attr = FilteredDocHelper.getHeritedAttribute(e,
					TIMEOUT_DELETE_ATTR);
			throw new NodeRelatedException(attr, Ex);
		}
	}

	private GenericTimeout loadStopTimeout(Element e)
			throws NodeRelatedException {
		String v = XPathHelper.getHeritedAttributeValue(e, TIMEOUT_STOP_ATTR);
		if (v == null || v.length() == 0) {
			return null;
		}
		try {
			return GenericTimeout.parseString(v);
		} catch (IllegalTimeoutException Ex) {
			Attr attr = FilteredDocHelper.getHeritedAttribute(e,
					TIMEOUT_STOP_ATTR);
			throw new NodeRelatedException(attr, Ex);
		}
	}

	private GenericTimeout loadStartTimeout(Element e)
			throws NodeRelatedException {
		String v = XPathHelper.getHeritedAttributeValue(e, TIMEOUT_START_ATTR);
		if (v == null || v.length() == 0) {
			return null;
		}
		try {
			return GenericTimeout.parseString(v);
		} catch (IllegalTimeoutException Ex) {
			Attr attr = FilteredDocHelper.getHeritedAttribute(e,
					TIMEOUT_START_ATTR);
			throw new NodeRelatedException(attr, Ex);
		}
	}

	/**
	 * <p>
	 * Convert the given Instance {@link Element} into a {@link InstanceDatas}.
	 * </p>
	 * 
	 * <p>
	 * An Instance {@link Element} may have the attributes :
	 * <ul>
	 * <li>region : which should contains <tt>String</tt> ;</li>
	 * <li>site : which should contains <tt>String</tt> ;</li>
	 * <li>image-id : which should contains <tt>String</tt> ;</li>
	 * <li>instance-type : which should contains {@link InstanceType} ;</li>
	 * <li>keypair-repository : which should contains
	 * {@link KeyPairRepositoryPath} ;</li>
	 * <li>keypair-name : which should contains {@link KeyPairName} ;</li>
	 * <li>passphrase : which should contains <tt>String</tt> ;</li>
	 * <li>keypair-size : which should contains {@link KeyPairSize} ;</li>
	 * <li>timeout-create : which should contains a {@link GenericTimeout} ;</li>
	 * <li>timeout-attach : which should contains a {@link GenericTimeout} ;</li>
	 * <li>timeout-detach : which should contains a {@link GenericTimeout} ;</li>
	 * <li>herit : which should contains an XPath Expression which refer to
	 * another {@link Element}, which attributes will be used as source ;</li>
	 * </ul>
	 * </p>
	 * 
	 * @param instanceElmt
	 *            is an Instance {@link Element}.
	 * @param validator
	 *            is a call-back, which performs attributes's validation.
	 * 
	 * @return an {@link InstanceDatas} object.
	 * 
	 * @throws IllegalArgumentException
	 *             if the given Instance {@link Element} is <tt>null</tt>.
	 * @throws IllegalArgumentException
	 *             if the given {@link InstanceDatasValidator} is <tt>null</tt>.
	 * @throws NodeRelatedException
	 *             if the conversion failed (ex : the content of an Instance
	 *             {@link Element}'s attribute is not valid, or the 'herit' XML
	 *             attribute is not valid).
	 */
	public InstanceDatas load(Element instanceElmt,
			InstanceDatasValidator validator) throws NodeRelatedException {
		if (instanceElmt == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Element.class.getCanonicalName()
					+ ".");
		}
		if (validator == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ InstanceDatasValidator.class.getCanonicalName() + ".");
		}
		String region = loadRegion(instanceElmt);
		String site = loadSite(instanceElmt);
		String imageId = loadImageId(instanceElmt);
		InstanceType type = loadInstanceType(instanceElmt);
		KeyPairRepositoryPath kprp = loadKeyPairRepositoryPath(instanceElmt);
		KeyPairName kpn = loadKeyPairName(instanceElmt);
		String passphrase = loadPassphrase(instanceElmt);
		KeyPairSize kps = loadKeyPairSize(instanceElmt);
		GenericTimeout createTimeout = loadCreateTimeout(instanceElmt);
		GenericTimeout deleteTimeout = loadDeleteTimeout(instanceElmt);
		GenericTimeout startTimeout = loadStartTimeout(instanceElmt);
		GenericTimeout stopTimeout = loadStopTimeout(instanceElmt);
		try {
			return new InstanceDatas(validator, region, site, imageId, type,
					kprp, kpn, passphrase, kps, createTimeout, deleteTimeout,
					startTimeout, stopTimeout);
		} catch (IllegalInstanceDatasException Ex) {
			throw new NodeRelatedException(instanceElmt, Ex);
		}
	}

}