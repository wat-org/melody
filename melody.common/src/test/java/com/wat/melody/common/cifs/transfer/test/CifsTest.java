package com.wat.melody.common.cifs.transfer.test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.wat.melody.common.cifs.transfer.CifsFileSystem4Upload;
import com.wat.melody.common.ex.MelodyException;
import com.wat.melody.common.transfer.TemplatingHandler;
import com.wat.melody.common.transfer.TransferableFileSystem;
import com.wat.melody.common.transfer.exception.TemplatingException;
import com.wat.melody.common.transfer.resources.attributes.AttributeDosReadOnly;
import com.wat.melody.common.transfer.resources.attributes.Scopes;

public class CifsTest {

	public static void main(String[] args) throws Exception {

		TemplatingHandler th = new TemplatingHandler() {

			@Override
			public Path doTemplate(Path template, Path destination)
					throws TemplatingException {
				// don't want to template anything in this test case
				return template;
			}
		};

		TransferableFileSystem cifs = new CifsFileSystem4Upload(
				"192.168.122.9", null, "rdpuser1", "abc@#123", th);

		Path dir = Paths.get("/Users/rdpuser1/Documents/superdir");
		Path src = Paths.get("/tmp/mescoudes.xml");
		Path dest = Paths.get("/Users/rdpuser1/Documents/mesgenoux.xml");

		try {

			AttributeDosReadOnly attr = new AttributeDosReadOnly();
			attr.setScopes(Scopes.ALL);
			attr.setStringValue("true");

			cifs.createDirectory(dir, attr);
			cifs.transferRegularFile(src, dest, attr);

		} catch (IOException Ex) {
			System.out.println(new MelodyException(Ex)
					.getUserFriendlyStackTrace());
		}

		// LocalFileSystem lfs = new LocalFileSystem();
		//
		// Path dir =
		// Paths.get("/Users/rdpuser1/Documents/fsdfsdf/sdfsdf/sdfsd");
		// try {
		// lfs.setAttributes(dir, attr);
		// } catch (IOException Ex) {
		// System.out.println(new
		// MelodyException(Ex).getUserFriendlyStackTrace());
		// }

	}

}