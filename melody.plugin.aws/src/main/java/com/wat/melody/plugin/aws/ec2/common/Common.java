package com.wat.melody.plugin.aws.ec2.common;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class Common {

	/**
	 * The 'region' XML attribute of the LibVirt Instance Node
	 */
	public static final String REGION_ATTR = "region";

	/**
	 * The 'instanceType' XML attribute of the LibVirt Instance Node
	 */
	public static final String INSTANCETYPE_ATTR = "instance-type";

	/**
	 * The 'imageId' XML attribute of the LibVirt Instance Node
	 */
	public static final String IMAGEID_ATTR = "image-id";

	/**
	 * The 'availabilityZone' XML attribute of the Aws Instance Node
	 */
	public static final String AVAILABILITYZONE_ATTR = "availability-zone";

	/**
	 * The 'keyName' XML attribute of the LibVirt Instance Node
	 */
	public static final String KEYPAIR_NAME_ATTR = "keypair-name";

	/**
	 * The 'passphrase' XML attribute of the LibVirt Instance Node
	 */
	public static final String PASSPHRASE_ATTR = "passphrase";

}