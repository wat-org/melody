package com.wat.melody.plugin.aws.ec2;

import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.NodeList;

import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Volume;
import com.wat.melody.api.annotation.Attribute;
import com.wat.melody.plugin.aws.ec2.common.AbstractAwsOperation;
import com.wat.melody.plugin.aws.ec2.common.Common;
import com.wat.melody.plugin.aws.ec2.common.Disk;
import com.wat.melody.plugin.aws.ec2.common.DiskList;
import com.wat.melody.plugin.aws.ec2.common.DisksLoader;
import com.wat.melody.plugin.aws.ec2.common.Messages;
import com.wat.melody.plugin.aws.ec2.common.exception.AwsException;
import com.wat.melody.plugin.aws.ec2.common.exception.WaitVolumeAttachmentStatusException;
import com.wat.melody.plugin.aws.ec2.common.exception.WaitVolumeStatusException;
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
	 * The 'DisksXprSuffix' XML attribute
	 */
	public static final String DISKS_XPR_SUFFIX_ATTR = "DisksXprSuffix";

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

	private String msDisksXprSuffix;
	private DiskList maDiskList;
	private long mlDetachTimeout;
	private long mlCreateTimeout;
	private long mlAttachTimeout;

	public UpdateDisks() {
		super();
		initDisksXprSuffix();
		initDiskList();
		initDetachTimeout();
		initCreateTimeout();
		initAttachTimeout();
	}

	private void initDisksXprSuffix() {
		msDisksXprSuffix = "//" + Common.DISK_NE;
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

		// Build a FwRule's Collection with FwRule Nodes found
		try {
			NodeList nl = GetHeritedContent.getHeritedContent(getTargetNode(),
					getDisksXprSuffix());
			DisksLoader dl = new DisksLoader(getContext());
			setDiskList(dl.load(nl));
		} catch (XPathExpressionException Ex) {
			throw new AwsException(Messages.bind(
					Messages.UpdateDiskEx_INVALID_DISK_XPATH,
					getDisksXprSuffix()), Ex);
		} catch (ResourcesDescriptorException Ex) {
			throw new AwsException(Ex);
		}
	}

	@Override
	public void doProcessing() throws AwsException, InterruptedException {
		getContext().handleProcessorStateUpdates();

		Instance i = getInstance();
		if (i == null) {
			log.warn(Messages.bind(
					Messages.UpdateDiskMsg_NO_INSTANCE,
					new Object[] { NewMachine.NEW_MACHINE,
							NewMachine.class.getPackage(),
							getTargetNodeLocation() }));
			removeInstanceRelatedInfosToED(true);
			return;
		} else {
			setInstanceRelatedInfosToED(i);
		}

		List<Volume> aVol = Common.getInstanceVolumes(getEc2(), i);
		DiskList diskToAddList = computeAddRemoveDisks(i, aVol);
		detachAndDeleteVolumes(aVol);
		createAndAttachVolumes(i, diskToAddList);
		updateDeleteOnTerminationFlag(getDiskList());
	}

	/**
	 * <p>
	 * Let only the Disk to remove into the given list.
	 * </p>
	 * 
	 * @param i
	 * @param aVol
	 * 
	 * @return the Disks to add
	 */
	private DiskList computeAddRemoveDisks(Instance i, List<Volume> aVol)
			throws AwsException {
		// Validate and remove the root device
		computeRootDevice(i, aVol);
		// Deduce which disk to add/remove
		DiskList diskToAddList = new DiskList();
		for (Disk d : getDiskList()) {
			if (!d.getRootDevice() && !containsDisk(aVol, d)) {
				diskToAddList.add(d);
			}
		}
		log.info(Messages.bind(Messages.UpdateDiskMsg_DISKS_RESUME,
				new Object[] { getAwsInstanceID(), getDiskList(),
						diskToAddList, aVol, getTargetNodeLocation() }));
		return diskToAddList;
	}

	private void computeRootDevice(Instance i, List<Volume> aVol)
			throws AwsException {
		Disk rootDevice = getDiskList().getRootDevice();
		if (rootDevice == null) {
			throw new AwsException(Messages.bind(
					Messages.UpdateDiskEx_UNDEF_ROOT_DEVICE, new Object[] {
							DisksLoader.ROOTDEVICE_ATTR, i.getRootDeviceName(),
							getTargetNodeLocation() }));
		}
		if (!i.getRootDeviceName().equals(rootDevice.getDevice())) {
			throw new AwsException(Messages.bind(
					Messages.UpdateDiskEx_INCORRECT_ROOT_DEVICE, new Object[] {
							DisksLoader.ROOTDEVICE_ATTR,
							rootDevice.getDevice(), i.getRootDeviceName(),
							getTargetNodeLocation() }));
		}
		// Remove the root device from the list
		// so that it will not be detached and delete
		for (Volume v : aVol) {
			if (v.getAttachments().get(0).getDevice()
					.equals(i.getRootDeviceName())) {
				aVol.remove(v);
				break;
			}
		}
	}

	/**
	 * <p>
	 * Return <code>true</code> if the given disk can be found in the given
	 * volume list.
	 * </p>
	 * <p>
	 * <i> Also note that, when found, the disk is removed from the volume
	 * list.</i>
	 * </p>
	 * 
	 * @param aVol
	 * @param disk
	 * 
	 * @return <code>true</code> if the given disk can be found in the given
	 *         volume list, <code>false</code> otherwise.
	 * 
	 * @throws IllegalArgumentException
	 *             if volumeList is <code>null</code>.
	 * @throws IllegalArgumentException
	 *             if disk is <code>null</code>.
	 */
	private boolean containsDisk(List<Volume> aVol, Disk disk) {
		if (aVol == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid List<Volume>.");
		}
		if (disk == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid Disk.");
		}
		for (Volume v : aVol) {
			if (disk.equals(v.getSize(), v.getAttachments().get(0).getDevice())) {
				aVol.remove(v);
				return true;
			}
		}
		return false;
	}

	private void detachAndDeleteVolumes(List<Volume> aVol) throws AwsException,
			InterruptedException {
		try {
			Common.detachAndDeleteVolumes(getEc2(), aVol, getDetachTimeout());
		} catch (WaitVolumeStatusException Ex) {
			throw new AwsException(Messages.bind(
					Messages.UpdateDiskEx_DETACH,
					new Object[] { Ex.getVolumeId(), Ex.getDisk(),
							Ex.getTimeout() }), Ex);
		}
	}

	private void createAndAttachVolumes(Instance i, DiskList diskList)
			throws AwsException, InterruptedException {
		String sAZ = i.getPlacement().getAvailabilityZone();
		try {
			Common.createAndAttachVolumes(getEc2(), getAwsInstanceID(), sAZ,
					diskList, getCreateTimeout(), getAttachTimeout());
		} catch (WaitVolumeStatusException Ex) {
			throw new AwsException(Messages.bind(
					Messages.UpdateDiskEx_CREATE,
					new Object[] { Ex.getVolumeId(), Ex.getDisk(),
							Ex.getTimeout() }), Ex);
		} catch (WaitVolumeAttachmentStatusException Ex) {
			throw new AwsException(Messages.bind(
					Messages.UpdateDiskEx_ATTACH,
					new Object[] { Ex.getVolumeId(), Ex.getDisk(),
							Ex.getTimeout() }), Ex);
		}
	}

	private void updateDeleteOnTerminationFlag(DiskList diskList) {
		Common.updateDeleteOnTerminationFlag(getEc2(), getAwsInstanceID(),
				diskList);
	}

	private String getDisksXprSuffix() {
		return msDisksXprSuffix;
	}

	@Attribute(name = DISKS_XPR_SUFFIX_ATTR)
	public String setDisksXprSuffix(String v) {
		if (v == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Cannot be null.");
		}
		String previous = getDisksXprSuffix();
		msDisksXprSuffix = v;
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
					Messages.MachineEx_INVALID_TIMEOUT_ATTR, new Object[] {
							timeout, DETACH_TIMEOUT_ATTR,
							getClass().getSimpleName().toLowerCase() }));
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
					Messages.MachineEx_INVALID_TIMEOUT_ATTR, new Object[] {
							timeout, CREATE_TIMEOUT_ATTR,
							getClass().getSimpleName().toLowerCase() }));
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
					Messages.MachineEx_INVALID_TIMEOUT_ATTR, new Object[] {
							timeout, ATTACH_TIMEOUT_ATTR,
							getClass().getSimpleName().toLowerCase() }));
		}
		long previous = getAttachTimeout();
		mlAttachTimeout = timeout;
		return previous;
	}
}
