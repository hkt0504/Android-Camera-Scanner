package com.websoft.vantium.mobilescanner.manage;

import java.io.File;

import android.os.Environment;

public class FileManager {
	
	public static final String DIR_PATH2 = Environment.getExternalStorageDirectory() + File.separator + "vantium";
	public static final String DIR_PATH = DIR_PATH2 + File.separator + "mobilescanner";
	
	
	private static final String THUMB_PATH = DIR_PATH + File.separator + "thumb";
	private static final String IMG_PATH = DIR_PATH + File.separator + "img";
	private static final String WORK_PATH = DIR_PATH + File.separator + "work";
	public static final String PHOTO_PATH = DIR_PATH + File.separator + "photo";
	private static final String EXPORT_PATH = DIR_PATH + File.separator + "export";

	
	public static void init(){
		
		File dir = new File(DIR_PATH2);
		if (! dir.exists()){
			dir.mkdir();
		}
		
		dir = new File(DIR_PATH);
		if (! dir.exists()){
			dir.mkdir();
		}
		
		dir = new File(THUMB_PATH);
		if (! dir.exists()){
			dir.mkdir();
		}
		
		dir = new File(EXPORT_PATH);
		if (! dir.exists()){
			dir.mkdir();
		}
		
		dir = new File(IMG_PATH);
		if (! dir.exists()){
			dir.mkdir();
		}
		
		dir = new File(WORK_PATH);
		if (! dir.exists()){
			dir.mkdir();
		}
	}
	
	public static String getCropFilePath(){
		return WORK_PATH + File.separator + "crop.jpg";
	}
	
	public static String getEditFilePath(){
		return WORK_PATH + File.separator + "edit.jpg";
	}
	
	public static String getThumbFilePath(){
		return THUMB_PATH + File.separator + "thumb_" + System.currentTimeMillis() + ".jpg";
	}
	
	public static String getPageFilePath(){
		return IMG_PATH + File.separator + "img_" + System.currentTimeMillis() + ".jpg";
	}
	
	public static String getPdfFilePath(String name) {
		
		//return PDF_PATH + File.separator + "pdf_" + System.currentTimeMillis() + ".pdf";
		return EXPORT_PATH + File.separator + name + System.currentTimeMillis() + ".pdf";
	}

	public static String getJPGFolderPath(String name) {
		return EXPORT_PATH + File.separator + name + System.currentTimeMillis() + File.separator;
	}

	public static String getPNGFolderPath(String name) {
		return EXPORT_PATH + File.separator + name + System.currentTimeMillis() + File.separator;
	}

	public static boolean deleteFile(String path){
		
		File file = new File(path);
		return file.delete();
	}
	
}
