package com.github.ecsoya.bear.common.utils.file;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.zip.GZIPInputStream;

import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.junrar.Archive;
import com.github.junrar.exception.UnsupportedRarV5Exception;
import com.github.junrar.rarfile.FileHeader;
import com.github.junrar.rarfile.MainHeader;

import net.sf.sevenzipjbinding.ArchiveFormat;

public class DecompressUtil {

	private static final Logger log = LoggerFactory.getLogger("Decompress");

	public static FileType getFileType(File file) {
		if (file == null || !file.exists()) {
			return FileType.UNKNOWN;
		}
		String fileName = file.getName().toLowerCase();
		if (fileName.endsWith(".tar.gz")) {
			return FileType.TAR_GZ;
		} else if (fileName.endsWith("tar.bz2")) {
			return FileType.TAR_BZ2;
		} else if (fileName.endsWith(".zip")) {
			return FileType.ZIP;
		} else if (fileName.endsWith(".rar")) {
			return FileType.RAR;
		} else if (fileName.endsWith(".7z")) {
			return FileType._7Z;
		} else if (fileName.endsWith(".bz2")) {
			return FileType.BZ2;
		} else if (fileName.endsWith("tar")) {
			return FileType.TAR;
		} else if (fileName.endsWith("gz")) {
			return FileType.GZ;
		}
		try (FileInputStream inputStream = new FileInputStream(file)) {
			byte[] head = new byte[4];
			if (-1 == inputStream.read(head)) {
				return FileType.UNKNOWN;
			}
			int headHex = 0;
			for (byte b : head) {
				headHex <<= 8;
				headHex |= b;
			}
			switch (headHex) {
			case 0x504B0304:
				return FileType.ZIP;
			case 0x776f7264:
				return FileType.TAR;
			case -0x51:
				return FileType._7Z;
			case 0x425a6839:
				return FileType.BZ2;
			case -0x74f7f8:
				return FileType.GZ;
			case 0x52617221:
				return FileType.RAR;
			default:
				return FileType.UNKNOWN;
			}
		} catch (Exception e) {
			log.warn("Unknown type", e);
		}
		return FileType.UNKNOWN;
	}

	/**
	 * Decompress .tar
	 */
	private static boolean decompressTar(File file, File outputDir) {
		try (FileInputStream fis = new FileInputStream(file);
				TarArchiveInputStream tarInputStream = (TarArchiveInputStream) new ArchiveStreamFactory()
						.createArchiveInputStream(ArchiveStreamFactory.TAR, fis);) {
			TarArchiveEntry entry = null;
			while ((entry = (TarArchiveEntry) tarInputStream.getNextEntry()) != null) {
				final File outputFile = new File(outputDir, entry.getName());
				if (entry.isDirectory()) {
					if (!outputFile.exists()) {
						if (!outputFile.mkdirs()) {
							throw new IllegalStateException(
									String.format("Couldn't create directory %s.", outputFile.getAbsolutePath()));
						}
					}
				} else {
					Files.copy(tarInputStream, outputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
				}
			}
		} catch (Exception e) {
			log.warn("*.tar", e);
			return false;
		}
		return true;
	}

	/**
	 * Decompress .bz2
	 */
	private static boolean decompressBZ2(File file, File dir) {
		String fileName = file.getName();
		int index = fileName.lastIndexOf(".");
		if (index != -1) {
			fileName = fileName.substring(0, index);
		}
		File target = new File(dir, fileName);
		try (BZip2CompressorInputStream bis = new BZip2CompressorInputStream(new FileInputStream(file));
				FileOutputStream fos = new FileOutputStream(target)) {
			target.getParentFile().mkdirs();
			int count;
			byte data[] = new byte[2048];
			while ((count = bis.read(data)) != -1) {
				fos.write(data, 0, count);
			}
			fos.flush();
		} catch (IOException e) {
			log.warn("*.bz2", e);
			return false;
		}
		return true;
	}

	/**
	 * Decompress .tar.bz2
	 */
	private static boolean decompressTarBz2(File file, File dir) {
		try {
			TarArchiveInputStream tis = new TarArchiveInputStream(
					new BZip2CompressorInputStream(new FileInputStream(file)));
			TarArchiveEntry entry;
			while ((entry = tis.getNextTarEntry()) != null) {
				if (entry.isDirectory()) {
					new File(dir, entry.getName()).mkdirs();
				} else {
					try (FileOutputStream fos = new FileOutputStream(new File(dir, entry.getName()))) {
						int count;
						byte data[] = new byte[2048];
						while ((count = tis.read(data)) != -1) {
							fos.write(data, 0, count);
						}
						fos.flush();
					}
				}
			}
		} catch (IOException e) {
			log.warn("*.tag.bz2", e);
			return false;
		}
		return true;
	}

	/**
	 * Decompress .tar.gz
	 */
	private static boolean decompressTarGz(File file, File dir) {
		try (TarArchiveInputStream tarIn = new TarArchiveInputStream(
				new GZIPInputStream(new BufferedInputStream(new FileInputStream(file))))) {
			TarArchiveEntry entry = null;
			while ((entry = tarIn.getNextTarEntry()) != null) {
				if (entry.isDirectory()) { // 是目录
					new File(dir, entry.getName()).mkdirs();
				} else { // 是文件
					File item = new File(dir, entry.getName());
					item.getParentFile().mkdirs();
					try (OutputStream out = new FileOutputStream(item)) {
						int len = 0;
						byte[] b = new byte[2048];
						while ((len = tarIn.read(b)) != -1) {
							out.write(b, 0, len);
						}
						out.flush();
					}
				}
			}
		} catch (IOException e) {
			log.warn("*.tar.gz", e);
			return false;
		}
		return true;
	}

	/**
	 * Decompress .gz
	 */
	private static boolean decompressGz(File file, File dir) {
		String fileName = file.getName();
		int index = fileName.lastIndexOf(".");
		if (index != -1) {
			fileName = fileName.substring(0, index);
		}
		File tempFile = new File(dir, fileName);
		try (GZIPInputStream gzipIn = new GZIPInputStream(new FileInputStream(file));
				FileOutputStream fos = new FileOutputStream(tempFile)) {
			int count;
			byte data[] = new byte[2048];
			while ((count = gzipIn.read(data)) != -1) {
				fos.write(data, 0, count);
			}
			fos.flush();
		} catch (IOException e) {
			log.warn("*.gz", e);
			return false;
		}
		return true;
	}

	/**
	 * Decompress .7z
	 */
	private static boolean decompress7Z(File file, File dir) {
		try (SevenZFile sevenZFile = new SevenZFile(file)) {
			SevenZArchiveEntry entry;

			while ((entry = sevenZFile.getNextEntry()) != null) {
				if (entry.isDirectory()) {
					new File(dir, entry.getName()).mkdirs(); // 创建子目录
				} else {
					File target = new File(dir, entry.getName());
					target.getParentFile().mkdirs();
					try (FileOutputStream fos = new FileOutputStream(target)) {
						int len = 0;
						byte[] b = new byte[2048];
						while ((len = sevenZFile.read(b)) != -1) {
							fos.write(b, 0, len);
						}
						fos.flush();
					}
				}
			}
		} catch (IOException e) {
			log.warn("*.7z", e);
			return false;
		}
		return true;
	}

	/**
	 * Decompress .rar
	 */
	private static boolean decompressRAR(File file, File dir) {
		try (Archive archive = new Archive(file)) {
			MainHeader mainHeader = archive.getMainHeader();
			if (mainHeader == null) {
				return false;
			}
			mainHeader.print();
			FileHeader fileHeader;
			while ((fileHeader = archive.nextFileHeader()) != null) {
				if (fileHeader.isDirectory()) {
					new File(dir, fileHeader.getFileName().trim()).mkdirs(); // 创建子目录
				} else {
					File target = new File(dir, fileHeader.getFileName().trim());
					target.getParentFile().mkdirs();
					try (FileOutputStream outputStream = new FileOutputStream(target)) {
						archive.extractFile(fileHeader, outputStream);
					}
				}
			}
		} catch (Exception e) {
			if (e instanceof UnsupportedRarV5Exception) {
				return SevenZipUtil.extract(file, dir, ArchiveFormat.RAR5);
			}
			log.warn("*.rar", e);
			return false;
		}
		return true;
	}

	/**
	 * Decompress .zip
	 */
	public static boolean decompressZip(File zip, File dir) {
		if (zip == null || !zip.exists() || dir == null) {
			return false;
		}
		try {
			return decompressZip(zip, dir, "gbk");
		} catch (Exception e) {
			try {
				return decompressZip(zip, dir, "utf-8");
			} catch (Exception e1) {
				log.warn("*.zip", e);
				return false;
			}
		}
	}

	private static boolean decompressZip(File zip, File dir, String encoding) throws Exception {
		if (zip == null || !zip.exists() || dir == null) {
			return false;
		}
		try (ZipFile zipFile = new ZipFile(zip, encoding)) {
			if (dir.exists()) {
				FileUtils.cleanDirectory(dir);
			}
			Enumeration<ZipArchiveEntry> entries = zipFile.getEntries();
			while (entries.hasMoreElements()) {
				ZipArchiveEntry entry = entries.nextElement();
				String entryName = entry.getName();
				if (!entry.isDirectory()) {
					File file = new File(dir, entryName);
					if (!file.getParentFile().exists()) {
						file.getParentFile().mkdirs();
					}
					try (InputStream in = zipFile.getInputStream(entry);
							FileOutputStream out = new FileOutputStream(file)) {
						IOUtils.copy(in, out);
					}
				}
			}
		}
		return true;
	}

	public static boolean decompress(File compressFile, File outputDir) {
		if (compressFile == null || !compressFile.exists() || outputDir == null) {
			return false;
		}
		FileType fileType = getFileType(compressFile);
		if (fileType == null || FileType.UNKNOWN == fileType) {
			return false;
		}
		switch (fileType) {
		case ZIP:
			return decompressZip(compressFile, outputDir);
		case TAR_GZ:
			return decompressTarGz(compressFile, outputDir);
		case TAR_BZ2:
			return decompressTarBz2(compressFile, outputDir);
		case BZ2:
			return decompressBZ2(compressFile, outputDir);
		case _7Z:
			return decompress7Z(compressFile, outputDir);
		case RAR:
			return decompressRAR(compressFile, outputDir);
		case GZ:
			return decompressGz(compressFile, outputDir);
		case TAR:
			return decompressTar(compressFile, outputDir);
		default:
			break;
		}
		return false;
	}

	public static boolean isCompressed(File file) {
		FileType fileType = getFileType(file);
		return fileType != null && FileType.UNKNOWN != fileType;
	}

	public static void main(String[] args) {
		File file = new File("/Users/Ecsoya/Downloads/3.rar");
		System.out.println(decompress(file, new File("/Users/Ecsoya/Downloads/unzip7")));
	}
}
