package com.wat.melody.common.cifs.transfer;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;

import jcifs.smb.NtStatus;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

import com.wat.melody.common.cifs.transfer.exception.WrapperNoSuchShareException;
import com.wat.melody.common.cifs.transfer.exception.WrapperSmbException;
import com.wat.melody.common.ex.WrapperInterruptedIOException;
import com.wat.melody.common.files.LocalFileSystem;
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
public class CifsFileSystem4Download extends LocalFileSystem implements
		TransferableFileSystem {

	private NtlmPasswordAuthentication _smbCredential;
	private String _smbLocation;
	private TemplatingHandler _templatingHandler;

	public CifsFileSystem4Download(String location, String domain, String user,
			String password, TemplatingHandler th) {
		super();
		if (location == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ ".");
		}
		// if domain, user or pass is null, default values will be used
		_smbCredential = new NtlmPasswordAuthentication(domain, user, password);
		_smbLocation = "smb://" + location + "/";
		setTemplatingHandler(th);
	}

	protected String getLocation() {
		return _smbLocation;
	}

	protected NtlmPasswordAuthentication getCredential() {
		return _smbCredential;
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

	protected SmbFile createSmbFile(String path, LinkOption... options) {
		return CifsFileSystem.createSmbFile(getLocation(), path,
				getCredential(), options);
	}

	@Override
	public void transferRegularFile(Path src, Path dest,
			FileAttribute<?>... attrs) throws IOException,
			InterruptedIOException, NoSuchFileException,
			DirectoryNotEmptyException, AccessDeniedException,
			IllegalFileAttributeException {
		download(src, dest);
		setAttributes(dest, attrs);
	}

	@Override
	public void transformRegularFile(Path src, Path dest,
			FileAttribute<?>... attrs) throws TemplatingException, IOException,
			InterruptedIOException, NoSuchFileException,
			DirectoryNotEmptyException, AccessDeniedException,
			IllegalFileAttributeException {
		// download the file
		download(src, dest);
		// expand the into itself
		getTemplatingHandler().doTemplate(dest, dest);
		setAttributes(dest, attrs);
	}

	private void download(Path source, Path destination) throws IOException,
			InterruptedIOException, NoSuchFileException,
			DirectoryNotEmptyException, AccessDeniedException {
		download(CifsFileSystem.convertToUnixPath(source),
				destination.toString());
	}

	private void download(String source, String destination)
			throws IOException, InterruptedIOException, NoSuchFileException,
			DirectoryNotEmptyException, AccessDeniedException {
		// must validate source
		if (source == null || source.trim().length() == 0) {
			throw new IllegalArgumentException(source + ": Not accepted. "
					+ "Must be a valid " + String.class.getCanonicalName()
					+ ".");
		}
		// TODO : Should fail if source is a directory
		// Fail if destination is a directory
		if (isDirectory(destination)) {
			throw new WrapperDirectoryNotEmptyException(destination);
		}
		ProgressMonitor pm = new ProgressMonitor(getLocation(), null);
		InputStream fis = null;
		FileOutputStream fos = null;
		byte[] datas = null;
		try {
			SmbFile smbfile = createSmbFile(source);
			fis = smbfile.getInputStream();
			fos = new FileOutputStream(destination);

			int read = -1;
			datas = new byte[1024];
			pm.init(0, source, destination, smbfile.length());
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
			if (CifsFileSystem.containsInterruptedException(Ex)) {
				throw new InterruptedIOException(
						Messages.CifsEx_GET_INTERRUPTED);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_ACCESS_DENIED) {
				throw new WrapperAccessDeniedException(source, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_NO_SUCH_USER) {
				throw new WrapperAccessDeniedException(source, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_WRONG_PASSWORD) {
				throw new WrapperAccessDeniedException(source, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_LOGON_FAILURE) {
				throw new WrapperAccessDeniedException(source, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_ACCOUNT_RESTRICTION) {
				throw new WrapperAccessDeniedException(source, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_INVALID_LOGON_HOURS) {
				throw new WrapperAccessDeniedException(source, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_PASSWORD_EXPIRED) {
				throw new WrapperAccessDeniedException(source, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_ACCOUNT_DISABLED) {
				throw new WrapperAccessDeniedException(source, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_OBJECT_NAME_NOT_FOUND) {
				throw new WrapperNoSuchFileException(source, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_OBJECT_PATH_NOT_FOUND) {
				throw new WrapperNoSuchFileException(source, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_BAD_NETWORK_NAME) {
				throw new WrapperNoSuchShareException(source, wex);
			} else if (Ex.getNtStatus() == NtStatus.NT_STATUS_FILE_IS_A_DIRECTORY) {
				throw new WrapperDirectoryNotEmptyException(source, wex);
			} else {
				throw new IOException(Msg.bind(Messages.CifsEx_GET, source,
						destination), wex);
			}
		} catch (FileNotFoundException Ex) {
			String msg = Ex.getMessage();
			if (msg != null && msg.indexOf(" (Permission denied)") != -1) {
				throw new WrapperAccessDeniedException(destination);
			} else if (msg != null
					&& msg.indexOf(" (No such file or directory)") != -1) {
				throw new WrapperNoSuchFileException(destination);
			} else if (msg != null && msg.indexOf(" (Is a directory)") != -1) {
				throw new WrapperDirectoryNotEmptyException(destination);
			} else {
				throw new WrapperNoSuchFileException(destination, Ex);
			}
		} catch (InterruptedIOException Ex) {
			throw new WrapperInterruptedIOException(
					Messages.CifsEx_GET_INTERRUPTED, Ex);
		} catch (IOException Ex) {
			if (Thread.interrupted()) {
				throw new InterruptedIOException(
						Messages.CifsEx_GET_INTERRUPTED);
			} else {
				throw new IOException(Msg.bind(Messages.CifsEx_GET, source,
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