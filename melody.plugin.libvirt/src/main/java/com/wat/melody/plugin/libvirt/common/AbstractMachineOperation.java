package com.wat.melody.plugin.libvirt.common;

import java.util.Arrays;

import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.NodeList;

import com.jcraft.jsch.JSchException;
import com.wat.cloud.libvirt.Instance;
import com.wat.melody.api.annotation.Attribute;
import com.wat.melody.common.network.Host;
import com.wat.melody.common.network.Port;
import com.wat.melody.common.network.exception.IllegalHostException;
import com.wat.melody.common.network.exception.IllegalPortException;
import com.wat.melody.common.utils.exception.NoSuchDUNIDException;
import com.wat.melody.plugin.libvirt.common.exception.IllegalManagementMethodException;
import com.wat.melody.plugin.libvirt.common.exception.LibVirtException;
import com.wat.melody.plugin.ssh.common.Configuration;
import com.wat.melody.plugin.ssh.common.exception.SshException;
import com.wat.melody.xpathextensions.GetHeritedContent;
import com.wat.melody.xpathextensions.common.exception.ResourcesDescriptorException;

public abstract class AbstractMachineOperation extends AbstractLibVirtOperation {

	private static Log log = LogFactory.getLog(AbstractMachineOperation.class);

	/**
	 * The 'enableManagement' XML attribute
	 */
	public static final String ENABLEMGNT_ATTR = "enableManagement";

	/**
	 * The 'enableManagementTimeout' XML attribute
	 */
	public static final String ENABLEMGNT_TIMEOUT_ATTR = "enableManagementTimeout";

	private boolean mbEnableManagement;
	private long mlEnableManagementTimeout;

	public AbstractMachineOperation() {
		super();
		initEnableManagementTimeout();
		initEnableManagement();
	}

	private void initEnableManagementTimeout() {
		mlEnableManagementTimeout = 300000;
	}

	private void initEnableManagement() {
		mbEnableManagement = true;
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
		Instance i = getInstance();
		ManagementMethod mm = findManagementMethodTag();
		log.debug(Messages.bind(Messages.MachineMsg_MANAGEMENT_ENABLE_BEGIN,
				mm, getInstanceID()));
		switch (mm) {
		case SSH:
			enableSshManagement(i);
			break;
		case WINRM:
			enableWinRmManagement(i);
		default:
			throw new RuntimeException("Unexpected error while branching "
					+ "based on the unknown management method '" + mm + "'. "
					+ "Source code has certainly been modified and a bug have "
					+ "been introduced.");
		}
		log.info(Messages.bind(Messages.MachineMsg_MANAGEMENT_ENABLE_SUCCESS,
				mm, getInstanceID()));
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
		Instance i = getInstance();
		if (i == null) {
			return;
		}
		ManagementMethod mm = findManagementMethodTag();
		log.debug(Messages.bind(Messages.MachineMsg_MANAGEMENT_DISABLE_BEGIN,
				mm, getInstanceID()));
		switch (mm) {
		case SSH:
			disableSshManagement(i);
			break;
		case WINRM:
			disableWinRmManagement(i);
		default:
			throw new RuntimeException("Unexpected error while branching "
					+ "based on the unknown management method '" + mm + "'. "
					+ "Source code has certainly been modified and a bug have "
					+ "been introduced.");
		}
		log.info(Messages.bind(Messages.MachineMsg_MANAGEMENT_DISABLE_SUCCESS,
				mm, getInstanceID()));
	}

	public static final String TAG_MGNT = "MGNT";
	public static final String TAG_SSH_PORT = "SSH.PORT";
	public static final String TAG_WINRM_PORT = "WINRM.PORT";

	/**
	 * <p>
	 * Retrieve the {@link ManagementMethod} of the Instance defined by
	 * {@link #getInstanceID()} from the Instance Node's management tag
	 * {@link #TAG_MGNT}.
	 * </p>
	 * 
	 * @return the {@link ManagementMethod} of the Instance defined by
	 *         {@link #getInstanceID()}.
	 * 
	 * @throws LibVirtException
	 *             if the structure of the tag {@link #TAG_MGNT} is not valid
	 *             (ex : no tag, too many tags or invalid tag content).
	 */
	private ManagementMethod findManagementMethodTag() throws LibVirtException {
		NodeList nl = null;
		try {
			nl = GetHeritedContent.getHeritedContent(getTargetNode(),
					"//tags//tag[@name='" + TAG_MGNT + "']/@value");
		} catch (XPathExpressionException Ex) {
			throw new RuntimeException("Unexpected error while evaluating "
					+ "the XPath Expression \"//tags//tag[@name='" + TAG_MGNT
					+ "']/@value\". "
					+ "Because this XPath Expression is hard coded, "
					+ "such error cannot happened. "
					+ "Source code has certainly been modified and a bug have "
					+ "been introduced.", Ex);
		} catch (ResourcesDescriptorException Ex) {
			throw new RuntimeException(Messages.bind(
					Messages.MachineEx_HERIT_ERROR, "",
					getED().getLocation(Ex.getErrorNode()).toFullString()), Ex);
		}
		if (nl.getLength() > 1) {
			throw new LibVirtException(Messages.bind(
					Messages.MachineEx_TOO_MANY_TAG_MGNT,
					new Object[] { TAG_MGNT, ENABLEMGNT_ATTR,
							Arrays.asList(ManagementMethod.values()),
							getTargetNodeLocation() }));
		} else if (nl.getLength() == 0) {
			throw new LibVirtException(Messages.bind(
					Messages.MachineEx_NO_TAG_MGNT,
					new Object[] { TAG_MGNT, ENABLEMGNT_ATTR,
							Arrays.asList(ManagementMethod.values()),
							getTargetNodeLocation() }));
		}
		String val = nl.item(0).getNodeValue();
		try {
			return ManagementMethod.parseString(val);
		} catch (IllegalManagementMethodException Ex) {
			throw new LibVirtException(Messages.bind(
					Messages.MachineEx_INVALID_TAG_MGNT, TAG_MGNT, getED()
							.getLocation(nl.item(0)).toFullString()), Ex);
		}
	}

	/**
	 * <p>
	 * Retrieve the Management {@link Port} of the Instance defined by
	 * {@link #getInstanceID()} from the Instance Node's management tag
	 * {@link #TAG_SSH_PORT} or {@link #TAG_WINRM_PORT}.
	 * </p>
	 * 
	 * @return the Management {@link Port} of the Instance defined by
	 *         {@link #getInstanceID()}.
	 * 
	 * @throws LibVirtException
	 *             if the structure of the tag {@link #TAG_SSH_PORT} or
	 *             {@link #TAG_WINRM_PORT} is not valid (ex : no tag, too many
	 *             tags or invalid tag content).
	 */
	private Port findManagementPortTag(ManagementMethod mm)
			throws LibVirtException {
		String portTag = null;
		switch (mm) {
		case SSH:
			portTag = TAG_SSH_PORT;
			break;
		case WINRM:
			portTag = TAG_WINRM_PORT;
			break;
		default:
			throw new RuntimeException("Unexpected error while branching "
					+ "based on the unknown management method " + mm + ". "
					+ "Source code has certainly been modified and a bug have "
					+ "been introduced.");
		}

		NodeList nl = null;
		try {
			nl = GetHeritedContent.getHeritedContent(getTargetNode(),
					"//tags//tag[@name='" + portTag + "']/@value");
		} catch (XPathExpressionException Ex) {
			throw new RuntimeException("Unexpected error while evaluating "
					+ "the XPath Expression \"//tags//tag[@name='" + portTag
					+ "']/@value\". "
					+ "Because this XPath Expression is hard coded, "
					+ "such error cannot happened. "
					+ "Source code has certainly been modified and a bug have "
					+ "been introduced.", Ex);
		} catch (ResourcesDescriptorException Ex) {
			throw new RuntimeException(Messages.bind(
					Messages.MachineEx_HERIT_ERROR, "",
					getED().getLocation(Ex.getErrorNode()).toFullString()), Ex);
		}
		if (nl.getLength() > 1) {
			throw new LibVirtException(Messages.bind(
					Messages.MachineEx_TOO_MANY_TAG_MGNT_PORT, new Object[] {
							portTag, ENABLEMGNT_ATTR, TAG_MGNT, mm,
							getTargetNodeLocation() }));
		} else if (nl.getLength() == 0) {
			throw new LibVirtException(Messages.bind(
					Messages.MachineEx_NO_TAG_MGNT_PORT, new Object[] {
							portTag, ENABLEMGNT_ATTR, TAG_MGNT, mm,
							getTargetNodeLocation() }));
		}
		String val = nl.item(0).getNodeValue();
		try {
			return Port.parseString(val);
		} catch (IllegalPortException Ex) {
			throw new LibVirtException(Messages.bind(
					Messages.MachineEx_INVALID_TAG_MGNT_PORT, portTag, getED()
							.getLocation(nl.item(0)).toFullString()), Ex);
		}
	}

	private void enableWinRmManagement(Instance i) throws LibVirtException {
		throw new LibVirtException(Messages.bind(
				Messages.MachineEx_INVLIAD_TAG_MGNT_WINRN_SUPPORT,
				new Object[] { TAG_MGNT, ManagementMethod.WINRM,
						getTargetNodeLocation() }));
	}

	private void enableSshManagement(Instance i) throws LibVirtException,
			InterruptedException {
		disableSshManagement(i);

		if (!addMachineToKnownHosts(i)) {
			throw new LibVirtException(Messages.bind(
					Messages.MachineEx_ENABLE_SSH_MGNT_TIMEOUT, new Object[] {
							i.getInstanceId(), ENABLEMGNT_TIMEOUT_ATTR,
							getTargetNodeLocation() }));
		}

		String k = getSshPluginConf().getKnownHostsHostKey(
				i.getPrivateIpAddress()).getKey();
		try {
			getSshPluginConf().addKnownHostsHostKey(i.getPrivateDnsName(), k);
		} catch (JSchException Ex) {
			throw new RuntimeException("Unexpected error while adding an "
					+ "host with the HostKey '" + k + "' into the KnownHosts "
					+ "file. "
					+ "Because this HostKey have been retrieve from the "
					+ "KnownHosts file, this key should be valid. "
					+ "Source code has certainly been modified and a bug "
					+ "have been introduced.", Ex);
		}
	}

	/**
	 * <p>
	 * Add the public IP of the given {@link Instance} to the KnownHosts file
	 * (which is defined in the configuration Ssh Plug-In).
	 * </p>
	 * 
	 * <p>
	 * <i> * After the operation complete, retrieve the HostKey of the
	 * {@link Instance} by calling {@link Configuration#getKnownHostsHostKey} ;
	 * <BR/>
	 * </i>
	 * </p>
	 * 
	 * @param i
	 *            is the {@link Instance} to add to the known hosts file.
	 * 
	 * @return <tt>true</tt> if the operation complete before the timeout
	 *         elapsed, <tt>false</tt> if the operation isn't complete before
	 *         the timeout elapsed.
	 * 
	 * @throws LibVirtException
	 *             if ...
	 * @throws InterruptedException
	 *             if this operation is interrupted.
	 */
	private boolean addMachineToKnownHosts(Instance i) throws LibVirtException,
			InterruptedException {
		Port p = findManagementPortTag(ManagementMethod.SSH);
		Host host = null;
		try {
			host = Host.parseString(i.getPrivateIpAddress());
		} catch (IllegalHostException Ex) {
			throw new RuntimeException(Ex);
		}

		try {
			return getSshPluginConf().addKnownHostsHost(getContext(), host, p,
					getEnableManagementTimeout());
		} catch (SshException Ex) {
			throw new LibVirtException(Ex);
		}
	}

	private void disableWinRmManagement(Instance i) throws LibVirtException {
		throw new LibVirtException(Messages.bind(
				Messages.MachineEx_INVLIAD_TAG_MGNT_WINRN_SUPPORT,
				new Object[] { TAG_MGNT, ManagementMethod.WINRM,
						getTargetNodeLocation() }));
	}

	private void disableSshManagement(Instance i) {
		if (i != null) {
			getSshPluginConf().removeKnownHostsHostKey(i.getPrivateIpAddress());
			getSshPluginConf().removeKnownHostsHostKey(i.getPrivateDnsName());
		}
		try {
			String v = null;
			v = getED().getAttributeValue(getMelodyID(), Common.IP_PRIV_ATTR);
			getSshPluginConf().removeKnownHostsHostKey(v);
			v = getED().getAttributeValue(getMelodyID(), Common.FQDN_PRIV_ATTR);
			getSshPluginConf().removeKnownHostsHostKey(v);
		} catch (NoSuchDUNIDException Ex) {
			throw new RuntimeException("Unexpected error while retrieving a "
					+ "node via its DUNID. " + "No node DUNID match "
					+ getMelodyID() + ". "
					+ "Source code has certainly been modified and a bug "
					+ "have been introduced.", Ex);
		}
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