LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE    := ftrwsqandroid
LOCAL_SRC_FILES := libftrwsqandroid.so
include $(PREBUILT_SHARED_LIBRARY) 

include $(CLEAR_VARS)
LOCAL_MODULE    := ftrwsqandroidjni
LOCAL_SRC_FILES := ftrwsqandroidjni.cpp
LOCAL_SHARED_LIBRARIES := ftrwsqandroid
include $(BUILD_SHARED_LIBRARY)
