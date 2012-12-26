package com.wat.melody.plugin.libvirt.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.wat.melody.api.annotation.Attribute;
import com.wat.melody.cloud.management.ManagementHelper;
import com.wat.melody.cloud.management.ManagementHelperFactory;
import com.wat.melody.cloud.management.exception.ManagementException;
import com.wat.melody.common.utils.Tools;
import com.wat.melody.plugin.libvirt.common.exception.LibVirtException;
import com.wat.melody.xpathextensions.common.ManagementInterfaceHelper;
import com.wat.melody.xpathextensions.common.exception.ResourcesDescriptorException;

public abstract class AbstractMachineOperation extends AbstractLibVirtOperation {

	private static Log log = LogFactory.getLog(AbstractMachineOperation.class);

	/**
	 * The 'enableManagement' XML attribute
	 */
	public static final String ENABLEMGNT_ATTR = ManagementInterfaceHelper.ENABLEMGNT_ATTR;

	/**
	 * The 'enableManagementTimeout' XML attribute
	 */
	public static final String ENABLEMGNT_TIMEOUT_ATTR = ManagementInterfaceHelper.ENABLEMGNT_TIMEOUT_ATTR;

	private boolean mbEnableManagement;
	private long mlEnableManagementTimeout;

	public AbstractMachineOperation() {
		super();
		try {
			setEnableManagementTimeout(300000);
		} catch (LibVirtException Ex) {
			throw new RuntimeException("Unexpected error while setting "
					+ "the management timeout to '300000'. "
					+ "Because this value is hard coded, such error "
					+ "cannot happened. "
					+ "Source code has certainly been modified and a bug have "
					+ "been introduced.", Ex);
		}
		setEnableManagement(true);
	}

	private ManagementHelper buildManagementHelper() throws LibVirtException {
		try {
			return ManagementHelperFactory.getManagementHelper(getContext(),
					getTargetNode());
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
	protected void enableManagement() throws LibVirtException,
			InterruptedException {
		if (getEnableManagement() == false) {
			return;
		}
		ManagementHelper mh = buildManagementHelper();

		log.debug(Messages.bind(Messages.MachineMsg_MANAGEMENT_ENABLE_BEGIN,
				getInstanceID()));
		try {
			mh.enableManagement(getEnableManagementTimeout());
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
	protected void disableManagement() throws LibVirtException,
			InterruptedException {
		if (getEnableManagement() == false) {
			return;
		}
		/*
		 * TODO : should test the validity of the Management-datas (e.g.
		 * instance's ip) before the call to buildManagementHelper. Doing so, we
		 * will not try to disable management if no Management-datas are
		 * present.
		 */
		ManagementHelper mh = null;
		try {
			mh = buildManagementHelper();
		} catch (LibVirtException Ex) {
			// TODO : externalize error message
			log.warn(Tools.getUserFriendlyStackTrace(new LibVirtException(
					"Cannot disable Melody-Management.", Ex)));
			return;
		}

		log.debug(Messages.bind(Messages.MachineMsg_MANAGEMENT_DISABLE_BEGIN,
				getInstanceID()));
		try {
			mh.disableManagement();
		} catch (ManagementException Ex) {
			throw new LibVirtException(Messages.bind(
					Messages.MachineEx_MANAGEMENT_DISABLE_FAILED,
					getInstanceID(), getTargetNodeLocation()), Ex);
		}
		log.info(Messages.bind(Messages.MachineMsg_MANAGEMENT_DISABLE_SUCCESS,
				getInstanceID()));
	}

	public boolean getEnableManagement() {
		return mbEnableManagement;
	}

	@Attribute(name = ENABLEMGNT_ATTR)
	public boolean setEnableManagement(boolean enableManagement) {
		boolean previous = getEnableManagement();
		mbEnableManagement = enableManagement;
		return previous;
	}

	public long getEnableManagementTimeout() {
		return mlEnableManagementTimeout;
	}

	@Attribute(name = ENABLEMGNT_TIMEOUT_ATTR)
	public long setEnableManagementTimeout(long timeout)
			throws LibVirtException {
		if (timeout < 0) {
			throw new LibVirtException(Messages.bind(
					Messages.MachineEx_INVALID_TIMEOUT_ATTR, timeout));
		}
		long previous = getEnableManagementTimeout();
		mlEnableManagementTimeout = timeout;
		return previous;
	}

}