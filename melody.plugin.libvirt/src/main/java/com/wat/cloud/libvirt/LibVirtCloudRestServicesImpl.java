package com.wat.cloud.libvirt;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	private static Logger log = LoggerFactory
			.getLogger(LibVirtCloudRestServicesImpl.class);

	@Context
	HttpServletRequest request;

	public LibVirtCloudRestServicesImpl() {
	}

	@GET
	@Produces("text/plain")
	@Path("/GetRegisteredPublicKey")
	public Response getInstanceRegisteredPublicKey() {
		log.trace("Receive 'GetRegisteredPublicKey' request from remote "
				+ "system '" + request.getRemoteAddr() + "'.");
		String key = null;
		switch (getClientOsFamilly()) {
		case LINUX:
			key = LibVirtCloudKeyPair.getInstancePublicKey(request
					.getRemoteAddr());
			break;
		case WINDOWS:
			key = "Not supported yet";
			break;
		}
		if (key == null) {
			log.debug("Failed to respond to 'GetRegisteredPublicKey' "
					+ "request initiated by remote system '"
					+ request.getRemoteAddr() + "' "
					+ "(system not registered or no keypair associated to).");
			key = "undefined";
		} else {
			log.debug("Successfully respond to 'GetRegisteredPublicKey' "
					+ "request initiated by remote system '"
					+ request.getRemoteAddr() + "'.");
		}
		return Response.ok(key).build();
	}

	private OS_FAMILLY getClientOsFamilly() {
		String userAgent = request.getHeader("User-Agent");
		if (userAgent.contains("linux")) {
			return OS_FAMILLY.LINUX;
		} else if (userAgent.contains("redhat")) {
			return OS_FAMILLY.LINUX;
		} else {
			return OS_FAMILLY.WINDOWS;
		}
	}

	private enum OS_FAMILLY {
		LINUX, WINDOWS
	};

}