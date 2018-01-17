
LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_JAVA_LIBRARIES := bouncycastle telephony-common
LOCAL_STATIC_JAVA_LIBRARIES := libarity android-support-v4

LOCAL_SRC_FILES := $(call all-java-files-under, src)
  
LOCAL_PACKAGE_NAME := sysupdate-f70

LOCAL_CERTIFICATE := platform

LOCAL_PROGUARD_ENABLED := full

include $(BUILD_PACKAGE)

include $(call all-makefiles-under,$(LOCAL_PATH))

