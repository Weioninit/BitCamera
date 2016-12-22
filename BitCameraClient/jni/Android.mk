
LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
LOCAL_MODULE    := adc
LOCAL_SRC_FILES := com_topeet_adctest_adc.c
LOCAL_LDLIBS += -llog 
LOCAL_LDLIBS +=-lm

include $(BUILD_SHARED_LIBRARY)