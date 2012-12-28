package com.wat.melody.plugin.aws.ec2;

import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.NodeList;

import com.amazonaws.services.ec2.model.Instance;
import com.wat.melody.api.annotation.Attribute;
import com.wat.melody.cloud.disk.DiskList;
import com.wat.melody.cloud.disk.DiskManagementHelper;
import com.wat.melody.cloud.disk.DisksLoader;
import com.wat.melody.cloud.disk.exception.DiskException;
import com.wat.melody.common.utils.Tools;
import com.wat.melody.plugin.aws.ec2.common.AbstractAwsOperation;
import com.wat.melody.plugin.aws.ec2.common.Messages;
import com.wat.melody.plugin.aws.ec2.common.exception.AwsException;
import com.wat.melody.xpathextensions.GetHeritedContent;
import com.wat.melody.xpathextensions.common.exception.ResourcesDescriptorException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class UpdateDisks extends AbstractAwsOperation {

	private static Log log = LogFactory.getLog(UpdateDisks.class);

	/**
	 * The 'UpdateDisks' XML element
	 */
	public static final String UPDATE_DISKS = "UpdateDisks";

	/**
	 * The 'diskNodeSelector' XML attribute
	 */
	public static final String DISKS_NODE_SELECTOR_ATTR = DiskManagementHelper.DISKS_NODE_SELECTOR_ATTR;

	/**
	 * The 'detachTimeout' XML attribute
	 */
	public static final String DETACH_TIMEOUT_ATTR = "detachTimeout";

	/**
	 * The 'createTimeout' XML attribute
	 */
	public static final String CREATE_TIMEOUT_ATTR = "createTimeout";

	/**
	 * The 'attachTimeout' XML attribute
	 */
	public static final String ATTACH_TIMEOUT_ATTR = "attachTimeout";

	private String msDisksNodeSelector;
	private DiskList maDiskList;
	private long mlDetachTimeout;
	private long mlCreateTimeout;
	private long mlAttachTimeout;

	public UpdateDisks() {
		super();
		setDisksNodeSelector(DiskManagementHelper.DEFAULT_DISKS_NODE_SELECTOR);
		initDiskList();
		initDetachTimeout();
		initCreateTimeout();
		initAttachTimeout();
	}

	private void initDiskList() {
		maDiskList = null;
	}

	private void initDetachTimeout() {
		mlDetachTimeout = getTimeout();
	}

	private void initCreateTimeout() {
		mlCreateTimeout = getTimeout();
	}

	private void initAttachTimeout() {
		mlAttachTimeout = getTimeout();
	}

	@Override
	public void validate() throws AwsException {
		super.validate();

		// Disk Nodes Selector found in the RD override Disk Nodes Selector
		// defined in the SD
		try {
			String sTargetSpecificDisksSelector = DiskManagementHelper
					.findDiskManagementDisksSelector(getTargetNode());
			if (sTargetSpecificDisksSelector == null) {
				setDisksNodeSelector(sTargetSpecificDisksSelector);
			}
		} catch (ResourcesDescriptorException Ex) {
			throw new AwsException(Ex);
		}

		// Build a FwRule's Collection with FwRule Nodes found
		try {
			NodeList nl = GetHeritedContent.getHeritedContent(getTargetNode(),
					getDisksNodeSelector());
			DisksLoader dl = new DisksLoader(getContext());
			setDiskList(dl.load(nl));
		} catch (XPathExpressionException Ex) {
			throw new AwsException(Messages.bind(
					Messages.UpdateDiskEx_INVALID_DISK_XPATH,
					getDisksNodeSelector()), Ex);
		} catch (ResourcesDescriptorException Ex) {
			throw new AwsException(Ex);
		}
	}

	@Override
	public void doProcessing() throws AwsException, InterruptedException {
		getContext().handleProcessorStateUpdates();

		Instance i = getInstance();
		if (i == null) {
			AwsException Ex = new AwsException(Messages.bind(
					Messages.UpdateDiskMsg_NO_INSTANCE,
					new Object[] { NewMachine.NEW_MACHINE,
							NewMachine.class.getPackage(),
							getTargetNodeLocation() }));
			// TODO : externalize error message
			log.warn(Tools.getUserFriendlyStackTrace(new AwsException(
					"Cannot update instance's disks.", Ex)));
			removeInstanceRelatedInfosToED(true);
			return;
		} else {
			setInstanceRelatedInfosToED(i);
		}

		DiskList iDisks = getInstanceDisks(i);
		try {
			DiskManagementHelper.ensureDiskUpdateIsPossible(iDisks,
					getDiskList());
		} catch (DiskException Ex) {
			throw new AwsException("[" + getTargetNodeLocation()
					+ "] Disk update is not possible till following "
					+ "errors are not corrected. ", Ex);
		}

		DiskList disksToAdd = null;
		DiskList disksToRemove = null;
		disksToAdd = DiskManagementHelper.computeDiskToAdd(iDisks,
				getDiskList());
		disksToRemove = DiskManagementHelper.computeDiskToRemove(iDisks,
				getDiskList());

		log.info(Messages.bind(Messages.UpdateDiskMsg_DISKS_RESUME,
				new Object[] { getAwsInstanceID(), getDiskList(), disksToAdd,
						disksToRemove, getTargetNodeLocation() }));

		detachAndDeleteVolumes(i, disksToRemove, getDetachTimeout());
		createAndAttachVolumes(i, disksToAdd, getCreateTimeout(),
				getAttachTimeout());

		updateDeleteOnTerminationFlag(getDiskList());
	}

	private String getDisksNodeSelector() {
		return msDisksNodeSelector;
	}

	@Attribute(name = DISKS_NODE_SELECTOR_ATTR)
	public String setDisksNodeSelector(String v) {
		if (v == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Cannot be null.");
		}
		String previous = getDisksNodeSelector();
		msDisksNodeSelector = v;
		return previous;
	}

	private DiskList getDiskList() {
		return maDiskList;
	}

	private DiskList setDiskList(DiskList fwrs) {
		if (fwrs == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid DiskList.");
		}
		DiskList previous = getDiskList();
		maDiskList = fwrs;
		return previous;
	}

	public long getDetachTimeout() {
		return mlDetachTimeout;
	}

	@Attribute(name = DETACH_TIMEOUT_ATTR)
	public long setDetachTimeout(long timeout) throws AwsException {
		if (timeout < 0) {
			throw new AwsException(Messages.bind(
					Messages.MachineEx_INVALID_TIMEOUT_ATTR, timeout));
		}
		long previous = getDetachTimeout();
		mlDetachTimeout = timeout;
		return previous;
	}

	public long getCreateTimeout() {
		return mlCreateTimeout;
	}

	@Attribute(name = CREATE_TIMEOUT_ATTR)
	public long setCreateTimeout(long timeout) throws AwsException {
		if (timeout < 0) {
			throw new AwsException(Messages.bind(
					Messages.MachineEx_INVALID_TIMEOUT_ATTR, timeout));
		}
		long previous = getCreateTimeout();
		mlCreateTimeout = timeout;
		return previous;
	}

	public long getAttachTimeout() {
		return mlAttachTimeout;
	}

	@Attribute(name = ATTACH_TIMEOUT_ATTR)
	public long setAttachTimeout(long timeout) throws AwsException {
		if (timeout < 0) {
			throw new AwsException(Messages.bind(
					Messages.MachineEx_INVALID_TIMEOUT_ATTR, timeout));
		}
		long previous = getAttachTimeout();
		mlAttachTimeout = timeout;
		return previous;
	}
}