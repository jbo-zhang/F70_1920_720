LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-java-files-under, src) 

LOCAL_PACKAGE_NAME := BtMusicService

LOCAL_CERTIFICATE := platform

LOCAL_STATIC_JAVA_LIBRARIES := pinyin4j com.hwatong.bt com.hwatong.btmusic nForeAPI com.nforetek.bt.aidl

LOCAL_PROGUARD_FLAG_FILES := proguard.flags

include $(BUILD_PACKAGE)

include $(CLEAR_VARS)

LOCAL_SRC_FILES := \
  src/com/hwatong/btmusic/ICallback.aidl \
  src/com/hwatong/btmusic/IService.aidl \
  src/com/hwatong/btmusic/NowPlaying.java

LOCAL_MODULE := com.hwatong.btmusic

include $(BUILD_STATIC_JAVA_LIBRARY)
