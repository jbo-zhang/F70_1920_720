LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-java-files-under, src) 

LOCAL_PACKAGE_NAME := BtService

LOCAL_CERTIFICATE := platform

LOCAL_STATIC_JAVA_LIBRARIES := pinyin4j nForeAPI com.hwatong.bt com.nforetek.bt.aidl

LOCAL_PROGUARD_FLAG_FILES := proguard.flags

include $(BUILD_PACKAGE)

include $(CLEAR_VARS)
LOCAL_STATIC_JAVA_LIBRARIES := nForeAPI
LOCAL_SRC_FILES := \
  src/com/hwatong/bt/ICallback.aidl \
  src/com/hwatong/bt/IService.aidl \
  src/com/hwatong/bt/BtDevice.java \
  src/com/hwatong/bt/BtDef.java

LOCAL_MODULE := com.hwatong.bt

include $(BUILD_STATIC_JAVA_LIBRARY)
