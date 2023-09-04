package com.github.ecsoya.bear.common.utils.file;

import java.io.File;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.sevenzipjbinding.ArchiveFormat;
import net.sf.sevenzipjbinding.ExtractAskMode;
import net.sf.sevenzipjbinding.ExtractOperationResult;
import net.sf.sevenzipjbinding.IArchiveExtractCallback;
import net.sf.sevenzipjbinding.IInArchive;
import net.sf.sevenzipjbinding.ISequentialOutStream;
import net.sf.sevenzipjbinding.PropID;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.SevenZipException;
import net.sf.sevenzipjbinding.SevenZipNativeInitializationException;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;
import net.sf.sevenzipjbinding.simple.ISimpleInArchive;
import net.sf.sevenzipjbinding.simple.ISimpleInArchiveItem;

public class SevenZipUtil {

	private static final Logger log = LoggerFactory.getLogger(SevenZipUtil.class);

	private SevenZipUtil() {
	}

	private static String getPlatformJar() {
		try {
			return SevenZip.getPlatformBestMatch();
		} catch (SevenZipNativeInitializationException e) {
			return null;
		}
	}

	public static boolean extract(File file, File dir, ArchiveFormat format) {
		if (!SevenZip.isInitializedSuccessfully()) {

			String platform = getPlatformJar();
			log.warn("SevenZip not initialized");
			try {
				log.info("Try to load SevenZip: {}", platform);
				SevenZip.initSevenZipFromPlatformJAR(platform);
			} catch (SevenZipNativeInitializationException e) {
				log.warn("Try to load SevenZip failed", e);
			}
			if (!SevenZip.isInitializedSuccessfully()) {
				log.warn("SevenZip unsupported");
				return false;
			}
		}
		if (file == null || !file.exists() || dir == null) {
			return false;
		}
		log.info("Extract: {} => {}, {}", file.getPath(), dir.getPath(), format);
		try (RandomAccessFile raf = new RandomAccessFile(file, "r");
				IInArchive archive = SevenZip.openInArchive(format, new RandomAccessFileInStream(raf))) {
			/*
			 * log.info("   Hash   |    Size    | Filename");
			 * log.info("----------+------------+---------");
			 * 
			 * int count = archive.getNumberOfItems(); List<Integer> itemsToExtract = new
			 * ArrayList<Integer>(); for (int i = 0; i < count; i++) { if (!((Boolean)
			 * archive.getProperty(i, PropID.IS_FOLDER)).booleanValue()) {
			 * itemsToExtract.add(Integer.valueOf(i)); } } int[] items = new
			 * int[itemsToExtract.size()]; int i = 0; for (Integer integer : itemsToExtract)
			 * { items[i++] = integer.intValue(); } archive.extract(items, false, new
			 * ExtractCallback(archive, dir));
			 */
			ISimpleInArchive simpleInterface = archive.getSimpleInterface();
			ISimpleInArchiveItem[] archiveItems = simpleInterface.getArchiveItems();
			if (archiveItems != null) {
				log.info("Extract: {} items", archiveItems.length);
				for (ISimpleInArchiveItem item : archiveItems) {
					final String path = item.getPath();
					boolean folder = item.isFolder();
					if (folder) {
						continue;
					}
					final long[] sizeArray = new long[1];
					final File target = new File(dir, path);
					final String[] oldPath = { "" };
					target.getParentFile().mkdirs();
					ExtractOperationResult result = item.extractSlow(data -> {
						try (FileOutputStream fos = new FileOutputStream(target, oldPath[0].equals(path))) {
							fos.write(data);
						} catch (Exception e) {
							throw new SevenZipException(e);
						}
						oldPath[0] = path;
						sizeArray[0] += data.length;
						return data.length;
					});
					if (result == ExtractOperationResult.OK) {
						log.info(String.format("%s | %10s", //
								path, sizeArray[0]));
					} else {
						log.error("Error extracting item: {}", result);
					}
				}
			}
		} catch (Exception e) {
			log.error("Extract failed", e);
			return false;
		}
		return true;
	}

	public static class ExtractCallback implements IArchiveExtractCallback {
		private int hash = 0;
		private int size = 0;
		private int index;
		private boolean skipExtraction;
		private IInArchive archive;
		private File dir;

		public ExtractCallback(IInArchive inArchive, File dir) {
			this.archive = inArchive;
			this.dir = dir;
		}

		public ISequentialOutStream getStream(int index, ExtractAskMode extractAskMode) throws SevenZipException {
			this.index = index;
			skipExtraction = (Boolean) archive.getProperty(index, PropID.IS_FOLDER);
			if (skipExtraction || extractAskMode != ExtractAskMode.EXTRACT) {
				return null;
			}
			final String path = (String) archive.getProperty(index, PropID.PATH);
			final File file = new File(dir, path);
			file.getParentFile().mkdirs();
			final String[] oldPath = { "" };
			return new ISequentialOutStream() {
				public int write(byte[] data) throws SevenZipException {
					try (FileOutputStream fos = new FileOutputStream(file, path.equals(oldPath[0]))) {
						fos.write(data);
					} catch (Exception e) {
						throw new SevenZipException(e);
					}
					oldPath[0] = path;
					hash ^= Arrays.hashCode(data);
					size += data.length;
					return data.length; // Return amount of proceed data
				}
			};
		}

		public void prepareOperation(ExtractAskMode extractAskMode) throws SevenZipException {
		}

		public void setOperationResult(ExtractOperationResult extractOperationResult) throws SevenZipException {
			if (skipExtraction) {
				return;
			}
			if (extractOperationResult != ExtractOperationResult.OK) {
				log.error("Extraction error");
			} else {
				log.info(String.format("%9X | %10s | %s", hash, size, //
						archive.getProperty(index, PropID.PATH)));
				hash = 0;
				size = 0;
			}
		}

		public void setCompleted(long completeValue) throws SevenZipException {
		}

		public void setTotal(long total) throws SevenZipException {
		}
	}
}
