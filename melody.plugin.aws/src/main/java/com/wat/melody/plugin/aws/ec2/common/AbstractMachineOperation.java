package com.wat.melody.plugin.aws.ec2.common;

import java.io.File;
import java.io.IOException;
import java.security.KeyPair;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.ec2.model.AuthorizeSecurityGroupIngressRequest;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.IpPermission;
import com.amazonaws.services.ec2.model.RevokeSecurityGroupIngressRequest;
import com.wat.melody.api.annotation.Attribute;
import com.wat.melody.api.exception.ResourcesDescriptorException;
import com.wat.melody.cloud.instance.InstanceState;
import com.wat.melody.cloud.instance.InstanceType;
import com.wat.melody.cloud.network.NetworkManager;
import com.wat.melody.cloud.network.NetworkManagerFactory;
import com.wat.melody.cloud.network.exception.ManagementException;
import com.wat.melody.common.network.IpRange;
import com.wat.melody.common.network.Port;
import com.wat.melody.common.network.Protocol;
import com.wat.melody.common.utils.exception.IllegalDirectoryException;
import com.wat.melody.plugin.aws.ec2.DeleteMachine;
import com.wat.melody.plugin.aws.ec2.IngressMachine;
import com.wat.melody.plugin.aws.ec2.NewMachine;
import com.wat.melody.plugin.aws.ec2.StartMachine;
import com.wat.melody.plugin.aws.ec2.StopMachine;
import com.wat.melody.plugin.aws.ec2.common.exception.AwsException;
import com.wat.melody.plugin.ssh.common.KeyPairHelper;
import com.wat.melody.plugin.ssh.common.KeyPairRepository;
import com.wat.melody.plugin.ssh.common.exception.KeyPairRepositoryException;
import com.wat.melody.xpathextensions.common.NetworkManagementHelper;
import com.wat.melody.xpathextensions.common.NetworkManagementMethod;

/**
 * <p>
 * Based on the underlying operating system of the Aws Instance, the AWS EC2
 * Plug-In can perform different actions to facilitates the management of the
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
 * This class provides the Task's attribute {@link #ENABLE_NETWORK_MGNT_ATTR}
 * which enable/disable such management enablement and the Task's attribute
 * {@link #ENABLE_NETWORK_MGNT_TIMEOUT_ATTR} which represent the timeout of
 * these management enablement operations.
 * </p>
 * <p>
 * In order to perform these actions, each AWS Instance Node must have :
 * <ul>
 * <li>a "tags/tag[@name='mgnt']/@value" equal to one of
 * {@link NetworkManagementMethod} ;</li>
 * <li>for unix/lunix, a "tags/tag[@name='ssh.port']/@value" ;</li>
 * <li>for windows, a "tags/tag[@name='winrm.port']/@value" ;</li>
 * </ul>
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class AbstractMachineOperation extends AbstractAwsOperation {

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
		} catch (AwsException Ex) {
			throw new RuntimeException("Unexpected error while setting "
					+ "the management timeout to '300000'. "
					+ "Because this value is hard coded, such error "
					+ "cannot happened. "
					+ "Source code has certainly been modified and a bug have "
					+ "been introduced.", Ex);
		}
		setEnableNetworkManagement(true);
	}

	@Override
	public void validate() throws AwsException {
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
			throw new AwsException(Ex);
		}
	}

	/**
	 * <p>
	 * Create a new Aws Instance based on the given values, and wait for the
	 * newly created Aws Instance to reach the {@link InstanceState#RUNNING}
	 * state.
	 * </p>
	 * 
	 * <p>
	 * <i> * Create and associate an Aws Security Group to the Aws Instance.
	 * This Security Group is call the Melody Security Group and is used by
	 * {@link IngressMachine} to manage Network access to the created Aws
	 * Instance ; <BR/>
	 * * Once created, set the Aws Instance ID of this object to the ID of the
	 * created Aws Instance, so you can use {@link #getAwsInstanceID} to
	 * retrieve it ; <BR/>
	 * * Once created, store the Aws Instance ID into the
	 * {@link Common#AWS_INSTANCE_ID_ATTR} XML Attribute of the Aws Instance
	 * Node ; <BR/>
	 * </i>
	 * </p>
	 * 
	 * @param type
	 *            is the Aws Instance Type of the Aws Instance to create.
	 * @param sImageId
	 *            is the Aws Ami Id of the Aws Instance to create.
	 * @param sSGName
	 *            if the name of the Security Group to create and associate to
	 *            the Aws Instance to create.
	 * @param sSGDesc
	 *            is the description of the Security Group.
	 * @param sAZ
	 *            is the Aws Availability Zone of the Aws Instance to create.
	 * @param sKeyName
	 *            is the Aws KeyPair Name of the Aws Instance to create.
	 * 
	 * @throws AwsException
	 *             if the Aws Instance was not created.
	 * @throws AmazonServiceException
	 *             if the operation fails.
	 * @throws AmazonClientException
	 *             if the operation fails.
	 * @throws InterruptedException
	 *             if the wait is interrupted.
	 */
	protected void newInstance(InstanceType type, String sImageId,
			String sSGName, String sSGDesc, String sAZ, String sKeyName)
			throws AwsException, InterruptedException {
		Common.createSecurityGroup(getEc2(), sSGName, sSGDesc);
		Instance i = Common.newAwsInstance(getEc2(), type, sImageId, sSGName,
				sAZ, sKeyName);
		if (i == null) {
			throw new AwsException(Messages.bind(Messages.NewEx_FAILED,
					new Object[] { getRegion(), sImageId, type, sKeyName,
							getTargetNodeLocation() }));
		}
		// Immediately store the instanceID to the ED
		setAwsInstanceID(i.getInstanceId());
		setInstanceRelatedInfosToED(i);
		if (!Common.waitUntilInstanceStatusBecomes(getEc2(), i.getInstanceId(),
				InstanceState.RUNNING, getTimeout(), 10000)) {
			throw new AwsException(
					Messages.bind(Messages.MachineEx_TIMEOUT,
							new Object[] { getAwsInstanceID(),
									NewMachine.NEW_MACHINE, getTimeout(),
									TIMEOUT_ATTR, getTargetNodeLocation() }));
		}
	}

	/**
	 * <p>
	 * Start the Aws Instance defined by {@link #getAwsInstanceID()}, and wait
	 * for the Aws Instance to reach the {@link InstanceState#RUNNING} state.
	 * </p>
	 * 
	 * @throws AwsException
	 *             if the Aws Instance was not started within the timeout
	 *             defined by {@link #getTimeout()}.
	 * @throws AmazonServiceException
	 *             if the operation fails.
	 * @throws AmazonClientException
	 *             if the operation fails.
	 * @throws InterruptedException
	 *             if the wait is interrupted.
	 */
	protected void startInstance() throws AwsException, InterruptedException {
		if (!Common
				.startAwsInstance(getEc2(), getAwsInstanceID(), getTimeout())) {
			throw new AwsException(Messages.bind(Messages.MachineEx_TIMEOUT,
					new Object[] { getAwsInstanceID(),
							StartMachine.START_MACHINE, getTimeout(),
							TIMEOUT_ATTR, getTargetNodeLocation() }));
		}
	}

	/**
	 * <p>
	 * Stop the Aws Instance defined by {@link #getAwsInstanceID()}, and wait
	 * for the Aws Instance to reach the {@link InstanceState#STOPPED} state.
	 * </p>
	 * 
	 * @throws AwsException
	 *             if the Aws Instance was not stopped within the timeout
	 *             defined by {@link #getTimeout()}.
	 * @throws AmazonServiceException
	 *             if the operation fails.
	 * @throws AmazonClientException
	 *             if the operation fails.
	 * @throws InterruptedException
	 *             if the wait is interrupted.
	 */
	protected void stopInstance() throws AwsException, InterruptedException {
		if (!Common.stopAwsInstance(getEc2(), getAwsInstanceID(), getTimeout())) {
			throw new AwsException(Messages.bind(Messages.MachineEx_TIMEOUT,
					new Object[] { getAwsInstanceID(),
							StopMachine.STOP_MACHINE, getTimeout(),
							TIMEOUT_ATTR, getTargetNodeLocation() }));
		}
	}

	/**
	 * <p>
	 * Delete the Aws Instance defined by {@link #getAwsInstanceID()}, and wait
	 * for the Aws Instance to reach the {@link InstanceState#TERMINATED} state.
	 * </p>
	 * 
	 * <p>
	 * <i> * Delete the Melody Security Group of the Aws Instance ; <BR/>
	 * * Set the Aws Instance ID of this object to <code>null</code> ; <BR/>
	 * </i>
	 * </p>
	 * 
	 * @throws AwsException
	 *             if the Aws Instance was not deleted within the timeout
	 *             defined by {@link #getTimeout()}.
	 * @throws AmazonServiceException
	 *             if the operation fails.
	 * @throws AmazonClientException
	 *             if the operation fails.
	 * @throws InterruptedException
	 *             if the wait is interrupted.
	 */
	protected void deleteInstance() throws AwsException, InterruptedException {
		Instance i = getInstance();
		String sgname = i.getSecurityGroups().get(0).getGroupName();

		if (!Common.deleteAwsInstance(getEc2(), getAwsInstanceID(),
				getTimeout())) {
			throw new AwsException(Messages.bind(Messages.MachineEx_TIMEOUT,
					new Object[] { getAwsInstanceID(),
							DeleteMachine.DELETE_MACHINE, getTimeout(),
							TIMEOUT_ATTR, getTargetNodeLocation() }));
		}
		setAwsInstanceID(null);
		if (sgname == null || sgname.length() == 0) {
			return;
		}
		Common.deleteSecurityGroup(getEc2(), sgname);
	}

	/**
	 * <p>
	 * Enable the given KeyPair in Aws. More formally, this will :
	 * <ul>
	 * <li>Create a new {@link KeyPair} and store it in the given local
	 * {@link KeyPairRepository} in openSSH RSA format if the {@link KeyPair}
	 * can not be found the given local {@link KeyPairRepository} ;</li>
	 * <li>Import the public part of the given {@link KeyPair} in the Aws Region
	 * defined by {@link #getRegion()} if the {@link KeyPair} exists in the
	 * given local {@link KeyPairRepository} and doesn't exists in the given Aws
	 * Region ;</li>
	 * <li>Compare the public part of the given {@link KeyPair} with the public
	 * part of the Aws {@link com.amazonaws.services.ec2.model.KeyPair} if the
	 * {@link KeyPair} exists in the given local {@link KeyPairRepository} and
	 * also exists in the given Aws Region, and will throw an
	 * {@link AwsException} if they doesn't match ;</li>
	 * </ul>
	 * </p>
	 * 
	 * @param keyPairRepoFile
	 *            is the path of the local {@link KeyPairRepository}.
	 * @param sKeyPairName
	 *            is the name of the {@link KeyPair} to enable.
	 * @param iKeySize
	 *            is the size of the {@link KeyPair} to create (only apply if
	 *            the local {@link KeyPairRepository} doesn't contains the key
	 *            pair).
	 * @param sPassphrase
	 *            is the passphrase to associate to the {@link KeyPair} to
	 *            create (only apply if the local {@link KeyPairRepository}
	 *            doesn't contains the key pair).
	 * 
	 * @throws AwsException
	 *             if the {@link KeyPair} found in the local
	 *             {@link KeyPairRepository} is corrupted (ex : not a valid
	 *             OpenSSH RSA KeyPair) or if the {@link KeyPair} found in the
	 *             local {@link KeyPairRepository} is not equal to the Aws
	 *             {@link com.amazonaws.services.ec2.model.KeyPair}.
	 * @throws IOException
	 *             if an I/O error occurred while storing the {@link KeyPair} in
	 *             the local {@link KeyPairRepository}.
	 */
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
				/*
				 * TODO : externalize error message
				 */
				throw new AwsException("Aws KeyPair and Local KeyPair doesn't "
						+ "match.");
			}
		} else {
			String pubkey = KeyPairHelper.generateOpenSshRSAPublicKey(kp,
					"Generated by Melody");
			Common.importKeyPair(getEc2(), sKeyPairName, pubkey);
		}
	}

	/**
	 * <p>
	 * Based on the underlying operating system of the Aws Instance defined by
	 * {@link #getAwsInstanceID()}, will perform different actions to
	 * facilitates the management of the Aws Instance :
	 * <ul>
	 * <li>If the operating system is Unix/Linux : will add the instance's
	 * HostKey from the Ssh Plug-In KnownHost file ;</li>
	 * <li>If the operating system is Windows : will add the instance's
	 * certificate in the local WinRM Plug-In repo ;</li>
	 * </ul>
	 * </p>
	 * 
	 * @throws AwsException
	 * @throws InterruptedException
	 */
	protected void enableNetworkManagement() throws AwsException,
			InterruptedException {
		if (getEnableNetworkManagement() == false) {
			return;
		}

		NetworkManager mh = null;
		try {
			mh = NetworkManagerFactory.createNetworkManager(getContext(),
					getTargetNode());
		} catch (ResourcesDescriptorException | ManagementException Ex) {
			throw new AwsException(Ex);
		}

		log.debug(Messages.bind(Messages.MachineMsg_MANAGEMENT_ENABLE_BEGIN,
				getAwsInstanceID()));

		Port p = mh.getManagementDatas().getPort();
		String sgname = getInstance().getSecurityGroups().get(0).getGroupName();
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

		try {
			mh.enableNetworkManagement(getEnableNetworkManagementTimeout());
		} catch (ManagementException Ex) {
			throw new AwsException(Messages.bind(
					Messages.MachineEx_MANAGEMENT_ENABLE_FAILED,
					getAwsInstanceID(), getTargetNodeLocation()), Ex);
		} finally {
			if (!doNotRevoke) {
				RevokeSecurityGroupIngressRequest revreq = null;
				revreq = new RevokeSecurityGroupIngressRequest();
				revreq = revreq.withGroupName(sgname).withIpPermissions(toAdd);
				getEc2().revokeSecurityGroupIngress(revreq);
			}
		}
		log.info(Messages.bind(Messages.MachineMsg_MANAGEMENT_ENABLE_SUCCESS,
				getAwsInstanceID()));
	}

	/**
	 * <p>
	 * Based on the underlying operating system of the Aws Instance defined by
	 * {@link #getAwsInstanceID()}, will perform different actions to
	 * facilitates the management of the Aws Instance :
	 * <ul>
	 * <li>If the operating system is Unix/Linux : will remove the instance's
	 * HostKey from the Ssh Plug-In KnownHost file ;</li>
	 * <li>If the operating system is Windows : will remove the instance's
	 * certificate in the local WinRM Plug-In repo ;</li>
	 * </ul>
	 * </p>
	 * 
	 * @throws AwsException
	 * @throws InterruptedException
	 */
	protected void disableNetworkManagement() throws AwsException,
			InterruptedException {
		if (getEnableNetworkManagement() == false) {
			return;
		}

		NetworkManager mh = null;
		try {
			mh = NetworkManagerFactory.createNetworkManager(getContext(),
					getTargetNode());
		} catch (ResourcesDescriptorException | ManagementException Ex) {
			throw new AwsException(Ex);
		}

		log.debug(Messages.bind(Messages.MachineMsg_MANAGEMENT_DISABLE_BEGIN,
				getAwsInstanceID()));
		try {
			mh.disableNetworkManagement();
		} catch (ManagementException Ex) {
			throw new AwsException(Messages.bind(
					Messages.MachineEx_MANAGEMENT_DISABLE_FAILED,
					getAwsInstanceID(), getTargetNodeLocation()), Ex);
		}
		log.info(Messages.bind(Messages.MachineMsg_MANAGEMENT_DISABLE_SUCCESS,
				getAwsInstanceID()));
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
			throws AwsException {
		if (timeout < 0) {
			throw new AwsException(Messages.bind(
					Messages.MachineEx_INVALID_TIMEOUT_ATTR, timeout));
		}
		long previous = getEnableNetworkManagementTimeout();
		mlEnableNetworkManagementTimeout = timeout;
		return previous;
	}

}