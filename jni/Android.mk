LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

#OPENCV_INSTALL_MODULES=on
#OPENCV_LIB_TYPE=STATIC

OPENCV_PATH := D:\job\tasks\finished\CamScan\project\source\OpenCV-2.4.9-android-sdk

LOCAL_MODULE := libopencv_java
LOCAL_SRC_FILES := $(OPENCV_PATH)/sdk/native/libs/armeabi-v7a/libopencv_java.so
include $(PREBUILT_SHARED_LIBRARY)


include $(CLEAR_VARS)
include $(OPENCV_PATH)/sdk/native/jni/OpenCV.mk

LOCAL_MODULE    := mscanner
LOCAL_SRC_FILES := mscanner.cpp \
					interface.c \
					imgprocess.c


LOCAL_LDLIBS    += -lm -llog -landroid -ljnigraphics


include $(BUILD_SHARED_LIBRARY)

