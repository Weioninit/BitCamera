#include <stdio.h>
#include <stdlib.h>
#include <fcntl.h>
#include <errno.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <string.h>
#include <stdint.h>
#include <termios.h>
#include <android/log.h>
#include <sys/ioctl.h>
#include "com_topeet_adctest_adc.h"

#undef  TCSAFLUSH
#define TCSAFLUSH  TCSETSF
#ifndef _TERMIOS_H_
#define _TERMIOS_H_
#endif

int fd=0;

/*
 * Class:     com_topeet_adctest_adc
 * Method:    Open
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_topeet_adctest_adc_Open
  (JNIEnv *env, jobject obj)
  {
	if(fd<=0)fd = open("/dev/adc", O_RDWR|O_NDELAY|O_NOCTTY);
	if(fd <=0 )__android_log_print(ANDROID_LOG_INFO, "serial", "open /dev/adc Error");
	else __android_log_print(ANDROID_LOG_INFO, "serial", "open /dev/adc Sucess fd=%d",fd);
  }

/*
 * Class:     com_topeet_adctest_adc
 * Method:    Close
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_topeet_adctest_adc_Close
  (JNIEnv *env, jobject obj)
  {
	if(fd > 0)close(fd);
  }

/*
 * Class:     com_topeet_adctest_adc
 * Method:    Ioctl
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_topeet_adctest_adc_Ioctl
  (JNIEnv *env, jobject obj, jint num, jint en)
  {
  	ioctl(fd, en, num);
  }

/*
 * Class:     com_topeet_adctest_adc
 * Method:    Read
 * Signature: ()[I
 */
JNIEXPORT jintArray JNICALL Java_com_topeet_adctest_adc_Read
  (JNIEnv *env, jobject obj)
 {
		unsigned char buffer[512];
		int BufToJava[512];
		int len=0, i=0;

		memset(buffer,0,sizeof(buffer));
		memset(BufToJava,0,sizeof(BufToJava));

		len=read(fd,buffer,512);

		if(len<=0)return NULL;

		for(i=0;i<len;i++)
		{
			printf("%x", buffer[i]);
			BufToJava[i] = buffer[i];
		}
		jintArray array = (*env)-> NewIntArray(env, len);
		(*env)->SetIntArrayRegion(env,array, 0, len, BufToJava);

		return array;
  }



