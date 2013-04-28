package com.wat.cloud.libvirt;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

/**
 * <p>
 * Implementation of the {@link LibVirtCloudRestServices}.
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
@Path("/Rest")
public class LibVirtCloudRestServicesImpl implements LibVirtCloudRestServices {

	@Context
	HttpServletRequest request;

	public LibVirtCloudRestServicesImpl() {
	}

	@GET
	@Produces("text/plain")
	@Path("/GetRegisteredPublicKey")
	public Response getInstanceRegisteredPublicKey() {
		String key = LibVirtCloudKeyPair.getInstancePublicKey(request
				.getRemoteAddr());
		return Response.ok(key).build();
	}

}
