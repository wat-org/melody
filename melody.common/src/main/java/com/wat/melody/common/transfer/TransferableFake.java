package com.wat.melody.common.transfer;

import java.nio.file.Path;

import com.wat.melody.common.files.EnhancedFileAttributes;
import com.wat.melody.common.ssh.types.GroupID;
import com.wat.melody.common.ssh.types.Modifiers;
import com.wat.melody.common.transfer.resources.ResourceSpecification;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class TransferableFake implements Transferable {

	private Path _destinationPath;
	private ResourceSpecification _resourceSpecification;

	public TransferableFake(Path destinationPath,
			ResourceSpecification resourceSpecification) {
		_destinationPath = destinationPath;
		_resourceSpecification = resourceSpecification;
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
	public EnhancedFileAttributes getAttributes() {
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
	public boolean getTemplate() {
		return false;
	}

	@Override
	public Modifiers getModifiers() {
		return null;
	}

	@Override
	public GroupID getGroup() {
		return null;
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
	public boolean linkShouldBeConvertedToFile() {
		return true;
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