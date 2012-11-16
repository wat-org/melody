package com.wat.melody.plugin.libvirt.common;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class Common {

	/**
	 * The 'instanceID' XML attribute of the LibVirt instance Node
	 */
	public static final String INSTANCE_ID_ATTR = "instanceId";

	/**
	 * The 'IP_priv' XML attribute of the LibVirt instance Node
	 */
	public static final String IP_PRIV_ATTR = "IP_priv";

	/**
	 * The 'FQDN_priv' XML attribute of the LibVirt Instance Node
	 */
	public static final String FQDN_PRIV_ATTR = "FQDN_priv";

	/**
	 * The 'IP_pub' XML attribute of the LibVirt instance Node
	 */
	public static final String IP_PUB_ATTR = "IP_pub";

	/**
	 * The 'FQDN_pub' XML attribute of the LibVirt Instance Node
	 */
	public static final String FQDN_PUB_ATTR = "FQDN_pub";

	/**
	 * The 'region' XML attribute of the LibVirt Instance Node
	 */
	public static final String REGION_ATTR = "region";

	/**
	 * The 'instanceType' XML attribute of the LibVirt Instance Node
	 */
	public static final String INSTANCETYPE_ATTR = "instanceType";

	/**
	 * The 'imageId' XML attribute of the LibVirt Instance Node
	 */
	public static final String IMAGEID_ATTR = "imageId";

	/**
	 * The 'keyName' XML attribute of the LibVirt Instance Node
	 */
	public static final String KEYPAIR_NAME_ATTR = "keyPairName";
	public static final String KEYPAIR_NAME_PATTERN = "[.\\d\\w-_\\[\\]\\{\\}\\(\\)\\\\ \"']+";

	/**
	 * The 'passphrase' XML attribute of the LibVirt Instance Node
	 */
	public static final String PASSPHRASE_ATTR = "passphrase";

	/**
	 * The 'fwrule' XML Nested element of the LibVirt Instance Node
	 */
	public static final String FWRULE_NE = "fwrule";

	/**
	 * The 'disk' XML Nested element of the LibVirt Instance Node
	 */
	public static final String DISK_NE = "disk";

}
