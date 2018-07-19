LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_PACKAGE_NAME := BtPhoneService

LOCAL_CERTIFICATE := platform

LOCAL_STATIC_JAVA_LIBRARIES := pinyin4j \
	com.hwatong.bt com.hwatong.btphone nForeAPI com.nforetek.bt.aidl com.hwatong.providers.carsettings

LOCAL_PROGUARD_FLAG_FILES := proguard.flags

include $(BUILD_PACKAGE)

include $(CLEAR_VARS)

LOCAL_SRC_FILES := \
  src/com/hwatong/btphone/ICallback.aidl \
  src/com/hwatong/btphone/IService.aidl \
  src/com/hwatong/btphone/CallStatus.java \
  src/com/hwatong/btphone/Contact.java \
  src/com/hwatong/btphone/CallLog.java

LOCAL_MODULE := com.hwatong.btphone

include $(BUILD_STATIC_JAVA_LIBRARY)
