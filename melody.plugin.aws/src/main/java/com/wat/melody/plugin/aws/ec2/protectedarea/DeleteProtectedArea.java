package com.wat.melody.plugin.aws.ec2.protectedarea;

import com.wat.melody.api.Melody;
import com.wat.melody.api.annotation.Task;
import com.wat.melody.api.exception.TaskException;
import com.wat.melody.cloud.protectedarea.exception.ProtectedAreaException;
import com.wat.melody.common.xml.exception.NodeRelatedException;
import com.wat.melody.plugin.aws.ec2.common.Messages;
import com.wat.melody.plugin.aws.ec2.common.exception.AwsPlugInEc2Exception;

/**
 * <P>
 * Caller should call {@link ResetProtectedArea} prior, in order to avoid
 * protected area dependencies conflicts which can occurred during deletion of a
 * protected area.
 * 
 * @author Guillaume Cornet
 * 
 */
@Task(name = DeleteProtectedArea.DELETE_PROTECTED_AREA)
public class DeleteProtectedArea extends AbstractProtectedAreaOperation {

	/**
	 * Task's name
	 */
	public static final String DELETE_PROTECTED_AREA = "delete-protected-area";

	public DeleteProtectedArea() {
		super();
	}

	@Override
	public void doProcessing() throws TaskException, InterruptedException {
		Melody.getContext().handleProcessorStateUpdates();

		try {
			getProtectedAreaController().ensureProtectedAreaIsDestroyed();
		} catch (ProtectedAreaException Ex) {
			throw new AwsPlugInEc2Exception(new NodeRelatedException(
					getTargetElement(), Messages.PADestroyEx_GENERIC_FAIL, Ex));
		}
	}

}