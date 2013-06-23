package com.wat.melody.common.ssh.impl;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.SftpException;
import com.wat.melody.common.messages.Msg;
import com.wat.melody.common.ssh.Messages;
import com.wat.melody.common.ssh.types.RemoteResource;
import com.wat.melody.common.ssh.types.ResourceMatcher;
import com.wat.melody.common.ssh.types.Resources;
import com.wat.melody.common.systool.SysTool;

/**
 * 
 * @author Guillaume Cornet
 * 
 */
abstract class DownloaderHelper {

	protected static List<RemoteResource> findResources(ChannelSftp chan,
			Resources resources) throws DownloaderException {
		if (resources == null) {
			return new ArrayList<RemoteResource>();
		}
		if (chan == null) {
			throw new IllegalArgumentException("null: Not accepted. "
					+ "Must be a valid " + ChannelSftp.class.getCanonicalName()
					+ ".");
		}
		if (!chan.isConnected()) {
			throw new IllegalStateException("Channel must be a connected.");
		}

		List<RemoteResource> res = findMatchingResources(chan, resources);
		for (ResourceMatcher r : resources.getIncludes()) {
			updateMatchingRemoteResources(res, r);
		}
		for (ResourceMatcher r : resources.getExcludes()) {
			excludeMatchingRemoteResources(res, r);
		}
		return res;
	}

	private static List<RemoteResource> findMatchingResources(ChannelSftp chan,
			ResourceMatcher resourceMatcher) throws DownloaderException {
		if (resourceMatcher == null) {
			return new ArrayList<RemoteResource>();
		}
		// Recurs lists remote files from the remote basedir
		String dir = convertToUnixPath(resourceMatcher.getRemoteBaseDir());
		List<RemoteResource> res = listrecurs(chan, dir, resourceMatcher);
		excludeNotMatchingRemoteResources(res, resourceMatcher);
		return res;
	}

	private static List<RemoteResource> listrecurs(ChannelSftp chan,
			String dir, ResourceMatcher resourceMatcher)
			throws DownloaderException {
		List<RemoteResource> res = new ArrayList<RemoteResource>();
		List<RemoteResource> listing = list(chan, dir, resourceMatcher);
		for (RemoteResource rm : listing) {
			res.add(rm);
			if (rm.isDir()) {
				String unixDir = convertToUnixPath(rm.getPath());
				try {
					res.addAll(listrecurs(chan, unixDir, resourceMatcher));
				} catch (DownloaderException Ex) {
					throw new DownloaderException(Msg.bind(
							Messages.DownloadEx_LIST, dir), Ex);
				}
			}
		}
		return res;
	}

	@SuppressWarnings("unchecked")
	private static List<RemoteResource> list(ChannelSftp chan, String dir,
			ResourceMatcher resourceMatcher) throws DownloaderException {
		List<RemoteResource> res = new ArrayList<RemoteResource>();
		Vector<LsEntry> listing;
		try {
			listing = chan.ls(dir);
		} catch (SftpException Ex) {
			throw new DownloaderException(Msg.bind(Messages.DownloadEx_LIST,
					dir), Ex);
		}
		for (LsEntry entry : listing) {
			if (entry.getAttrs().isDir()
					&& (entry.getFilename().equals(".") || entry.getFilename()
							.equals(".."))) {
				continue;
			}
			res.add(new RemoteResource(Paths.get(dir + "/"
					+ entry.getFilename()), entry.getAttrs(), resourceMatcher));
		}
		return res;
	}

	private static List<RemoteResource> excludeNotMatchingRemoteResources(
			List<RemoteResource> remoteResources,
			ResourceMatcher resourceMatcher) {
		List<RemoteResource> notMatching = new ArrayList<RemoteResource>();
		String path = Paths.get(resourceMatcher.getRemoteBaseDir()).normalize()
				+ SysTool.FILE_SEPARATOR + resourceMatcher.getMatch();
		/*
		 * As indicated in the javadoc of {@link FileSystem#getPathMatcher()},
		 * the backslash is escaped; string literal example : "C:\\\\*"
		 */
		String pattern = "glob:" + path.replaceAll("\\\\", "\\\\\\\\");
		PathMatcher matcher = FileSystems.getDefault().getPathMatcher(pattern);
		for (RemoteResource r : remoteResources) {
			if (!matcher.matches(r.getPath())) {
				notMatching.add(r);
			}
		}
		remoteResources.removeAll(notMatching);
		return remoteResources;
	}

	private static void updateMatchingRemoteResources(
			List<RemoteResource> remoteResources,
			ResourceMatcher resourceMatcher) {
		String path = Paths.get(resourceMatcher.getRemoteBaseDir()).normalize()
				+ SysTool.FILE_SEPARATOR + resourceMatcher.getMatch();
		/*
		 * As indicated in the javadoc of {@link FileSystem#getPathMatcher()},
		 * the backslash is escaped; string literal example : "C:\\\\*"
		 */
		String pattern = "glob:" + path.replaceAll("\\\\", "\\\\\\\\");
		PathMatcher matcher = FileSystems.getDefault().getPathMatcher(pattern);
		for (RemoteResource r : remoteResources) {
			if (matcher.matches(r.getPath())) {
				r.setResourceMatcher(resourceMatcher);
			}
		}
	}

	private static List<RemoteResource> excludeMatchingRemoteResources(
			List<RemoteResource> remoteResources,
			ResourceMatcher resourceMatcher) {
		List<RemoteResource> matching = new ArrayList<RemoteResource>();
		String path = Paths.get(resourceMatcher.getRemoteBaseDir()).normalize()
				+ SysTool.FILE_SEPARATOR + resourceMatcher.getMatch();
		/*
		 * As indicated in the javadoc of {@link FileSystem#getPathMatcher()},
		 * the backslash is escaped; string literal example : "C:\\\\*"
		 */
		String pattern = "glob:" + path.replaceAll("\\\\", "\\\\\\\\");
		PathMatcher matcher = FileSystems.getDefault().getPathMatcher(pattern);
		for (RemoteResource r : remoteResources) {
			if (matcher.matches(r.getPath())) {
				matching.add(r);
			}
		}
		remoteResources.removeAll(matching);
		return remoteResources;
	}

	private static String convertToUnixPath(String path) {
		return path.replaceAll("\\\\", "/");
	}

	private static String convertToUnixPath(Path path) {
		return convertToUnixPath(path.toString());
	}

}