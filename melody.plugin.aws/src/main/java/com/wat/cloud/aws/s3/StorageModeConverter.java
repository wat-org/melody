package com.wat.cloud.aws.s3;

import com.amazonaws.services.s3.model.CryptoStorageMode;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
public abstract class StorageModeConverter {

	public static StorageMode convert(CryptoStorageMode cryptoStorageMode) {
		switch (cryptoStorageMode) {
		case InstructionFile:
			return StorageMode.FILE;
		case ObjectMetadata:
			return StorageMode.METADATA;
		default:
			throw new RuntimeException("BUG ! '" + cryptoStorageMode
					+ "' is not supported. "
					+ "This method should handle this.");
		}
	}

	public static CryptoStorageMode convert(StorageMode storageMode) {
		switch (storageMode) {
		case FILE:
			return CryptoStorageMode.InstructionFile;
		case METADATA:
			return CryptoStorageMode.ObjectMetadata;
		default:
			throw new RuntimeException("BUG ! '" + storageMode
					+ "' is not supported. "
					+ "This method should handle this.");
		}
	}

}