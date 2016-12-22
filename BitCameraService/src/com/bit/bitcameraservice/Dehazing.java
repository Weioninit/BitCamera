package com.bit.bitcameraservice;

public class Dehazing {
	static {
        System.loadLibrary("OpenCV");
    }
    public static native int[] OutPut(int[] buf,float f, int w, int h, int flag);  //输入数组，输出数组 一次调用

}
