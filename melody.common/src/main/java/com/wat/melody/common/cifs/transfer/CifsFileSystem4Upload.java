package com.wat.melody.common.cifs.transfer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.nio.file.AccessDeniedException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;

import jcifs.smb.NtStatus;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

import com.wat.melody.common.cifs.transfer.exception.WrapperNoSuchShareException;
import com.wat.melody.common.cifs.transfer.exception.WrapperSmbException;
import com.wat.melody.common.ex.WrapperInterruptedIOException;
import com.wat.melody.common.files.exception.IllegalFileAttributeException;
import com.wat.melody.common.files.exception.WrapperAccessDeniedException;
import com.wat.melody.common.files.exception.WrapperDirectoryNotEmptyException;
import com.wat.melody.common.files.exception.WrapperNoSuchFileException;
import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.ssh.impl.transfer.ProgressMonitor;
import com.wat.melody.common.transfer.TemplatingHandler;
import com.wat.melody.common.transfer.TransferableFileSystem;
import com.wat.melody.common.transfer.exception.TemplatingException;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public class CifsFileSystem4Upload extends CifsFileSystem implements
		TransferableFileSystem {

	private TemplatingHandler _templatingHandler;

	public CifsFileSystem4Upload(String location, String domain, String user,
			String password, TemplatingHandler th) {
		super(location, domain, user, password);
		setTemplatingHandler(th);
	}

	protected TemplatingHandler getTemplatingHandler() {
		return _templatingHandler;
	}

	protected TemplatingHandler setTemplatingHandler(TemplatingHandler th) {
		if (th == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid "
					+ TemplatingHandler.class.getCanonicalName() + ".");
		}
		TemplatingHandler previous = getTemplatingHandler();
		_templatingHandler = th;
		return previous;
	}

	@Override
	public void transferRegularFile(Path src, Path dest,
			FileAttribute<?>... attrs) throws IOException,
			InterruptedIOException, NoSuchFileException,
			DirectoryNotEmptyException, AccessDeniedException,
			IllegalFileAttributeException {
		upload(src, dest);
		setAttributes(dest, attrs);
	}

	@Override
	public void transformRegularFile(Path src, Path dest,
			FileAttribute<?>... attrs) throws TemplatingException, IOException,
			InterruptedIOException, NoSuchFileException,
			DirectoryNotEmptyException, AccessDeniedException,
			IllegalFileAttributeException {
		// expand src into a tmpfile and upload the tmpfile into dest
		upload(getTemplatingHandler().doTemplate(src, null), dest);
		setAttributes(dest, attrs);
	}

	private void upload(Path source, Path destination) throws IOException,
			InterruptedIOException, NoSuchFileException,
			DirectoryNotEmptyException, AccessDeniedException {
		upload(source.toString(), convertToUnixPath(destination));
	}

	private void upload(String source, String destination) throws IOException,
			InterruptedIOException, NoSuchFileException,
			DirectoryNotEmptyException, AccessDeniedException {
		if (source == null || source.trim().length() == 0) {
			throw new IllegalArgumentException(source + ": Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ ".");
		}
		if (destination == null || destination.trim().length() == 0) {
			throw new IllegalArgumentException(destination + ": Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ ".");
		}
		ProgressMonitor pm = new ProgressMonitor(null, getLocation());
		FileInputStream fis = null;
		OutputStream fos = null;
		byte[] datas = null;
		try {
			SmbFile smbfile = createSmbFile(destination);
			fis = new FileInputStream(source);
			fos = smbfile.getOutputStream();

			int read = -1;
			datas = new byte[1024];
			pm.init(0, source, destination, new File(source).length());
			while ((read = fis.read(datas)) > 0) {
				fos.write(datas, 0, read);
				pm.count(read);
				if (Thread.interrupted()) {
					throw new InterruptedIOException();
				}
			}
			pm.end();
		} catch (SmbException Ex) {
			WrapperSmbException wex = new WrapperSmbException(Ex);
			if (containsInterruptedException(Ex)) {
				throw new InterruptedIOException(
						Messages.CifsEx_PUT_INTERRUPTED);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_ACCESS_DENIED) {
				throw new WrapperAccessDeniedException(destination, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_NO_SUCH_USER) {
				throw new WrapperAccessDeniedException(destination, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_WRONG_PASSWORD) {
				throw new WrapperAccessDeniedException(destination, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_LOGON_FAILURE) {
				throw new WrapperAccessDeniedException(destination, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_ACCOUNT_RESTRICTION) {
				throw new WrapperAccessDeniedException(destination, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_INVALID_LOGON_HOURS) {
				throw new WrapperAccessDeniedException(destination, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_PASSWORD_EXPIRED) {
				throw new WrapperAccessDeniedException(destination, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_ACCOUNT_DISABLED) {
				throw new WrapperAccessDeniedException(destination, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_OBJECT_NAME_NOT_FOUND) {
				throw new WrapperNoSuchFileException(destination, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_OBJECT_PATH_NOT_FOUND) {
				throw new WrapperNoSuchFileException(destination, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_BAD_NETWORK_NAME) {
				throw new WrapperNoSuchShareException(destination, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_FILE_IS_A_DIRECTORY) {
				throw new WrapperDirectoryNotEmptyException(destination, wex);
			} else {
				throw new IOException(Msg.bind(Messages.CifsEx_PUT, source,
						destination), wex);
			}
		} catch (FileNotFoundException Ex) {
			if (Ex.getMessage().indexOf(" (Permission denied)") != -1) {
				throw new WrapperAccessDeniedException(source);
			} else if (Ex.getMessage().indexOf(" (No such file or directory)") != -1) {
				throw new WrapperNoSuchFileException(source);
			} else if (Ex.getMessage().indexOf(" (Is a directory)") != -1) {
				throw new WrapperDirectoryNotEmptyException(source);
			} else {
				throw new WrapperNoSuchFileException(source, Ex);
			}
		} catch (InterruptedIOException Ex) {
			throw new WrapperInterruptedIOException(
					Messages.CifsEx_PUT_INTERRUPTED, Ex);
		} catch (IOException Ex) {
			if (Thread.interrupted()) {
				throw new InterruptedIOException(
						Messages.CifsEx_PUT_INTERRUPTED);
			} else {
				throw new IOException(Msg.bind(Messages.CifsEx_PUT, source,
						destination), Ex);
			}
		} finally {
			datas = null;
			if (fos != null)
				fos.close();
			if (fis != null)
				fis.close();
		}
	}

}