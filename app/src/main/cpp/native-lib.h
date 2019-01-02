//
// Created by liquanzhan on 18-12-13.
//

#ifndef SANQINAPP_NATIVE_LIB_H
#define SANQINAPP_NATIVE_LIB_H

#include <jni.h>
#include <string>
#include <android/log.h>
#include <android/native_window_jni.h>
#include <unistd.h>
//#include <iostream>

//定义日志宏变量
#define logw(content)   __android_log_write(ANDROID_LOG_WARN,"eric",content)
#define loge(content)   __android_log_write(ANDROID_LOG_ERROR,"eric",content)
#define logd(content)   __android_log_write(ANDROID_LOG_DEBUG,"eric",content)


#define LOGI(FORMAT, ...) __android_log_print(ANDROID_LOG_INFO,"wayne",FORMAT,##__VA_ARGS__);
#define LOGE(FORMAT, ...) __android_log_print(ANDROID_LOG_ERROR,"wayne",FORMAT,##__VA_ARGS__);

extern "C" {
//编码
#include "libavcodec/avcodec.h"
//封装格式处理
#include "libavformat/avformat.h"
//像素处理
#include "libswscale/swscale.h"

//引入时间
#include "libavutil/time.h"

#include "libavutil/imgutils.h"

JNIEXPORT jstring JNICALL Java_com_gotop_sanqinapp_MainActivity_stringFromJNI(JNIEnv *env, jobject /* this */);

/*
 * Class:     com_gotop_sanqinapp_FFmpegHandle
 * Method:    setCallback
 * Signature: (Lcom/gotop/sanqinapp/PushCallback;)I
 */
JNIEXPORT jint JNICALL Java_com_gotop_sanqinapp_FFmpegHandle_setCallback
        (JNIEnv *, jobject, jobject);

/*
 * Class:     com_gotop_sanqinapp_FFmpegHandle
 * Method:    getAvcodecConfiguration
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_gotop_sanqinapp_FFmpegHandle_getAvcodecConfiguration
        (JNIEnv *, jobject);

/*
 * Class:     com_gotop_sanqinapp_FFmpegHandle
 * Method:    pushRtmpFile
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_com_gotop_sanqinapp_FFmpegHandle_pushRtmpFile
        (JNIEnv *, jobject, jstring);

/*
 * Class:     com_gotop_sanqinapp_FFmpegHandle
 * Method:    initVideo
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_com_gotop_sanqinapp_FFmpegHandle_initVideo
        (JNIEnv *, jobject, jstring);

/*
 * Class:     com_gotop_sanqinapp_FFmpegHandle
 * Method:    onFrameCallback
 * Signature: ([B)I
 */
JNIEXPORT jint JNICALL Java_com_gotop_sanqinapp_FFmpegHandle_onFrameCallback
        (JNIEnv *, jobject, jbyteArray);

/*
 * Class:     com_gotop_sanqinapp_FFmpegHandle
 * Method:    close
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_gotop_sanqinapp_FFmpegHandle_close
        (JNIEnv *, jobject);


}
#include <iostream>

#endif //SANQINAPP_NATIVE_LIB_H
