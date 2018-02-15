#include "common.h"
#include <android/bitmap.h>


jobject BitmapLock( JNIEnv* env, jobject thiz, jobject pBitmap, void** bmpBuffer )
{
	jobject pBitmapRef = (*env)->NewGlobalRef(env, pBitmap); //lock the bitmap preventing the garbage collector from destructing it

	if (pBitmapRef == NULL)
	{
		*bmpBuffer = NULL;
		return NULL;
	}

	int result = AndroidBitmap_lockPixels(env, pBitmapRef, bmpBuffer);
	if (result != 0)
	{
		*bmpBuffer = NULL;
		return NULL;
	}

	return pBitmapRef;
}

void BitmapUnlock( JNIEnv* env, jobject thiz, jobject pBitmapRef, void* bmpBuffer )
{
	if (pBitmapRef)
	{
		if (bmpBuffer)
		{
			AndroidBitmap_unlockPixels(env, pBitmapRef);
			bmpBuffer = NULL;
		}
		(*env)->DeleteGlobalRef(env, pBitmapRef);
		pBitmapRef = NULL;
	}
}

JNIEXPORT jintArray JNICALL Java_com_websoft_vantium_mobilescanner_manage_ImageCrop_nativeSetPrevBitmap(JNIEnv* env, jobject thiz, jstring imgPath)
{
	jintArray vInfo = (*env)->NewIntArray(env, 5);

	char* path = (*env)->GetStringUTFChars(env, imgPath, 0);

	int narr[5];
	setPrevImage(path, narr);

	(*env)->SetIntArrayRegion(env, vInfo, 0, 5, narr);
	(*env)->ReleaseIntArrayElements(env, vInfo, narr, 0);

	(*env)->ReleaseStringUTFChars(env, imgPath, path);

	return vInfo;
}


JNIEXPORT jint JNICALL Java_com_websoft_vantium_mobilescanner_manage_ImageCrop_nativeSetCropImage(JNIEnv* env, jobject thiz,
						jstring srcPath,
						jstring dstPath,
						int rotate,
						int infalteW, int infalteH,
						int cropx1, int cropy1,
						int cropx2, int cropy2,
						int cropx3, int cropy3,
						int cropx4, int cropy4){

	const char* src = (*env)->GetStringUTFChars(env, srcPath, 0);
	const char* dst = (*env)->GetStringUTFChars(env, dstPath, 0);

	int ret = cropImage(env,
				src, dst,
				rotate,
				infalteW, infalteH,
				cropx1, cropy1,
				cropx2, cropy2,
				cropx3, cropy3,
				cropx4, cropy4);

	(*env)->ReleaseStringUTFChars(env, srcPath, src);
	(*env)->ReleaseStringUTFChars(env, dstPath, dst);

	return ret;
}

JNIEXPORT jint JNICALL Java_com_websoft_vantium_mobilescanner_manage_ImageCrop_nativeBatchWork(JNIEnv* env, jobject thiz,
						jstring srcPath,
						jstring dstPath,
						int mode,
						int rotate,
						int infalteW, int infalteH,
						int cropx1, int cropy1,
						int cropx2, int cropy2,
						int cropx3, int cropy3,
						int cropx4, int cropy4){

	const char* src = (*env)->GetStringUTFChars(env, srcPath, 0);
	const char* dst = (*env)->GetStringUTFChars(env, dstPath, 0);

	int ret = doBatchWork(env,
				src, dst,
				mode,
				rotate,
				infalteW, infalteH,
				cropx1, cropy1,
				cropx2, cropy2,
				cropx3, cropy3,
				cropx4, cropy4);

	(*env)->ReleaseStringUTFChars(env, srcPath, src);
	(*env)->ReleaseStringUTFChars(env, dstPath, dst);

	return ret;
}

JNIEXPORT jint JNICALL Java_com_websoft_vantium_mobilescanner_manage_ImageCrop_nativeSetEditBitmap(JNIEnv* env, jobject thiz,
						jstring bmpPath){

	char* path = (*env)->GetStringUTFChars(env, bmpPath, 0);

	int ret = setEditBitmap(path);

	(*env)->ReleaseStringUTFChars(env, bmpPath, path);

	return ret;
}


JNIEXPORT jint JNICALL Java_com_websoft_vantium_mobilescanner_manage_ImageCrop_nativeSetEditMode(JNIEnv* env, jobject thiz,
						jint mode, jstring savePath){

	char* path = (*env)->GetStringUTFChars(env, savePath, 0);

	int ret = setEditMode(mode, path);

	(*env)->ReleaseStringUTFChars(env, savePath, path);

	return ret;
}


JNIEXPORT void JNICALL Java_com_websoft_vantium_mobilescanner_manage_ImageCrop_nativeAdjustContrastBrightness(JNIEnv* env, jobject thiz,
					jobject bmpSrc, jobject bmpDst, int width, int height,
					int contrast, int brightness, int detail){


	void* srcBuffer;
	jobject srcRef = BitmapLock(env, thiz, bmpSrc, &srcBuffer);

	void* dstBuffer;
	jobject dstRef = BitmapLock(env, thiz, bmpDst, &dstBuffer);


	adjustContrastBright((unsigned short*)srcBuffer, (unsigned short*)dstBuffer, width, height, contrast, brightness, detail);

	BitmapUnlock(env, thiz, dstRef, dstBuffer);
	BitmapUnlock(env, thiz, srcRef, srcBuffer);
}

