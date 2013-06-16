package com.wat.cloud.libvirt;

import javax.ws.rs.core.Response;

/**
 * <p>
 * Rest WebService, which allow a LibVirtCloud's virtual machine to retrieve the
 * public key associated to it.
 * </p>
 * 
 * <p>
 * For Linux virtual machine, a specifically designed deamon must be running on
 * the LibVirtCloud's virtual machine. This deamon's role is to deploy the
 * public key which is associated with the virtual machine. The deamon should
 * call the {@link #getInstanceRegisteredPublicKey()} web service in order to
 * retrieve the public key and then put this public key in the super admin
 * account's authorized keys.
 * </p>
 * 
 * <p>
 * For Windows virtual machine, ....
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public interface LibVirtCloudRestServices {

	public Response getInstanceRegisteredPublicKey();

}