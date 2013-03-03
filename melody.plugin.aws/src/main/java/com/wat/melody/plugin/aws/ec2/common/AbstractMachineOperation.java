package com.wat.melody.plugin.aws.ec2.common;

import java.io.IOException;
import java.security.KeyPair;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.ec2.model.Instance;
import com.wat.melody.api.exception.ResourcesDescriptorException;
import com.wat.melody.cloud.instance.InstanceState;
import com.wat.melody.cloud.instance.InstanceType;
import com.wat.melody.cloud.instance.exception.OperationException;
import com.wat.melody.cloud.network.ManagementNetworkMethod;
import com.wat.melody.cloud.network.NetworkManager;
import com.wat.melody.cloud.network.NetworkManagerFactory;
import com.wat.melody.common.keypair.KeyPairName;
import com.wat.melody.common.keypair.KeyPairRepository;
import com.wat.melody.plugin.aws.ec2.DeleteMachine;
import com.wat.melody.plugin.aws.ec2.NewMachine;
import com.wat.melody.plugin.aws.ec2.StartMachine;
import com.wat.melody.plugin.aws.ec2.StopMachine;
import com.wat.melody.plugin.aws.ec2.common.exception.AwsException;

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
 * {@link ManagementNetworkMethod} ;</li>
 * <li>for unix/lunix, a "tags/tag[@name='ssh.port']/@value" ;</li>
 * <li>for windows, a "tags/tag[@name='winrm.port']/@value" ;</li>
 * </ul>
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class AbstractMachineOperation extends AbstractAwsOperation {

	public AbstractMachineOperation() {
		super();
	}

	/**
	 * <p>
	 * Create a new Aws Instance based on the given values, and wait for the
	 * newly created Aws Instance to reach the {@link InstanceState#RUNNING}
	 * state.
	 * </p>
	 * 
	 * <p>
	 * <i> * Once created, set the Aws Instance ID of this object to the ID of
	 * the created Aws Instance, so you can use {@link #getAwsInstanceID} to
	 * retrieve it ; <BR/>
	 * * Once created, store the Aws Instance ID into the
	 * {@link Common#INSTANCE_ID_ATTR} XML Attribute of the Aws Instance Node ;
	 * <BR/>
	 * </i>
	 * </p>
	 * 
	 * @param type
	 *            is the Aws Instance Type of the Aws Instance to create.
	 * @param sImageId
	 *            is the Aws Ami Id the Aws Instance will be created from.
	 * @param sAZ
	 *            is the Aws Availability Zone the Aws Instance will be placed
	 *            in.
	 * @param keyPairName
	 *            is the Aws Key Pair Name to attache to the Aws Instance.
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
	protected AwsInstance newInstance(InstanceType type, String sImageId,
			String sAZ, KeyPairName keyPairName) throws AwsException,
			InterruptedException {
		Instance i = Common.newAwsInstance(getEc2(), type, sImageId, sAZ,
				keyPairName);
		if (i == null) {
			throw new AwsException(Messages.bind(Messages.NewEx_FAILED,
					new Object[] { getRegion(), sImageId, type, keyPairName,
							getTargetNodeLocation() }));
		}
		// Immediately store the instanceID to the ED
		setAwsInstanceID(i.getInstanceId());
		setInstanceRelatedInfosToED(i);
		if (!Common.waitUntilInstanceStatusBecomes(getEc2(), i.getInstanceId(),
				InstanceState.RUNNING, getTimeout(), 10000)) {
			throw new AwsException(
					Messages.bind(Messages.MachineEx_TIMEOUT,
							new Object[] { getInstanceID(),
									NewMachine.NEW_MACHINE, getTimeout(),
									TIMEOUT_ATTR, getTargetNodeLocation() }));
		}
		return getInstance();
	}

	/**
	 * <p>
	 * Start the Aws Instance defined by {@link #getInstanceID()}, and wait for
	 * the Aws Instance to reach the {@link InstanceState#RUNNING} state.
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
		if (!Common.startAwsInstance(getEc2(), getInstanceID(), getTimeout())) {
			throw new AwsException(
					Messages.bind(Messages.MachineEx_TIMEOUT,
							new Object[] { getInstanceID(),
									StartMachine.START_MACHINE, getTimeout(),
									TIMEOUT_ATTR, getTargetNodeLocation() }));
		}
	}

	/**
	 * <p>
	 * Stop the Aws Instance defined by {@link #getInstanceID()}, and wait for
	 * the Aws Instance to reach the {@link InstanceState#STOPPED} state.
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
		if (!Common.stopAwsInstance(getEc2(), getInstanceID(), getTimeout())) {
			throw new AwsException(
					Messages.bind(Messages.MachineEx_TIMEOUT,
							new Object[] { getInstanceID(),
									StopMachine.STOP_MACHINE, getTimeout(),
									TIMEOUT_ATTR, getTargetNodeLocation() }));
		}
	}

	/**
	 * <p>
	 * Delete the Aws Instance defined by {@link #getInstanceID()}, and wait for
	 * the Aws Instance to reach the {@link InstanceState#TERMINATED} state.
	 * </p>
	 * 
	 * <p>
	 * <i> * Set the Aws Instance ID of this object to <code>null</code> ; <BR/>
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
		if (!Common.deleteAwsInstance(getEc2(), getAwsInstance(), getTimeout())) {
			throw new AwsException(Messages.bind(Messages.MachineEx_TIMEOUT,
					new Object[] { getInstanceID(),
							DeleteMachine.DELETE_MACHINE, getTimeout(),
							TIMEOUT_ATTR, getTargetNodeLocation() }));
		}
		setAwsInstanceID(null);
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
	 * @param keyPairRepo
	 *            is the {@link KeyPairRepository}.
	 * @param keyPairName
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
	 *             if an I/O error occurred while reading/storing the
	 *             {@link KeyPair} in the local {@link KeyPairRepository}.
	 */
	protected synchronized void enableKeyPair(KeyPairRepository keyPairRepo,
			KeyPairName keyPairName, int iKeySize, String sPassphrase)
			throws AwsException, IOException {
		// Create KeyPair in the KeyPair Repository
		KeyPair kp = null;
		if (!keyPairRepo.containsKeyPair(keyPairName)) {
			kp = keyPairRepo.createKeyPair(keyPairName, iKeySize, sPassphrase);
		} else {
			kp = keyPairRepo.getKeyPair(keyPairName, sPassphrase);
		}

		// Create KeyPair in Aws
		if (Common.keyPairExists(getEc2(), keyPairName) == true) {
			String fingerprint = KeyPairRepository.getFingerprint(kp);
			if (Common.keyPairCompare(getEc2(), keyPairName, fingerprint) == false) {
				/*
				 * TODO : externalize error message
				 */
				throw new AwsException("Aws KeyPair and Local KeyPair doesn't "
						+ "match.");
			}
		} else {
			String pubkey = KeyPairRepository.getPublicKeyInOpenSshFormat(kp,
					"Generated by Melody");
			Common.importKeyPair(getEc2(), keyPairName, pubkey);
		}
	}

	public void enableNetworkManagement() throws AwsException,
			InterruptedException {
		NetworkManager mh = null;
		try {
			mh = NetworkManagerFactory.createNetworkManager(this,
					getTargetNode());
		} catch (ResourcesDescriptorException Ex) {
			throw new AwsException(Ex);
		}
		try {
			getInstance().enableNetworkManagement(mh);
		} catch (OperationException Ex) {
			throw new AwsException(Messages.bind(
					Messages.MachineEx_ENABLE_MANAGEMENT_ERROR,
					getTargetNodeLocation()), Ex);
		}
	}

	public void disableNetworkManagement() throws AwsException,
			InterruptedException {
		NetworkManager mh = null;
		try {
			mh = NetworkManagerFactory.createNetworkManager(this,
					getTargetNode());
		} catch (ResourcesDescriptorException Ex) {
			throw new AwsException(Ex);
		}
		try {
			getInstance().disableNetworkManagement(mh);
		} catch (OperationException Ex) {
			throw new AwsException(Messages.bind(
					Messages.MachineEx_DISABLE_MANAGEMENT_ERROR,
					getTargetNodeLocation()), Ex);
		}
	}

}