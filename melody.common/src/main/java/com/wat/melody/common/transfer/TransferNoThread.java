package com.wat.melody.common.transfer;

import static com.wat.melody.common.transfer.LinkOption.SKIP_LINKS;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wat.melody.common.ex.MelodyException;
import com.wat.melody.common.files.FileSystem;
import com.wat.melody.common.files.exception.IllegalFileAttributeException;
import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.transfer.exception.TemplatingException;
import com.wat.melody.common.transfer.exception.TransferException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class TransferNoThread {

	private static Logger log = LoggerFactory.getLogger(TransferNoThread.class);

	private Transferable _transferable;
	private TemplatingHandler _templatingHandler;
	private FileSystem _sourceFileSystem;
	private FileSystem _destinationFileSystem;

	public TransferNoThread(FileSystem srcFS, FileSystem destFS,
			Transferable t, TemplatingHandler th) {
		setSourceFileSystem(srcFS);
		setDestinationFileSystem(destFS);
		setTransferable(t);
		setTemplatingHandler(th);
	}

	public void doTransfer() throws TransferException {
		Transferable t = getTransferable();
		log.debug(Msg.bind(Messages.TransferMsg_BEGIN, t));
		try {
			if (t.isSymbolicLink()) {
				ln();
			} else {
				template();
			}
		} catch (IOException Ex) {
			throw new TransferException(Ex);
		}
		log.info(Msg.bind(Messages.TransferMsg_END, t));
	}

	protected void ln() throws IOException, TransferException {
		Transferable t = getTransferable();
		if (t.getLinkOption() == SKIP_LINKS) {
			ln_skip();
		} else if (t.linkShouldBeConvertedToFile()) {
			ln_copy();
		} else {
			createSymlink();
		}
	}

	protected void createSymlink() throws IOException {
		Transferable t = getTransferable();
		TransferHelper.createSymbolicLink(getDestinationFileSystem(),
				t.getDestinationPath(), t.getSymbolicLinkTarget(),
				t.getExpectedAttributes());
	}

	protected void ln_copy() throws IOException, TransferException {
		Transferable t = getTransferable();
		if (!t.exists()) {
			deleteDestination();
			log.info(Messages.TransferMsg_LINK_COPY_UNSAFE_IMPOSSIBLE);
			return;
		}
		template();
	}

	protected void ln_skip() {
		log.info(Messages.TransferMsg_LINK_SKIPPED);
	}

	protected void deleteDestination() throws IOException {
		Path path = getTransferable().getDestinationPath();
		FileSystem destFS = getDestinationFileSystem();
		if (destFS.exists(path, LinkOption.NOFOLLOW_LINKS)) {
			if (destFS.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
				destFS.deleteDirectory(path);
			} else {
				destFS.deleteIfExists(path);
			}
		}
	}

	protected void template() throws IOException, TransferException {
		Transferable t = getTransferable();
		if (t.getTemplate() == true) {
			if (getTemplatingHandler() == null) {
				throw new TransferException(
						Messages.TransferEx_NO_TEMPLATING_HANDLER);
			}
			Path template;
			try {
				template = getTemplatingHandler().doTemplate(t.getSourcePath());
			} catch (TemplatingException Ex) {
				throw new TransferException(Ex);
			}
			transfer(template);
		} else {
			transfer(t.getSourcePath());
		}
	}

	public void transfer(Path source) throws IOException {
		Transferable t = getTransferable();
		Path dest = t.getDestinationPath();
		FileAttribute<?>[] attrs = t.getExpectedAttributes();
		FileSystem destFS = getDestinationFileSystem();
		if (TransferHelper.ensureDestinationIsRegularFile(destFS,
				t.getAttributes(), dest, t.getTransferBehavior())) {
			log.info(Messages.TransferMsg_DONT_TRANSFER_CAUSE_FILE_ALREADY_EXISTS);
			try {
				destFS.setAttributes(dest, attrs);
			} catch (IllegalFileAttributeException Ex) {
				log.warn(new MelodyException(Messages.TransferMsg_SKIP_ATTR, Ex)
						.toString());
			}
		} else {
			try {
				transferFile(source, dest, attrs);
			} catch (IllegalFileAttributeException Ex) {
				log.warn(new MelodyException(Messages.TransferMsg_SKIP_ATTR, Ex)
						.toString());
			}
		}
	}

	public abstract void transferFile(Path source, Path dest,
			FileAttribute<?>... attrs) throws IOException, NoSuchFileException,
			AccessDeniedException, IllegalFileAttributeException;;

	protected FileSystem getSourceFileSystem() {
		return _sourceFileSystem;
	}

	private FileSystem setSourceFileSystem(FileSystem sourceFileSystem) {
		if (sourceFileSystem == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + FileSystem.class.getCanonicalName()
					+ ".");
		}
		FileSystem previous = getSourceFileSystem();
		_sourceFileSystem = sourceFileSystem;
		return previous;
	}

	protected FileSystem getDestinationFileSystem() {
		return _destinationFileSystem;
	}

	private FileSystem setDestinationFileSystem(FileSystem destinationFileSystem) {
		if (destinationFileSystem == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + FileSystem.class.getCanonicalName()
					+ ".");
		}
		FileSystem previous = getDestinationFileSystem();
		_destinationFileSystem = destinationFileSystem;
		return previous;
	}

	protected Transferable getTransferable() {
		return _transferable;
	}

	private Transferable setTransferable(Transferable t) {
		if (t == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ Transferable.class.getCanonicalName() + ".");
		}
		Transferable previous = getTransferable();
		_transferable = t;
		return previous;
	}

	protected TemplatingHandler getTemplatingHandler() {
		return _templatingHandler;
	}

	private TemplatingHandler setTemplatingHandler(TemplatingHandler th) {
		TemplatingHandler previous = getTemplatingHandler();
		_templatingHandler = th;
		return previous;
	}

}