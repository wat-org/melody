package com.wat.melody.plugin.libvirt.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.wat.melody.api.annotation.Attribute;
import com.wat.melody.api.exception.ResourcesDescriptorException;
import com.wat.melody.cloud.network.NetworkManager;
import com.wat.melody.cloud.network.NetworkManagerFactory;
import com.wat.melody.cloud.network.exception.ManagementException;
import com.wat.melody.plugin.libvirt.common.exception.LibVirtException;
import com.wat.melody.xpathextensions.common.NetworkManagementHelper;

public abstract class AbstractMachineOperation extends AbstractLibVirtOperation {

	private static Log log = LogFactory.getLog(AbstractMachineOperation.class);

	/**
	 * The 'enableNetworkManagement' XML attribute
	 */
	public static final String ENABLE_NETWORK_MGNT_ATTR = NetworkManagementHelper.ENABLE_NETWORK_MGNT_ATTR;

	/**
	 * The 'enableNetworkManagementTimeout' XML attribute
	 */
	public static final String ENABLE_NETWORK_MGNT_TIMEOUT_ATTR = NetworkManagementHelper.ENABLE_NETWORK_MGNT_TIMEOUT_ATTR;

	private boolean mbEnableNetworkManagement;
	private long mlEnableNetworkManagementTimeout;

	public AbstractMachineOperation() {
		super();
		try {
			setEnableNetworkManagementTimeout(300000);
		} catch (LibVirtException Ex) {
			throw new RuntimeException("Unexpected error while setting "
					+ "the Network Management timeout to '300000'. "
					+ "Because this value is hard coded, such error "
					+ "cannot happened. "
					+ "Source code has certainly been modified and a bug have "
					+ "been introduced.", Ex);
		}
		setEnableNetworkManagement(true);
	}

	@Override
	public void validate() throws LibVirtException {
		super.validate();

		// Network Management found in the RD override Network Management
		// defined in the SD
		try {
			boolean isNetMgmtEnale = NetworkManagementHelper
					.isManagementNetworkEnable(getTargetNode());
			if (isNetMgmtEnale == false) {
				setEnableNetworkManagement(false);
			}
		} catch (ResourcesDescriptorException Ex) {
			throw new LibVirtException(Ex);
		}
	}

	/**
	 * <p>
	 * Based on the underlying operating system of the Instance defined by
	 * {@link #getInstanceID()}, will perform different actions to facilitates
	 * the management of the Instance :
	 * <ul>
	 * <li>If the operating system is Unix/Linux : will add the instance's
	 * HostKey from the Ssh Plug-In KnownHost file ;</li>
	 * <li>If the operating system is Windows : will add the instance's
	 * certificate in the local WinRM Plug-In repo ;</li>
	 * </ul>
	 * </p>
	 * 
	 * @throws LibVirtException
	 * @throws InterruptedException
	 */
	protected void enableNetworkManagement() throws LibVirtException,
			InterruptedException {
		if (getEnableNetworkManagement() == false) {
			return;
		}

		NetworkManager mh = null;
		try {
			mh = NetworkManagerFactory.createNetworkManager(getContext(),
					getTargetNode());
		} catch (ResourcesDescriptorException | ManagementException Ex) {
			throw new LibVirtException(Ex);
		}

		log.debug(Messages.bind(Messages.MachineMsg_MANAGEMENT_ENABLE_BEGIN,
				getInstanceID()));
		try {
			mh.enableNetworkManagement(getEnableNetworkManagementTimeout());
		} catch (ManagementException Ex) {
			throw new LibVirtException(Messages.bind(
					Messages.MachineEx_MANAGEMENT_ENABLE_FAILED,
					getInstanceID(), getTargetNodeLocation()), Ex);
		}
		log.info(Messages.bind(Messages.MachineMsg_MANAGEMENT_ENABLE_SUCCESS,
				getInstanceID()));
	}

	/**
	 * <p>
	 * Based on the underlying operating system of the Instance defined by
	 * {@link #getInstanceID()}, will perform different actions to facilitates
	 * the management of the Instance :
	 * <ul>
	 * <li>If the operating system is Unix/Linux : will remove the instance's
	 * HostKey from the Ssh Plug-In KnownHost file ;</li>
	 * <li>If the operating system is Windows : will remove the instance's
	 * certificate in the local WinRM Plug-In repo ;</li>
	 * </ul>
	 * </p>
	 * 
	 * @throws LibVirtException
	 * @throws InterruptedException
	 */
	protected void disableNetworkManagement() throws LibVirtException,
			InterruptedException {
		if (getEnableNetworkManagement() == false) {
			return;
		}

		NetworkManager mh = null;
		try {
			mh = NetworkManagerFactory.createNetworkManager(getContext(),
					getTargetNode());
		} catch (ResourcesDescriptorException | ManagementException Ex) {
			throw new LibVirtException(Ex);
		}

		log.debug(Messages.bind(Messages.MachineMsg_MANAGEMENT_DISABLE_BEGIN,
				getInstanceID()));
		try {
			mh.disableNetworkManagement();
		} catch (ManagementException Ex) {
			throw new LibVirtException(Messages.bind(
					Messages.MachineEx_MANAGEMENT_DISABLE_FAILED,
					getInstanceID(), getTargetNodeLocation()), Ex);
		}
		log.info(Messages.bind(Messages.MachineMsg_MANAGEMENT_DISABLE_SUCCESS,
				getInstanceID()));
	}

	public boolean getEnableNetworkManagement() {
		return mbEnableNetworkManagement;
	}

	@Attribute(name = ENABLE_NETWORK_MGNT_ATTR)
	public boolean setEnableNetworkManagement(boolean enableManagement) {
		boolean previous = getEnableNetworkManagement();
		mbEnableNetworkManagement = enableManagement;
		return previous;
	}

	public long getEnableNetworkManagementTimeout() {
		return mlEnableNetworkManagementTimeout;
	}

	@Attribute(name = ENABLE_NETWORK_MGNT_TIMEOUT_ATTR)
	public long setEnableNetworkManagementTimeout(long timeout)
			throws LibVirtException {
		if (timeout < 0) {
			throw new LibVirtException(Messages.bind(
					Messages.MachineEx_INVALID_TIMEOUT_ATTR, timeout));
		}
		long previous = getEnableNetworkManagementTimeout();
		mlEnableNetworkManagementTimeout = timeout;
		return previous;
	}

}