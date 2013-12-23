package com.wat.melody.plugin.aws.s3.common;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.amazonaws.services.s3.AmazonS3;
import com.wat.cloud.aws.s3.BucketName;
import com.wat.melody.api.ITaskContext;
import com.wat.melody.api.Melody;
import com.wat.melody.api.annotation.Attribute;
import com.wat.melody.api.annotation.NestedElement;
import com.wat.melody.api.annotation.NestedElement.Type;
import com.wat.melody.common.files.exception.IllegalFileException;
import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.transfer.TemplatingHandler;
import com.wat.melody.common.transfer.exception.TemplatingException;
import com.wat.melody.common.transfer.exception.TransferException;
import com.wat.melody.common.transfer.resources.ResourcesSpecification;
import com.wat.melody.common.xpath.exception.ExpressionSyntaxException;
import com.wat.melody.plugin.aws.s3.common.exception.AwsPlugInS3Exception;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class Transfer extends AbstractTransferOperation implements
		TemplatingHandler {

	/**
	 * Task's attribute, which specifies the maximum number of concurrent
	 * download.
	 */
	public static final String MAXPAR_ATTR = "max-par";

	/**
	 * Task's nested element, which specifies the resources to download.
	 */
	public static final String RESOURCES_NE = "resources";

	private List<ResourcesSpecification> _resourcesSpecifications = new ArrayList<ResourcesSpecification>();
	private int _maxPar = 10;

	private ITaskContext _taskContext;

	public Transfer() {
		super();
	}

	@Override
	public void validate() throws AwsPlugInS3Exception {
		super.validate();

		// keep the current context, necessary for templating operations
		setContext(Melody.getContext());
	}

	@Override
	public void doProcessing() throws AwsPlugInS3Exception,
			InterruptedException {
		try {
			doTransfer(getS3Connection(), getBucketName());
		} catch (TransferException Ex) {
			throw new AwsPlugInS3Exception(Ex);
		}
	}

	public abstract void doTransfer(AmazonS3 s3Connection, BucketName bucketName)
			throws TransferException, InterruptedException;

	@Override
	public Path doTemplate(Path template, Path destination)
			throws TemplatingException {
		try {
			return getContext().expand(template, destination);
		} catch (IllegalFileException Ex) {
			throw new TemplatingException(Ex);
		} catch (IOException Ex) {
			throw new TemplatingException(Ex);
		} catch (ExpressionSyntaxException Ex) {
			throw new TemplatingException(Ex);
		}
	}

	public List<ResourcesSpecification> getResourcesSpecifications() {
		return _resourcesSpecifications;
	}

	@NestedElement(name = RESOURCES_NE, mandatory = true, type = Type.CREATE)
	public ResourcesSpecification createResourcesSpecification() {
		File basedir = Melody.getContext().getProcessorManager()
				.getSequenceDescriptor().getBaseDir();
		ResourcesSpecification rss = newResourcesSpecification(basedir);
		getResourcesSpecifications().add(rss);
		return rss;
	}

	public abstract ResourcesSpecification newResourcesSpecification(
			File basedir);

	public int getMaxPar() {
		return _maxPar;
	}

	@Attribute(name = MAXPAR_ATTR)
	public int setMaxPar(int maxPar) throws AwsPlugInS3Exception {
		if (maxPar < 1 || maxPar > 10) {
			throw new AwsPlugInS3Exception(Msg.bind(
					Messages.TransferEx_INVALID_MAXPAR_ATTR, maxPar));
		}
		int previous = getMaxPar();
		_maxPar = maxPar;
		return previous;
	}

	private ITaskContext getContext() {
		return _taskContext;
	}

	private ITaskContext setContext(ITaskContext tc) {
		ITaskContext previous = getContext();
		_taskContext = tc;
		return previous;
	}

}