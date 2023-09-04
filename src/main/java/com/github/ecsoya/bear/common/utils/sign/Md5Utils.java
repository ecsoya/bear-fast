package com.github.ecsoya.bear.common.utils.sign;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Md5加密方法
 * 
 * @author angryred
 */
public class Md5Utils {
	private static final Logger log = LoggerFactory.getLogger(Md5Utils.class);

	private static byte[] md5(String s) {
		MessageDigest algorithm;
		try {
			algorithm = MessageDigest.getInstance("MD5");
			algorithm.reset();
			algorithm.update(s.getBytes("UTF-8"));
			byte[] messageDigest = algorithm.digest();
			return messageDigest;
		} catch (Exception e) {
			log.error("MD5 Error...", e);
		}
		return null;
	}

	private static final String toHex(byte hash[]) {
		if (hash == null) {
			return null;
		}
		StringBuffer buf = new StringBuffer(hash.length * 2);
		int i;

		for (i = 0; i < hash.length; i++) {
			if ((hash[i] & 0xff) < 0x10) {
				buf.append("0");
			}
			buf.append(Long.toString(hash[i] & 0xff, 16));
		}
		return buf.toString();
	}

	public static String hash(String s) {
		try {
			return new String(toHex(md5(s)).getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
		} catch (Exception e) {
			log.error("not supported charset...{}", e);
			return s;
		}
	}

	public static String fileMd5(File file) {
		if (file == null || !file.exists()) {
			return null;
		}
		try (FileInputStream in = new FileInputStream(file)) {
			return DigestUtils.md2Hex(in);
		} catch (Exception e) {
			log.error("read file md5 failed", e);
			return null;
		}
	}

	public static String fileMd5(InputStream in) {
		if (in == null) {
			return null;
		}
		try {
			return DigestUtils.md2Hex(in);
		} catch (IOException e) {
			log.error("read file md5 failed", e);
			return null;
		} finally {
			IOUtils.closeQuietly(in);
		}
	}
}
