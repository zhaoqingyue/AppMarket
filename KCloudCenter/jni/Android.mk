LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

#ģ��ͷ�ļ�Ŀ¼������ʹ�á�������·�� (�����ʵ���������)
MODULE_INCS :=$(LOCAL_PATH)/AndRoid/cavne60_osal/include/hmi_android_include \
	$(LOCAL_PATH)/include

	

#ģ��Դ�ļ�������ʹ�á���ԡ�·���������LOCAL_PATH (�����ʵ���������)
MODULE_SRCS :=src/hello.c

	
	
	
LOCAL_MODULE    := filetest_jni
LOCAL_SRC_FILES := $(MODULE_SRCS)
LOCAL_LDLIBS    := -llog

include $(BUILD_SHARED_LIBRARY)
