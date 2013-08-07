package com.wat.melody.common.transfer;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wat.melody.common.files.EnhancedFileAttributes;
import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.transfer.resources.ResourceSpecification;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class TransferableFake implements Transferable {

	private static Logger log = LoggerFactory.getLogger(TransferableFake.class);

	private Path _destinationPath;
	/*
	 * TODO : maybye there's no more need for this ...
	 */
	private ResourceSpecification _resourceSpecification;

	public TransferableFake(Path destinationPath,
			ResourceSpecification resourceSpecification) {
		_destinationPath = destinationPath;
		_resourceSpecification = resourceSpecification;
	}

	@Override
	public void transfer(TransferableFileSystem fs) throws IOException,
			AccessDeniedException {
		log.debug(Msg.bind(Messages.TransferMsg_BEGIN, this));
		if (fs.isDirectory(getDestinationPath())) {
			// don't create the dir if a dir or a link on a dir already exists
			log.info(Messages.TransferMsg_DONT_TRANSFER_CAUSE_DIR_ALREADY_EXISTS);
		} else {
			// don't set attributes
			fs.createDirectory(getDestinationPath());
		}
		log.info(Msg.bind(Messages.TransferMsg_END, this));
	}

	@Override
	public Path getSourcePath() {
		return null;
	}

	@Override
	public Path getDestinationPath() {
		return _destinationPath;
	}

	@Override
	public LinkOption getLinkOption() {
		return LinkOption.KEEP_LINKS;
	}

	@Override
	public TransferBehavior getTransferBehavior() {
		return TransferBehavior.FORCE_OVERWRITE;
	}

	@Override
	public boolean getTemplate() {
		return false;
	}

	@Override
	public FileAttribute<?>[] getExpectedAttributes() {
		return null;
	}

	@Override
	public boolean exists() {
		return true;
	}

	@Override
	public boolean isRegularFile() {
		return false;
	}

	@Override
	public boolean isDirectory() {
		return true;
	}

	@Override
	public boolean isSymbolicLink() {
		return false;
	}

	@Override
	public boolean isSafeLink() {
		return false;
	}

	@Override
	public Path getSymbolicLinkTarget() {
		return null;
	}

	@Override
	public boolean linkShouldBeConvertedToFile() {
		return true;
	}

	@Override
	public EnhancedFileAttributes getAttributes() {
		return null;
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder("{ ");
		str.append("dir:");
		str.append(getDestinationPath());
		str.append(" }");
		return str.toString();
	}

	@Override
	public ResourceSpecification getResourceSpecification() {
		return _resourceSpecification;
	}

	@Override
	public ResourceSpecification setResourceSpecification(
			ResourceSpecification resourceSpecification) {
		return _resourceSpecification;
	}

}