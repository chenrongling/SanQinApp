package com.gotop.sanqinapp;

public interface PushCallback {
    public void videoCallback(long pts, long dts, long duration, long index);
}
