//
// Created by liquanzhan on 18-12-13.
//

#ifndef SANQINAPP_NATIVE_LIB_H
#define SANQINAPP_NATIVE_LIB_H

#include <jni.h>
#include <string>
#include <android/log.h>

extern "C" {
//编码
#include "libavcodec/avcodec.h"
//封装格式处理
#include "libavformat/avformat.h"
//像素处理
#include "libswscale/swscale.h"
#include <android/native_window_jni.h>
#include <unistd.h>

#define LOGI(FORMAT, ...) __android_log_print(ANDROID_LOG_INFO,"wayne",FORMAT,##__VA_ARGS__);
#define LOGE(FORMAT, ...) __android_log_print(ANDROID_LOG_ERROR,"wayne",FORMAT,##__VA_ARGS__);


JNIEXPORT jstring JNICALL Java_com_gotop_sanqinapp_MainActivity_stringFromJNI(JNIEnv *env, jobject /* this */);
}


#endif //SANQINAPP_NATIVE_LIB_H
