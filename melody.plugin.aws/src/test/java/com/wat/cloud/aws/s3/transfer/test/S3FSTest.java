package com.wat.cloud.aws.s3.transfer.test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import com.wat.cloud.aws.s3.BucketName;
import com.wat.cloud.aws.s3.transfer.AwsS3FileSystem4Download;
import com.wat.melody.common.ex.MelodyException;
import com.wat.melody.common.transfer.TemplatingHandler;
import com.wat.melody.common.transfer.TransferableFileSystem;
import com.wat.melody.common.transfer.exception.TemplatingException;
import com.wat.melody.plugin.aws.common.AwsPlugInConfiguration;

public class S3FSTest {

	private static Logger log = LoggerFactory.getLogger(S3FSTest.class);

	static {
		// will redirect (e.g. bridge) JUL (java.util.logging) to SLF4J
		SLF4JBridgeHandler.removeHandlersForRootLogger();
		SLF4JBridgeHandler.install();
	}

	public static void main(String[] args) throws Exception {

		AwsPlugInConfiguration conf = new AwsPlugInConfiguration();
		conf.setAccessKey("AKIAJWC7HLWMNAPFAUSQ");
		conf.setSecretKey("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");

		TemplatingHandler th = new TemplatingHandler() {

			@Override
			public Path doTemplate(Path template, Path destination)
					throws TemplatingException {
				// don't want to template anything in this test case
				return template;
			}
		};

		TransferableFileSystem s3fs = new AwsS3FileSystem4Download(
				conf.getAwsS3Connection(), BucketName.parseString("couille"),
				th);

		Path dir1 = Paths.get("/superdir");
		Path dir2 = Paths.get("/superdir/inside");
		Path dir3 = Paths.get("/superdir/genial/mesgenoux");
		Path dir4 = Paths.get("/anotherdir");
		Path src = Paths.get("/tmp/melody/scp/UC_1_upload/sd.xml");
		Path dest = Paths.get("/tmp/mescoudes.xml");

		try {
			// s3fs.readAttributes(target);

			// s3fs.createDirectory(dir1);
			// s3fs.createDirectory(dir2);
			// s3fs.createDirectories(dir3);

			// s3fs.deleteDirectory(dir4);
			// s3fs.delete(dir2);
			// s3fs.delete(dir1);
			// s3fs.delete(file);
			// s3fs.createDirectories(dir1);
			// s3fs.createDirectories(dir2);
			// s3fs.createDirectories(dir3);

			s3fs.transferRegularFile(src, dest);

			// DirectoryStream<Path> stream = s3fs.newDirectoryStream(dir1);
			// for (Path path : stream) {
			// System.out.println(path);
			// }
			// AttributeDosReadOnly attr = new AttributeDosReadOnly();
			// attr.setScopes(Scopes.ALL);
			// attr.setStringValue("true");

			// EnhancedFileAttributes attr = null;
			// attr = s3fs.readAttributes("/superdir/.java_pid17174");
			// System.out.println(attr);

			// NamedAttribute attr1 = new NamedAttribute();
			// attr1.setName("supergenia/lattribute");
			// attr1.setStringValue("OLDsupergenialvalue");
			// NamedAttribute attr2 = new NamedAttribute();
			// attr2.setName("supergeniala:ttribute2");
			// attr2.setStringValue("supergenialvalue2");
			// s3fs.setAttributes("/superdir/.java_pid17174", attr1, attr2);

			// attr = s3fs.readAttributes("/superdir/.java_pid17174");
			// System.out.println(attr);
		} catch (IOException Ex) {
			log.error(new MelodyException(Ex).getUserFriendlyStackTrace());
		}

		// LocalFileSystem lfs = new LocalFileSystem();
		//
		// Path dir = Paths.get("/tmp/mescoudes.xml/superdir");
		// try {
		// lfs.createDirectory(dir);
		// } catch (IOException Ex) {
		// System.out.println(new MelodyException(Ex)
		// .getUserFriendlyStackTrace());
		// }

	}

}