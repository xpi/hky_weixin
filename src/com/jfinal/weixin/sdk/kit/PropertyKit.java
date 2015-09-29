package com.jfinal.weixin.sdk.kit;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Properties;

public class PropertyKit {
	public static String getConfig(String key, String realpath) {
		Properties pro = new Properties();
		try {
			FileInputStream in = new FileInputStream(realpath);
			pro.load(in);
			in.close();

			return pro.get(key).toString();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public static boolean setConfig(String key, String value, String realpath) {
		Properties outpro = new Properties();
		Properties inpro = new Properties();
		try {
			FileOutputStream fos = new FileOutputStream(realpath, true);
			FileInputStream fin = new FileInputStream(realpath);
			inpro.load(fin);
			boolean isContain = inpro.containsKey(key);
			if (!isContain) {
				outpro.put(key, value);
				outpro.store(fos, "none");
			}
			fos.close();
			fin.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
		}
	}

	public static void main(String[] args) {
		String path = PropertyKit.class.getClassLoader().getResource("").getPath();
		System.out.println(path);
	}
}
