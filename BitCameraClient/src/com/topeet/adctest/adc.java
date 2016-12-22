package com.topeet.adctest;

public class adc {
	public native int       Open();
    public native int       Close();
    public native int       Ioctl(int num, long en);
    public native int[]     Read();
}
