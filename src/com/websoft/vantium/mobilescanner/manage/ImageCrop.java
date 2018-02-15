package com.websoft.vantium.mobilescanner.manage;

import android.graphics.Bitmap;

public class ImageCrop {
	
	// native functions.
	public static native int[] nativeSetPrevBitmap(String imgPath);
	
	public static native int nativeBatchWork(
			String srcPath,
			String dstPath,
			int mode,
			int rotate,
			int inflateW, int inflateH,
			int cropx1, int cropy1, 
			int cropx2, int cropy2, 
			int cropx3, int cropy3, 
			int cropx4, int cropy4);
	
	public static native int nativeSetCropImage(
			String srcPath,
			String dstPath,
			int rotate,
			int inflateW, int inflateH,
			int cropx1, int cropy1, 
			int cropx2, int cropy2, 
			int cropx3, int cropy3, 
			int cropx4, int cropy4);
	
	public static native int nativeSetEditBitmap(String path);
	
	public static native int nativeSetEditMode(int mode, String savePath);
	
	public static native void nativeAdjustContrastBrightness(Bitmap bmpSrc, Bitmap bmpDst, 
				int width, int height, 
				int contrast, int brightness, int detail);
	
	static {
        System.loadLibrary("opencv_java");
        System.loadLibrary("mscanner");
	}
	
		
}