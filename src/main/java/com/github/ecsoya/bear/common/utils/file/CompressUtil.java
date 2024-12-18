package com.github.ecsoya.bear.common.utils.file;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

public class CompressUtil {

	public static final Long ZIP_ENTRY_MAX = FileUtils.ONE_GB * 4;

	public static boolean zip2(File zip, File... files) throws ArchiveException, IOException {
		if (zip == null || files == null || files.length == 0) {
			return false;
		}
		try (OutputStream archiveStream = new FileOutputStream(zip);
				ArchiveOutputStream archive = new ArchiveStreamFactory()
						.createArchiveOutputStream(ArchiveStreamFactory.ZIP, archiveStream)) {
			for (File source : files) {
				if (source.isDirectory()) {
					Collection<File> fileList = FileUtils.listFiles(source, null, true);

					for (File file : fileList) {
						String entryName = getEntryName(source, file, true);
						ZipArchiveEntry entry = new ZipArchiveEntry(entryName);
						archive.putArchiveEntry(entry);

						try (BufferedInputStream input = new BufferedInputStream(new FileInputStream(file))) {
							IOUtils.copy(input, archive);
							archive.closeArchiveEntry();
						}
					}
				} else {
					String entryName = source.getName();
					ZipArchiveEntry entry = new ZipArchiveEntry(entryName);
					archive.putArchiveEntry(entry);

					try (BufferedInputStream input = new BufferedInputStream(new FileInputStream(source))) {
						IOUtils.copy(input, archive);
						archive.closeArchiveEntry();
					}
				}

			}
			archive.finish();
			return true;
		}
	}

	public static void zip(File source, File zipFile) throws IOException, ArchiveException {
		try (OutputStream archiveStream = new FileOutputStream(zipFile);
				ArchiveOutputStream archive = new ArchiveStreamFactory()
						.createArchiveOutputStream(ArchiveStreamFactory.ZIP, archiveStream)) {

			Collection<File> fileList = FileUtils.listFiles(source, null, true);

			for (File file : fileList) {
				String entryName = getEntryName(source, file, false);
				ZipArchiveEntry entry = new ZipArchiveEntry(entryName);
				archive.putArchiveEntry(entry);

				try (BufferedInputStream input = new BufferedInputStream(new FileInputStream(file))) {
					IOUtils.copy(input, archive);
					archive.closeArchiveEntry();
				}
			}

			archive.finish();
		}
	}

	/**
	 * Remove the leading part of each entry that contains the source directory name
	 *
	 * @param source the directory where the file entry is found
	 * @param file   the file that is about to be added
	 * @return the name of an archive entry
	 * @throws IOException if the io fails
	 */
	private static String getEntryName(File source, File file, boolean withSource) throws IOException {
		String name = source.toPath().relativize(file.toPath()).toString();
		if (withSource) {
			return source.getName() + File.separator + name;
		}
		return name;
	}

	public static boolean gzip(File source, File gzip) {
		try (FileInputStream fis = new FileInputStream(source);
				GZIPOutputStream gzipOS = new GZIPOutputStream(new FileOutputStream(gzip))) {
			IOUtils.copy(fis, gzipOS);
		} catch (IOException e) {
			return false;
		}
		return true;
	}

}