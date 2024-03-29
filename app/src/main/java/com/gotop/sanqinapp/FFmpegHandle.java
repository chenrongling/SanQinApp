package com.gotop.sanqinapp;

import android.content.Context;

public class FFmpegHandle {
    private static FFmpegHandle mInstance;

    public static void init(Context context) {
        mInstance = new FFmpegHandle();
    }

    public static FFmpegHandle getInstance() {
        if (mInstance == null) {
            throw new RuntimeException("FFmpegHandle must init fist");
        }
        return mInstance;
    }

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("avutil-55");
        System.loadLibrary("swresample-2");
        System.loadLibrary("avcodec-57");
        System.loadLibrary("avformat-57");
        System.loadLibrary("swscale-4");
        System.loadLibrary("avfilter-6");
        System.loadLibrary("avdevice-57");
        System.loadLibrary("postproc-54");
        System.loadLibrary("native-lib");
    }

    public native int setCallback(PushCallback pushCallback);

    public native String getAvcodecConfiguration();

    public native int pushRtmpFile(String path);

    public native int initVideo(String url);

    public native int onFrameCallback(byte[] buffer);

    public native int close();
}