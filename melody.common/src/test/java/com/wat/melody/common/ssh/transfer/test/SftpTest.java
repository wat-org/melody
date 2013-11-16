package com.wat.melody.common.ssh.transfer.test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.jcraft.jsch.ChannelSftp;
import com.wat.melody.common.ex.MelodyException;
import com.wat.melody.common.network.Host;
import com.wat.melody.common.ssh.ISshConnectionDatas;
import com.wat.melody.common.ssh.ISshUserDatas;
import com.wat.melody.common.ssh.impl.SshConnectionDatas;
import com.wat.melody.common.ssh.impl.SshSession;
import com.wat.melody.common.ssh.impl.SshUserDatas;
import com.wat.melody.common.ssh.impl.transfer.SftpFileSystem4Download;
import com.wat.melody.common.transfer.TemplatingHandler;
import com.wat.melody.common.transfer.TransferableFileSystem;
import com.wat.melody.common.transfer.exception.TemplatingException;
import com.wat.melody.common.transfer.resources.attributes.AttributeDosReadOnly;
import com.wat.melody.common.transfer.resources.attributes.Scopes;

public class SftpTest {

	public static void main(String[] args) throws Exception {

		TemplatingHandler th = new TemplatingHandler() {

			@Override
			public Path doTemplate(Path template, Path destination)
					throws TemplatingException {
				// don't want to template anything in this test case
				return template;
			}
		};

		ISshUserDatas ud = new SshUserDatas();
		ud.setLogin("login");
		ud.setPassword("password");
		ISshConnectionDatas cd = new SshConnectionDatas();
		cd.setHost(Host.parseString("127.0.0.1"));
		cd.setTrust(true);

		SshSession session = new SshSession(ud, cd);
		session.connect();

		ChannelSftp channel = session.openSftpChannel();
		TransferableFileSystem sftpfs = new SftpFileSystem4Download(channel, th);

		Path dir = Paths.get("/tmp/rdpuser1/Documents/superdir");
		Path src = Paths.get("/tmp/mescoudes.xml");
		Path dest = Paths.get("/tmp/rdpuser1/Documents/superdir");

		try {

			AttributeDosReadOnly attr = new AttributeDosReadOnly();
			attr.setScopes(Scopes.ALL);
			attr.setStringValue("true");

			// will fail cause parent doesn't exists
			sftpfs.createDirectory(dir, attr);
			// will fail cause dest parent doesn't exists
			sftpfs.transferRegularFile(src, dest, attr);

		} catch (IOException Ex) {
			System.out.println(new MelodyException(Ex)
					.getUserFriendlyStackTrace());
		}
		channel.disconnect();
		session.disconnect();
	}

}