LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_PACKAGE_NAME := PlatformAdapter

LOCAL_CERTIFICATE := platform

LOCAL_STATIC_JAVA_LIBRARIES := platformadapter3.16 \
    com.hwatong.btphone \
    com.hwatong.radio \
    com.hwatong.ipod \
    com.hwatong.systemui \
    com.hwatong.bt \
    com.hwatong.platformadapter \
    com.hwatong.music \
    com.hwatong.media
LOCAL_PROGUARD_ENABLED := disabled
include $(BUILD_PACKAGE)

include $(CLEAR_VARS)

LOCAL_SRC_FILES := \
  src/com/hwatong/platformadapter/thirdparty/IService.aidl \
  src/com/hwatong/platformadapter/thirdparty/CallBack.aidl

LOCAL_MODULE := com.hwatong.platformadapter

include $(BUILD_STATIC_JAVA_LIBRARY)

include $(call all-makefiles-under,$(LOCAL_PATH))
