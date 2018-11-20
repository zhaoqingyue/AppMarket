LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

#模块头文件目录，必须使用“完整”路径 (需根据实际情况更改)
MODULE_INCS :=$(LOCAL_PATH)/AndRoid/cavne60_osal/include/hmi_android_include \
	$(LOCAL_PATH)/include

	

#模块源文件，必须使用“相对”路径，相对于LOCAL_PATH (需根据实际情况更改)
MODULE_SRCS :=src/hello.c

	
	
	
LOCAL_MODULE    := filetest_jni
LOCAL_SRC_FILES := $(MODULE_SRCS)
LOCAL_LDLIBS    := -llog

include $(BUILD_SHARED_LIBRARY)
