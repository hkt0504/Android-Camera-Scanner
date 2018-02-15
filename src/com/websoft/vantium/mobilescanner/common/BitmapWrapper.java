package com.websoft.vantium.mobilescanner.common;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.util.Log;

public class BitmapWrapper {

	public static Bitmap createBitmap(Bitmap src) {
		return checkNew(Bitmap.createBitmap(src), src);
	}

	public static Bitmap createScaledBitmap(Bitmap src, int dstWidth, int dstHeight, boolean filter) {
		return checkNew(Bitmap.createScaledBitmap(src, dstWidth, dstHeight, filter), src);
	}

	public static Bitmap createBitmap(Bitmap source, int x, int y, int width, int height) {
		return checkNew(Bitmap.createBitmap(source, x, y, width, height), source);
	}

	private static Bitmap checkNew(Bitmap bitmap, Bitmap src) {
		return (bitmap != src) ? bitmap : src.copy(src.getConfig(), true);
	}

	public static Bitmap createBitmap(int width, int height, Config config) {
		return Bitmap.createBitmap(width, height, config);
	}

	public static Bitmap createBitmap(int colors[], int width, int height, Config config) {
		return Bitmap.createBitmap(colors, width, height, config);
	}

	public static Bitmap createBitmap(Bitmap source, int x, int y, int width, int height, Matrix m, boolean filter) {
		return Bitmap.createBitmap(source, x, y, width, height, m, filter);
	}

	public static void recycleBitmap(Bitmap bmp) {
		if (bmp != null) {
			if (!bmp.isRecycled())
				bmp.recycle();
		}
		bmp = null;
	}

	public static Bitmap bitmapFromBuffer(byte[] pBuffer, int dwBufSize, int nSampleSize) {
		Bitmap pResult = null;

		if (pBuffer == null)
			return pResult;

		BitmapFactory.Options opt = new BitmapFactory.Options();
		opt.inSampleSize = nSampleSize;

		pResult = BitmapFactory.decodeByteArray(pBuffer, 0, dwBufSize);
		opt = null;

		return pResult;
	}

	public static Bitmap BitmapFromSize(int nWidth, int nHeight, Config config) {
		return createBitmap(nWidth, nHeight, Bitmap.Config.ARGB_8888);
	}

	public static Bitmap BitmapFromFile(String strFileName) {
		return BitmapFactory.decodeFile(strFileName);
	}

	public static void setBitmapData(Bitmap src, Bitmap dest) {
		Canvas canvas = new Canvas(dest);
		if ((src!=null) && (dest!=null))
			canvas.drawBitmap(src, new Rect(0, 0, src.getWidth(), src.getHeight()), new Rect(0, 0, dest.getWidth(), dest.getHeight()), null);
	}

	public static void saveBitmapToSdcard(Bitmap bitmap, String fileName) {
		try {
			File bitmapFile = new File(fileName);
			FileOutputStream bitmapWtriter = new FileOutputStream(bitmapFile);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bitmapWtriter);
		} catch (FileNotFoundException e) {
		}
	}

	public static boolean saveBitmapToSdcard(Bitmap bitmap, String fileName, boolean jpgOrPng) {
		try {
			File bitmapFile = new File(fileName);
			FileOutputStream bitmapWtriter = new FileOutputStream(bitmapFile);
			bitmap.compress(jpgOrPng ? Bitmap.CompressFormat.JPEG : Bitmap.CompressFormat.PNG, 90, bitmapWtriter);
		} catch (FileNotFoundException e) {
			Log.e("BitmapWrapper", "err", e);
			return false;
		}
		return true;
	}

	public static int getPhotoOrientation(String imagePath){

		int rotate = 0;
		try {
			File imageFile = new File(imagePath);
			ExifInterface exif = new ExifInterface(
					imageFile.getAbsolutePath());
			int orientation = exif.getAttributeInt(
					ExifInterface.TAG_ORIENTATION,
					ExifInterface.ORIENTATION_NORMAL);

			switch (orientation) {
			case ExifInterface.ORIENTATION_ROTATE_270:
				rotate = 270;
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				rotate = 180;
				break;
			case ExifInterface.ORIENTATION_ROTATE_90:
				rotate = 90;
				break;
			}

		} catch (Exception e) {
		}
		return rotate;
	}

	public static Bitmap RotateBitmap(Bitmap source, int angle)
	{
		Matrix matrix = new Matrix();
		matrix.postRotate(angle);
		return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
	}

	public static Bitmap scaleBitmap(Bitmap bitmap, int MAXSIZE){

		int w = bitmap.getWidth();
		int h = bitmap.getHeight();

		if (w <= MAXSIZE && h <= MAXSIZE)
			return bitmap;

		float rate = (float)w/(float)h;
		int w2, h2;

		if (w > MAXSIZE){
			w2 = MAXSIZE;
			h2 = (int)(w2 / rate);
		}else {
			h2 = MAXSIZE;
			w2 = (int)(h2 * rate);	
		}

		Bitmap bmp2 = createScaledBitmap(bitmap, w2, h2, false);

		bitmap.recycle();
		bitmap = null;

		return bmp2;
	}

	public static Point getBitmapSize(String path){

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, options);
		int width = options.outWidth;
		int height = options.outHeight;

		return new Point(width, height);
	}

	public static Bitmap readScaledBitmap565(String path, int MAX){

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, options);
		int width = options.outWidth;
		int height = options.outHeight;

		if (width<=MAX && height<=MAX){

			options.inJustDecodeBounds = false;
			options.inDither = true;
			options.inPreferredConfig = Bitmap.Config.RGB_565;

			return BitmapFactory.decodeFile(path, options);
		}

		int w2, h2;
		float rate = (float)width/(float)height;

		if (width > MAX){
			w2 = MAX;
			h2 = (int)(w2 / rate);
		}else{
			h2 = MAX;
			w2 = (int)(h2 * rate);
		}

		options.inJustDecodeBounds = false;
		options.inDither = true;
		options.inPreferredConfig = Bitmap.Config.RGB_565;

		Bitmap bmp = BitmapFactory.decodeFile(path, options);
		Bitmap bitmap = Bitmap.createScaledBitmap(bmp, w2, h2, true);
		bmp.recycle();

		return bitmap;
	}

	public static Bitmap getThumb(String path, int w, int h){
		return ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(path), w, h);
	}

	public static Bitmap getUserViewThumb(String path, int w, int h){
	
		int rotate = getPhotoOrientation(path);
		Bitmap bmp = getThumb(path, w, h);
		
		if (rotate == 0)
			return bmp;
		
		Bitmap rotatedBmp = RotateBitmap(bmp, rotate);
		bmp.recycle();
		
		return rotatedBmp;
	}

	public static boolean rotateSavedImage(String path, int degree){

		Bitmap bitmap = readScaledBitmap565(path, Common.SAVE_IMGSIZE);
		Bitmap rotatedBitmap = RotateBitmap(bitmap, degree);
		bitmap.recycle();

		saveBitmapToSdcard(rotatedBitmap, path);
		rotatedBitmap.recycle();
		
		int rotation = getPhotoOrientation(path);

		return true;
	}
}
