#ifndef __COMMON_H__
#define __COMMON_H__

#ifdef __cplusplus
extern "C" {
#endif

#include <jni.h>
#include <android/log.h>
#include <errno.h>

#include <stdio.h>
#include <stdlib.h>
#include <math.h>


#define  LOG_TAG  "OCV:scanner"

#define  LOGI(...)  //__android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

#define MODE_ORIGIN 0
#define MODE_AUTO 	1
#define MODE_MAGIC 	2
#define MODE_GRAY 	3
#define MODE_BW 	4
#define MODE_LIGHT	5



void	setPrevImage(char* path, int* ppos);


int 	cropImage(JNIEnv *env,
				char* srcPath,
				char* dstPath,
				int rotate,
				int inflateW, int inflateH,
				int x1, int y1, int x2, int y2, int x3, int y3, int x4, int y4);


int 	doBatchWork(JNIEnv *env,
				char* srcPath,
				char* dstPath,
				int mode,
				int angle,
				int inflateW, int inflateH,
				int x1, int y1, int x2, int y2, int x3, int y3, int x4, int y4);

int 	setEditBitmap(char* path);

int		setEditMode(int mode, char* savePath);

void 	adjustContrastBright(unsigned short* src, unsigned short* dst, int w, int h, int contrast, int brightness, int detail);


#ifdef __cplusplus
};
#endif

#endif
