#include "native-lib.h"

jstring Java_com_gotop_sanqinapp_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    av_register_all();
    return env->NewStringUTF(hello.c_str());
}
