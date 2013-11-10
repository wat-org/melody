package com.wat.melody.common.transfer;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wat.melody.common.files.EnhancedFileAttributes;
import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.transfer.resources.ResourceSpecification;

/**
 * <p>
 * When transferring a file (e.g. a regular file, a directory or a link), when
 * the parent base directory doesn't exists and is not explicitly define in the
 * 'full transfer operation' directives, all levels of the parent directory are
 * automatically added by the 'full transfer operation' provider. Because these
 * directories are not explicitly defined, we have no indication concerning the
 * attributes (ex : permissions, ower, group, etc) to set on. In this situation,
 * the 'full transfer operation' provider will automatically add a
 * {@link TransferableFake} for each levels of the parent directory which is not
 * defined in the 'full transfer operation' directives.
 * </p>
 * <p>
 * The transfer operation of a {@link TransferableFake} simply consist in the
 * creation of the directory if it doesn't exists.
 * </p>
 * 
 * @author Guillaume Cornet
 * 
 */
public class TransferableFake implements Transferable {

	/*
	 * This class have no attribute (ex: permission, user owner, group owner,
	 * etc), meaning the default attribute will be set during the transfer
	 * operation.
	 */
	private static Logger log = LoggerFactory.getLogger(TransferableFake.class);

	private Path _destinationPath;

	public TransferableFake(Path destinationPath) {
		setDestinationPath(destinationPath);
	}

	@Override
	public void transfer(TransferableFileSystem fs) throws IOException,
			InterruptedIOException, NoSuchFileException,
			FileAlreadyExistsException, AccessDeniedException {
		log.debug(Msg.bind(Messages.TransferMsg_BEGIN, this));
		// don't set attributes
		TransferHelper.createDirectory(fs, getDestinationPath());
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

	public Path setDestinationPath(Path destinationPath) {
		if (destinationPath == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + Path.class.getCanonicalName() + ".");
		}
		Path previous = getDestinationPath();
		_destinationPath = destinationPath;
		return previous;
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
	public ResourceSpecification setResourceSpecification(
			ResourceSpecification resourceSpecification) {
		return null;
	}

}