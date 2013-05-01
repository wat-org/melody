package com.wat.cloud.libvirt;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.wat.melody.plugin.libvirt.common.exception.LibVirtException;

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

	private static Log log = LogFactory
			.getLog(LibVirtCloudRestServicesImpl.class);

	@Context
	HttpServletRequest request;

	public LibVirtCloudRestServicesImpl() {
	}

	@GET
	@Produces("text/plain")
	@Path("/GetRegisteredPublicKey")
	public Response getInstanceRegisteredPublicKey() {
		try {
			log.trace("Receive 'GetRegisteredPublicKey' request from remote "
					+ "system with ip '" + request.getRemoteAddr() + "'.");
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
			log.debug("Successfully respond to 'GetRegisteredPublicKey' "
					+ "request initiated by remote system with ip '"
					+ request.getRemoteAddr() + "'.");
			return Response.ok(key).build();
		} catch (LibVirtException Ex) {
			log.debug("Failed to respond to 'GetRegisteredPublicKey' "
					+ "request initiated by remote system with ip '"
					+ request.getRemoteAddr() + "' (" + Ex.getMessage() + ").");
			return Response.ok("undefined").build();
		}
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
