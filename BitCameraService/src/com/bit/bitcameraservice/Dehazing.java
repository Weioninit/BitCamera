package com.bit.bitcameraservice;

public class Dehazing {
	static {
        System.loadLibrary("OpenCV");
    }
    public static native int[] OutPut(int[] buf,float f, int w, int h, int flag);  //�������飬������� һ�ε���

}
