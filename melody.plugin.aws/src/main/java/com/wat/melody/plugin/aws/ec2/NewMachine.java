package com.wat.melody.plugin.aws.ec2;

import java.io.IOException;

import com.wat.cloud.aws.ec2.AwsInstanceController;
import com.wat.cloud.aws.ec2.AwsKeyPairRepository;
import com.wat.cloud.aws.ec2.exception.AwsKeyPairRepositoryException;
import com.wat.melody.api.Melody;
import com.wat.melody.api.annotation.Task;
import com.wat.melody.cloud.instance.InstanceController;
import com.wat.melody.cloud.instance.InstanceType;
import com.wat.melody.cloud.instance.exception.OperationException;
import com.wat.melody.cloud.instance.xml.InstanceDatasLoader;
import com.wat.melody.common.keypair.KeyPairName;
import com.wat.melody.common.keypair.exception.IllegalPassphraseException;
import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.xml.exception.NodeRelatedException;
import com.wat.melody.plugin.aws.ec2.common.AbstractOperation;
import com.wat.melody.plugin.aws.ec2.common.Messages;
import com.wat.melody.plugin.aws.ec2.common.exception.AwsException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
@Task(name = NewMachine.NEW_MACHINE)
public class NewMachine extends AbstractOperation {

	/**
	 * Task's name
	 */
	public static final String NEW_MACHINE = "new-machine";

	public NewMachine() {
		super();
	}

	@Override
	public void doProcessing() throws AwsException, InterruptedException {
		Melody.getContext().handleProcessorStateUpdates();

		try {
			getInstance().ensureInstanceIsCreated(
					getInstanceDatas().getInstanceType(),
					getInstanceDatas().getSite(),
					getInstanceDatas().getImageId(),
					getInstanceDatas().getKeyPairName(),
					getInstanceDatas().getCreateTimeout().getTimeoutInMillis());
		} catch (OperationException Ex) {
			throw new AwsException(
					new NodeRelatedException(getTargetElement(),
							Msg.bind(Messages.CreateEx_GENERIC_FAIL,
									getInstanceDatas()), Ex));
		}
	}

	/**
	 * @return an {@link AwsInstanceController} which provides additional
	 *         KeyPair Management features.
	 */
	@Override
	public InstanceController newAwsInstanceController() {
		// create AwsInstanceControllerWithKeyPairManagement class ?
		return new AwsInstanceController(getCloudConnection(), getInstanceId()) {

			public String createInstance(InstanceType type, String site,
					String imageId, KeyPairName keyPairName, long createTimeout)
					throws OperationException, InterruptedException {
				try {
					AwsKeyPairRepository kpr = AwsKeyPairRepository
							.getAwsKeyPairRepository(getConnection(),
									getInstanceDatas()
											.getKeyPairRepositoryPath());
					kpr.createKeyPair(keyPairName, getInstanceDatas()
							.getKeyPairSize(), getInstanceDatas()
							.getPassphrase());
				} catch (IllegalPassphraseException Ex) {
					if (getInstanceDatas().getPassphrase() == null) {
						throw new OperationException(Msg.bind(
								Messages.CreateEx_MISSING_PASSPHRASE_ATTR,
								InstanceDatasLoader.PASSPHRASE_ATTR,
								keyPairName));
					} else {
						throw new OperationException(Msg.bind(
								Messages.CreateEx_INVALID_PASSPHRASE_ATTR,
								InstanceDatasLoader.PASSPHRASE_ATTR,
								keyPairName));
					}
				} catch (IOException | AwsKeyPairRepositoryException Ex) {
					throw new OperationException(Ex);
				}

				return super.createInstance(type, site, imageId, keyPairName,
						createTimeout);
			}

		};
	}

}