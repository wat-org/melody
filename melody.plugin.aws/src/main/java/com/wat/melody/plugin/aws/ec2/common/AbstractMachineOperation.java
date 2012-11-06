package com.wat.melody.plugin.aws.ec2.common;

import java.io.File;
import java.io.IOException;
import java.security.KeyPair;
import java.util.Arrays;

import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.NodeList;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.ec2.model.AuthorizeSecurityGroupIngressRequest;
import com.amazonaws.services.ec2.model.Image;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.IpPermission;
import com.amazonaws.services.ec2.model.RevokeSecurityGroupIngressRequest;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.wat.melody.api.annotation.Attribute;
import com.wat.melody.api.exception.ResourcesDescriptorException;
import com.wat.melody.common.network.Host;
import com.wat.melody.common.network.IpRange;
import com.wat.melody.common.network.Port;
import com.wat.melody.common.network.Protocol;
import com.wat.melody.common.network.exception.IllegalHostException;
import com.wat.melody.common.network.exception.IllegalPortException;
import com.wat.melody.common.utils.exception.IllegalDirectoryException;
import com.wat.melody.common.utils.exception.NoSuchDUNIDException;
import com.wat.melody.plugin.aws.ec2.DeleteMachine;
import com.wat.melody.plugin.aws.ec2.NewMachine;
import com.wat.melody.plugin.aws.ec2.StartMachine;
import com.wat.melody.plugin.aws.ec2.StopMachine;
import com.wat.melody.plugin.aws.ec2.common.exception.AwsException;
import com.wat.melody.plugin.aws.ec2.common.exception.IllegalManagementMethodException;
import com.wat.melody.plugin.ssh.Upload;
import com.wat.melody.plugin.ssh.common.Configuration;
import com.wat.melody.plugin.ssh.common.KeyPairHelper;
import com.wat.melody.plugin.ssh.common.KeyPairRepository;
import com.wat.melody.plugin.ssh.common.exception.KeyPairRepositoryException;
import com.wat.melody.plugin.ssh.common.exception.SshException;
import com.wat.melody.xpathextensions.GetHeritedContent;

/**
 * <p>
 * Based on the underlying operating system of the Aws Instance, the AWS EC2
 * Plug-In will perform different actions to facilitates the management of the
 * Aws Instance :
 * <ul>
 * <li>If the operating system is Unix/Linux, it will add/remove the instance's
 * HostKey from the Ssh Plug-In KnownHost file on
 * newMachine/deleteMachine/startMachine/stopMachine operations ;</li>
 * <li>If the operating system is Windows, il will add/remove the instance's
 * certificate in the local WinRM Plug-In repo on
 * newMachine/deleteMachine/startMachine/stopMachine operations ;</li>
 * </ul>
 * </p>
 * <p>
 * This class provides the Task's attribute {@link #ENABLEMGNT_ATTR} which allow
 * such management enablement operations to be done and the Task's attribute
 * {@link #ENABLEMGNT_TIMEOUT_ATTR} which represent the timeout of this
 * management enablement operations.
 * </p>
 * <p>
 * In order to perform these actions, each AWS Instance Node must have :
 * <ul>
 * <li>a "tags/tag[@name='mgnt']/@value" ;</li>
 * <li>a "tags/tag[@name='ssh.port']/@value" ;</li>
 * </ul>
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class AbstractMachineOperation extends AbstractAwsOperation {

	private static Log log = LogFactory.getLog(AbstractMachineOperation.class);

	/**
	 * The 'enableManagement' XML attribute of the 'NewMachine' XML element
	 */
	public static final String ENABLEMGNT_ATTR = "enableManagement";

	/**
	 * The 'enableManagementTimeout' XML attribute of the 'NewMachine' XML
	 * element
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

	protected void newInstance(InstanceType type, String sImageId,
			String sSGName, String sSGDesc, String sAZ, String sKeyName)
			throws AwsException, InterruptedException {
		Common.createSecurityGroup(getEc2(), sSGName, sSGDesc);
		Instance i = Common.newAwsInstance(getEc2(), type, sImageId, sSGName,
				sAZ, sKeyName);
		if (i == null) {
			throw new AwsException(Messages.bind(Messages.NewEx_FAILED,
					new Object[] { getRegion(), sImageId, type, sKeyName }));
		}
		// Immediately store the instanceID to the ED
		setAwsInstanceID(i.getInstanceId());
		setInstanceRelatedInfosToED(i);
		if (!Common.waitUntilInstanceStatusBecomes(getEc2(), i.getInstanceId(),
				InstanceState.RUNNING, getTimeout(), 10000)) {
			throw new AwsException(Messages.bind(Messages.MachineEx_TIMEOUT,
					NewMachine.NEW_MACHINE, getTimeout()));
		}
	}

	protected void startInstance() throws AwsException, InterruptedException {
		if (!Common
				.startAwsInstance(getEc2(), getAwsInstanceID(), getTimeout())) {
			throw new AwsException(Messages.bind(Messages.MachineEx_TIMEOUT,
					StartMachine.START_MACHINE, getTimeout()));
		}
	}

	protected void stopInstance() throws AwsException, InterruptedException {
		if (!Common.stopAwsInstance(getEc2(), getAwsInstanceID(), getTimeout())) {
			throw new AwsException(Messages.bind(Messages.MachineEx_TIMEOUT,
					StopMachine.STOP_MACHINE, getTimeout()));
		}
	}

	protected void deleteInstance() throws AwsException, InterruptedException {
		Instance i = getInstance();
		String sgname = i.getSecurityGroups().get(0).getGroupName();

		if (!Common.deleteAwsInstance(getEc2(), getAwsInstanceID(),
				getTimeout())) {
			throw new AwsException(Messages.bind(Messages.MachineEx_TIMEOUT,
					DeleteMachine.DELETE_MACHINE, getTimeout()));
		}
		setAwsInstanceID(null);
		if (sgname == null || sgname.length() == 0) {
			return;
		}
		Common.deleteSecurityGroup(getEc2(), sgname);
	}

	protected synchronized void enableKeyPair(File keyPairRepoFile,
			String sKeyPairName, int iKeySize, String sPassphrase)
			throws AwsException, IOException {
		// Create KeyPair in the KeyPair Repository
		KeyPairRepository keyPairRepo;
		try {
			keyPairRepo = new KeyPairRepository(keyPairRepoFile);
		} catch (IllegalDirectoryException Ex) {
			throw new RuntimeException("Unexpected error while initializing "
					+ "the KeyPair Repository " + keyPairRepoFile + ". "
					+ "Because this KeyPair Repository have been previously "
					+ "validated, such error cannot happened. "
					+ "Source code has certainly been modified and a bug have "
					+ "been introduced, or an external process made this "
					+ "KeyPair Repository is no more available.", Ex);
		}

		try {
			if (keyPairRepo.containsKeyPair(sKeyPairName) == false) {
				keyPairRepo.createKeyPair(sKeyPairName, iKeySize, sPassphrase);
			}
		} catch (KeyPairRepositoryException Ex) {
			throw new AwsException(Ex);
		}

		KeyPair kp = KeyPairHelper.readOpenSslPEMPrivateKey(keyPairRepo
				.getPrivateKeyPath(sKeyPairName));
		// Create KeyPair in Aws
		if (Common.keyPairExists(getEc2(), sKeyPairName) == true) {
			String fingerprint = KeyPairHelper
					.generateOpenSslPEMFingerprint(kp);
			if (Common.keyPairCompare(getEc2(), sKeyPairName, fingerprint) == false) {
				throw new AwsException("Aws KeyPair and Local KeyPair doesn't "
						+ "match.");
			}
		} else {
			String pubkey = KeyPairHelper.generateOpenSshRSAPublicKey(kp,
					"Generated by Melody");
			Common.importKeyPair(getEc2(), sKeyPairName, pubkey);
		}
	}

	protected void enableManagement() throws AwsException, InterruptedException {
		if (getEnableManagement() == false) {
			return;
		}
		Instance i = getInstance();
		ManagementMethod mm = findManagementMethodTag();
		log.debug(Messages.bind(Messages.MachineMsg_MANAGEMENT_ENABLE_BEGIN,
				mm, getAwsInstanceID()));
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
				mm, getAwsInstanceID()));
	}

	protected void disableManagement() throws AwsException,
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
				mm, getAwsInstanceID()));
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
				mm, getAwsInstanceID()));
	}

	public static final String TAG_MGNT = "MGNT";
	public static final String TAG_SSH_PORT = "SSH.PORT";
	public static final String TAG_WINRM_PORT = "WINRM.PORT";

	private ManagementMethod findManagementMethodTag() throws AwsException {
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
			throw new AwsException(Ex);
		}
		if (nl.getLength() > 1) {
			throw new AwsException(
					Messages.bind(
							Messages.MachineEx_TOO_MANY_TAG_MGNT,
							new Object[] {
									TAG_MGNT,
									ENABLEMGNT_ATTR,
									Arrays.asList(ManagementMethod.values()),
									getED().getLocation(getTargetNode())
											.toFullString() }));
		} else if (nl.getLength() == 0) {
			throw new AwsException(
					Messages.bind(
							Messages.MachineEx_NO_TAG_MGNT,
							new Object[] {
									TAG_MGNT,
									ENABLEMGNT_ATTR,
									Arrays.asList(ManagementMethod.values()),
									getED().getLocation(getTargetNode())
											.toFullString() }));
		}
		String val = nl.item(0).getNodeValue();
		try {
			return ManagementMethod.parseString(val);
		} catch (IllegalManagementMethodException Ex) {
			throw new AwsException(Messages.bind(
					Messages.MachineEx_INVALID_TAG_MGNT, TAG_MGNT, getED()
							.getLocation(nl.item(0)).toFullString()), Ex);
		}
	}

	private Port findManagementPortTag(ManagementMethod mm) throws AwsException {
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
			throw new AwsException(Ex);
		}
		if (nl.getLength() > 1) {
			throw new AwsException(
					Messages.bind(Messages.MachineEx_TOO_MANY_TAG_MGNT_PORT,
							new Object[] {
									portTag,
									ENABLEMGNT_ATTR,
									TAG_MGNT,
									mm,
									getED().getLocation(getTargetNode())
											.toFullString() }));
		} else if (nl.getLength() == 0) {
			throw new AwsException(
					Messages.bind(Messages.MachineEx_NO_TAG_MGNT_PORT,
							new Object[] {
									portTag,
									ENABLEMGNT_ATTR,
									TAG_MGNT,
									mm,
									getED().getLocation(getTargetNode())
											.toFullString() }));
		}
		String val = nl.item(0).getNodeValue();
		try {
			return Port.parseString(val);
		} catch (IllegalPortException Ex) {
			throw new AwsException(Messages.bind(
					Messages.MachineEx_INVALID_TAG_MGNT_PORT, portTag, getED()
							.getLocation(nl.item(0)).toFullString()), Ex);
		}
	}

	private void enableWinRmManagement(Instance i) throws AwsException {
		throw new AwsException(Messages.bind(
				Messages.MachineEx_INVLIAD_TAG_MGNT_WINRN_SUPPORT,
				ManagementMethod.WINRM));
	}

	private void enableSshManagement(Instance i) throws AwsException,
			InterruptedException {
		disableSshManagement(i);

		if (!addMachineToKnownHosts(i)) {
			throw new AwsException(Messages.bind(
					Messages.MachineEx_ENABLE_SSH_MGNT_TIMEOUT,
					i.getInstanceId(), getRegion()));
		}

		String k = getSshPluginConf().getKnownHostsHostKey(
				i.getPublicIpAddress()).getKey();
		try {
			getSshPluginConf().addKnownHostsHostKey(i.getPrivateIpAddress(), k);
			getSshPluginConf().addKnownHostsHostKey(i.getPublicDnsName(), k);
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
	 * <i> * After the operation complete, retrieve the HostKey of the AWS
	 * {@link Instance} by calling {@link Configuration#getKnownHostsHostKey} ;
	 * <BR/>
	 * </i>
	 * </p>
	 * 
	 * @param i
	 *            is the AWS {@link Instance} to add to the known hosts file.
	 * 
	 * @return <tt>true</tt> if the operation complete before the timeout
	 *         elapsed, <tt>false</tt> if the operation isn't complete before
	 *         the timeout elapsed.
	 * 
	 * @throws AwsException
	 *             if
	 * @throws InterruptedException
	 *             if
	 */
	private boolean addMachineToKnownHosts(Instance i) throws AwsException,
			InterruptedException {
		String sgname = i.getSecurityGroups().get(0).getGroupName();
		Port p = findManagementPortTag(ManagementMethod.SSH);

		Host host = null;
		try {
			host = Host.parseString(i.getPublicIpAddress());
		} catch (IllegalHostException Ex) {
			throw new RuntimeException(Ex);
		}

		Upload upload = new Upload();

		try {
			upload.setContext(getContext());
		} catch (SshException Ex) {
			throw new AwsException(Ex);
		}

		upload.setTrust(true);
		upload.setHost(host);
		upload.setPort(p);

		try {
			upload.setLogin("melody");
		} catch (SshException Ex) {
			throw new RuntimeException("Unexpected error while setting the "
					+ "login of the Ssh connection to 'melody'. "
					+ "Because this login is harcoded, it must be valid. "
					+ "Source code has certainly been modified and a bug "
					+ "have been introduced.", Ex);
		}

		IpPermission toAdd = new IpPermission();
		toAdd.withFromPort(p.getValue());
		toAdd.withToPort(p.getValue());
		toAdd.withIpProtocol(Protocol.TCP.getValue());
		toAdd.withIpRanges(IpRange.ALL);

		boolean doNotRevoke = false;
		AuthorizeSecurityGroupIngressRequest authreq = null;
		authreq = new AuthorizeSecurityGroupIngressRequest();
		authreq = authreq.withGroupName(sgname).withIpPermissions(toAdd);
		try {
			getEc2().authorizeSecurityGroupIngress(authreq);
		} catch (AmazonServiceException Ex) {
			if (Ex.getErrorCode().indexOf("InvalidPermission.Duplicate") != -1) {
				doNotRevoke = true;
			}
		}

		final long WAIT_STEP = 5000;
		final long start = System.currentTimeMillis();
		long left;
		boolean enablementDone = true;

		try {
			while (true) {
				// Don't upload anything, just connect.
				try {
					Session session = upload.openSession();
					ChannelSftp channel = upload.openSftpChannel(session);
					channel.disconnect();
					session.disconnect();
					break;
				} catch (Throwable Ex) {
					if (Ex.getCause() == null
							|| Ex.getCause().getMessage() == null) {
						throw new AwsException(Ex);
					} else if (Ex.getCause().getMessage()
							.indexOf("Incorrect credentials") != -1) {
						// connection succeed
						break;
					} else if (Ex.getCause().getMessage()
							.indexOf("Connection refused") == -1
							&& Ex.getCause().getMessage().indexOf("timeout") == -1) {
						throw new AwsException(Ex);
					}
				}
				log.debug(Messages.bind(
						Messages.MachineMsg_WAIT_FOR_MANAGEMENT,
						getAwsInstanceID()));
				if (getEnableManagementTimeout() == 0) {
					Thread.sleep(WAIT_STEP);
					continue;
				}
				left = getEnableManagementTimeout()
						- (System.currentTimeMillis() - start);
				Thread.sleep(Math.min(WAIT_STEP, Math.max(0, left)));
				if (left < 0) {
					enablementDone = false;
					break;
				}
			}
			return enablementDone;
		} finally {
			if (!doNotRevoke) {
				RevokeSecurityGroupIngressRequest revreq = null;
				revreq = new RevokeSecurityGroupIngressRequest();
				revreq = revreq.withGroupName(sgname).withIpPermissions(toAdd);
				getEc2().revokeSecurityGroupIngress(revreq);
			}
		}
	}

	private void disableWinRmManagement(Instance i) throws AwsException {
		Image ami = Common.getImageId(getEc2(), i.getImageId());
		throw new AwsException(Messages.bind(
				Messages.MachineEx_INVLIAD_TAG_MGNT_WINRN_SUPPORT,
				new Object[] { ManagementMethod.WINRM, TAG_MGNT,
						ami.getImageId(), getRegion() }));
	}

	private void disableSshManagement(Instance i) {
		if (i != null) {
			getSshPluginConf().removeKnownHostsHostKey(i.getPublicIpAddress());
			getSshPluginConf().removeKnownHostsHostKey(i.getPrivateIpAddress());
			getSshPluginConf().removeKnownHostsHostKey(i.getPublicDnsName());
			getSshPluginConf().removeKnownHostsHostKey(i.getPrivateDnsName());
		}
		try {
			String v = null;
			v = getED().getAttributeValue(getMelodyID(), Common.IP_PUB_ATTR);
			getSshPluginConf().removeKnownHostsHostKey(v);
			v = getED().getAttributeValue(getMelodyID(), Common.FQDN_PUB_ATTR);
			getSshPluginConf().removeKnownHostsHostKey(v);
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
	public boolean setEnableManagement(boolean enableManagement)
			throws AwsException {
		boolean previous = getEnableManagement();
		mbEnableManagement = enableManagement;
		return previous;
	}

	public long getEnableManagementTimeout() {
		return mlEnableManagementTimeout;
	}

	@Attribute(name = ENABLEMGNT_TIMEOUT_ATTR)
	public long setEnableManagementTimeout(long timeout) throws AwsException {
		if (timeout < 0) {
			throw new AwsException(Messages.bind(
					Messages.MachineEx_INVALID_TIMEOUT_ATTR, new Object[] {
							timeout, ENABLEMGNT_TIMEOUT_ATTR,
							getClass().getSimpleName().toLowerCase() }));
		}
		long previous = getEnableManagementTimeout();
		mlEnableManagementTimeout = timeout;
		return previous;
	}

}
