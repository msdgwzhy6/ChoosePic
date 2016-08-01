package com.openxu.choosepic.util;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

public class FileUtils {
	private static String TAG= "FileUtils";

	/**
	 *
	  * copyFiletoFile
	 * description:循环将文件拷入
	  * @param addrs
	  *void
	  * @exception
	  * @since  1.0.0
	 */
	public static void copyFiletoFile(ArrayList<String> addrs, String path) {
		if(!addrs.isEmpty()) {
			int len = addrs.size();
			for(int i=0; i<len ;i++) {
				String oldpath = addrs.get(i);
				String newpath = path+"/"+System.currentTimeMillis()+".png";
				copyFile(oldpath, newpath);
			}
		}
	}
	/**
	 * 复制单个文件
	 * @param oldPath String 原文件路径 如：c:/fqf.txt
	 * @param newPath String 复制后路径 如：f:/fqf.txt
	 * @return boolean
	 */
	public static void copyFile(String oldPath, String newPath) {
		try {
			int bytesum = 0;
			int byteread = 0;
			File oldfile = new File(oldPath);
			if (oldfile.exists()) { //文件存在时
				InputStream inStream = new FileInputStream(oldPath); //读入原文件
				FileOutputStream fs = new FileOutputStream(newPath);
				byte[] buffer = new byte[1444];
				int length;
				while ( (byteread = inStream.read(buffer)) != -1) {
					bytesum += byteread; //字节数 文件大小
					fs.write(buffer, 0, byteread);
				}
				inStream.close();
			}
		}
		catch (Exception e) {
			Log.e(TAG, "复制单个文件操作出错");
			e.printStackTrace();

		}

	}
	public static int getlistSize(String path) {// 递归求取目录文件个数
		int size = 0;
		File file = new File(path);
		File flist[] = file.listFiles();
		size = flist.length;
		for (int i = 0; i < flist.length; i++) {
			if (flist[i].isDirectory()) {
				size = size + getlistSize(flist[i].getAbsolutePath());
				size--;
			}
		}
		return size;
	}

	public static ArrayList<String> getFileList(String path) {// 递归求取目录文件个数
		int size = 0;
		File file = new File(path);
		File flist[] = file.listFiles();
		ArrayList<String> files = new ArrayList<String>();
		size = flist.length;
		for (int i = 0; i < flist.length; i++) {
			if (flist[i].isFile()) {
				files.add(flist[i].getAbsolutePath());
			}
		}
		return files;
	}



	/**
	 * 获取指定文件大小
	 * @return
	 * @throws Exception
	 */
	public static long getFileSize(File file) throws Exception {
		long size = 0;
		if (file.exists()) {
			FileInputStream fis = null;
			fis = new FileInputStream(file);
			size = fis.available();
		} else {
			file.createNewFile();
		}
		return size;
	}


	// 删除指定文件夹下所有文件
	// param path 文件夹完整绝对路径
	public static boolean delAllFile(String path) {
		boolean flag = false;
		File file = new File(path);
		if (!file.exists()) {
			return flag;
		}
		if (!file.isDirectory()) {
			return flag;
		}
		String[] tempList = file.list();
		File temp = null;
		for (int i = 0; i < tempList.length; i++) {
			if (path.endsWith(File.separator)) {
				temp = new File(path + tempList[i]);
			} else {
				temp = new File(path + File.separator + tempList[i]);
			}
			if (temp.isFile()) {
				temp.delete();
			}else if (temp.isDirectory()){
				delAllFile(temp.getAbsolutePath());
			}
		}
		return true;
	}






}
