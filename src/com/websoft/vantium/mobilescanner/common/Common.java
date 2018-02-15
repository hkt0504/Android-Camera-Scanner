package com.websoft.vantium.mobilescanner.common;

public class Common {
	
	// define extra key
	public final static String docID 	= "docID";
	public final static String pageID 	= "pageID";
	public final static String fromCODE = "fromCode";
	public final static String imagePath = "imagePath";
	public final static String startItem = "startItem";
	public final static String bathImage = "batchImage";
	public final static String bathImageInfos = "batchImageInfos";
	
	public final static String resultImg = "resultImg";
	public final static String thumbImg = "thumbImg";
	public final static String cropPath = "cropPath";
	public final static String changedData = "changedData";
	
	
	public static final String EXTRA_MULTIMODE = "multimode";
	public static final String EXTRA_PATHS = "paths";
	
	
	public final static int 	THUMB_IMG_SIZE = 250;
			
	public final static int 	RESULT_ACTVT = 1001;

	// the num of page edit activity
	public final static int 	FROM_DOCLIST = 1;
	public final static int 	FROM_PGLIST = 2;
	public final static int 	FROM_PGVIEW = 3;
	public final static int 	FROM_BATCHVIEWER = 4;
	
	
	public final static int 	CROPVIEW_SIZE = 1020;
	public final static int 	SAVE_IMGSIZE = 1020;
	
	// opencv image process mode
	public final static int MODE_ORIGIN = 0;
	public final static int MODE_AUTO 	= 1;
	public final static int MODE_MAGIC = 2;
	public final static int MODE_GRAY 	= 3;
	public final static int MODE_BW	= 4;
	public final static int MODE_LIGHT	= 5;
	
	public static int 	IMAGE_PROCESS_MODE = MODE_AUTO;
	

	public static int 	SCREEN_WIDTH;
	public static int 	SCREEN_HEIGHT;	
}