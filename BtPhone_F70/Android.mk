LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_PACKAGE_NAME := BtPhone_F70

LOCAL_CERTIFICATE := platform

LOCAL_STATIC_JAVA_LIBRARIES := pinyin4j \
	android-support-v4 \
	com.hwatong.systemui \
	com.hwatong.btphone \
	com.tbox.service
	

LOCAL_PROGUARD_FLAG_FILES := proguard.flags

include $(BUILD_PACKAGE)