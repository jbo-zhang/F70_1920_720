/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
#include <string.h>
#include <jni.h>
#include <stdio.h>
#include <fcntl.h>
#include <android/log.h>
#include <stdlib.h>
#include "jni.h"
#include "JNIHelp.h"
#include "android_runtime/AndroidRuntime.h"
#include <tinyalsa/asoundlib.h>
using namespace android;
#include "AudioPlayer.h"

#define LOG_TAG "FMRadioJNI"

//#define ALOGV(...)	__android_log_print(ANDROID_LOG_VERBOSE, LOG_TAG, __VA_ARGS__)
//#define ALOGI(...)	__android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
//#define ALOGW(...)	__android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__)
//#define ALOGE(...)	__android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

#define FILE_TEF6686     "/dev/tef6638_radio0"

#define TEF6686_IOCTL_CMD_TURN_TO_FREQ			0x11
#define TEF6686_IOCTL_CMD_GET_QUALITY_STATUS	0x12
#define TEF6686_IOCTL_CMD_GET_QUALITY_DATA		0x13
#define TEF6686_IOCTL_CMD_SET_BANDWIDTH			0x14
#define TEF6686_IOCTL_CMD_SET_VOLUME			0x15
#define TEF6686_IOCTL_CMD_SET_MUTE				0x16

#define TEF6686_IOCTL_CMD_AM_OFFSET				0x8

#define TEF6686_MODE_PRESET		0x10  //tef6638
#define TEF6686_MODE_SEARCH		0x20  //tef6638

static int s_tef6686_fd = -1;
static jboolean audio_opened = false;

static jint
Java_com_hwatong_radio_RadioService_radioOpen(JNIEnv *env, jobject thiz)
{
    ALOGI("radioOpen()");
	if (s_tef6686_fd > 0)
		return 1;

	s_tef6686_fd = open(FILE_TEF6686, O_RDWR);
    if (s_tef6686_fd < 0)
        return -1;

    return 1;
}

static jint
Java_com_hwatong_radio_RadioService_radioClose(JNIEnv *env, jobject thiz)
{
    ALOGI("radioClose()");
	if (s_tef6686_fd < 0) {
        //jniThrowException(env, "java/io/IOException", "Cannot find fifo");
        return -1;
    }
    close(s_tef6686_fd);
    s_tef6686_fd = -1;
    return 0;
}

static jint
Java_com_hwatong_radio_RadioService_radioTurnToFreq(JNIEnv *env, jobject thiz, jboolean fm, jint frequency)
{
	unsigned int param;
	int ret;

	Java_com_hwatong_radio_RadioService_radioOpen(env, thiz);

	if (s_tef6686_fd < 0)
		return -1;

	param = (TEF6686_MODE_PRESET << 16) | frequency;

	ret = ioctl(s_tef6686_fd, fm ? TEF6686_IOCTL_CMD_TURN_TO_FREQ : TEF6686_IOCTL_CMD_TURN_TO_FREQ + TEF6686_IOCTL_CMD_AM_OFFSET, &param);
	if (ret < 0)
		return -1;
	
	return 0;
}

static jint
Java_com_hwatong_radio_RadioService_radioSetVolume(JNIEnv *env, jobject thiz, jint volume)
{
	unsigned int param;
	int ret;

	Java_com_hwatong_radio_RadioService_radioOpen(env, thiz);

	if (s_tef6686_fd < 0)
		return -1;

	param = (unsigned int)(volume * 10);

	ret = ioctl(s_tef6686_fd, TEF6686_IOCTL_CMD_SET_VOLUME, &param);
	if (ret < 0)
		return -1;
	
	return 0;
}

static jint
Java_com_hwatong_radio_RadioService_radioSetMute(JNIEnv *env, jobject thiz, jboolean muted)
{
	unsigned int param;
	int ret;

	Java_com_hwatong_radio_RadioService_radioOpen(env, thiz);

	if (s_tef6686_fd < 0)
		return -1;

	param = muted ? 1 : 0;

	ret = ioctl(s_tef6686_fd, TEF6686_IOCTL_CMD_SET_MUTE, &param);
	if (ret < 0)
		return -1;
	
	return 0;
}

/*tef6638 */
#define FM_LEVEL		23 //21  //25   //210 //190 	//50 
#define FM_USN			30 //45 //27   //230		//270
#define FM_WAM			30 //45 //23   //270		//230
#define FM_OFFSET		100   //100     //55 

#define AM_LEVEL		35   //240 //210		//350		
#define AM_OFFSET		8	//15

static jint
Java_com_hwatong_radio_RadioService_radioScanFreq(JNIEnv *env, jobject thiz, jboolean fm, jint frequency)
{
	unsigned int param;
	unsigned char buf[14];
	unsigned short status;
	short level;
	unsigned short usn, wam;
	char offset;
	unsigned short bandwidth, modulation;
	int ok = 0;
	int i;
	int j=0;
	int k=0;
	int l=0;
	int ret;

	Java_com_hwatong_radio_RadioService_radioOpen(env, thiz);

	if (s_tef6686_fd < 0)
		return -1;

	if (!fm) {
	    ALOGI("scan %d", frequency);

		param = (TEF6686_MODE_SEARCH << 16) | frequency;

		ret = ioctl(s_tef6686_fd, TEF6686_IOCTL_CMD_TURN_TO_FREQ + TEF6686_IOCTL_CMD_AM_OFFSET, &param);
		if (ret < 0)
			return -1;

		usleep(40000);

		for (i = 0; i < 5; i++) {
			ret = ioctl(s_tef6686_fd, TEF6686_IOCTL_CMD_GET_QUALITY_STATUS + TEF6686_IOCTL_CMD_AM_OFFSET, buf);
			if (ret < 0)
				return -1;

			status = (unsigned short)(buf[0]&0xE0);

			if (status >= 0xA0) {
				ret = ioctl(s_tef6686_fd, TEF6686_IOCTL_CMD_GET_QUALITY_DATA + TEF6686_IOCTL_CMD_AM_OFFSET, buf);
				if (ret < 0)
					return -1;

				level = ((short)buf[1] - 16)/2;

				ALOGI("%d: level  %d   is %sok", frequency, level, level >= AM_LEVEL ? "" : "NOT ");
				if (level < AM_LEVEL)
				return -1;

				offset = (char)buf[4];
				if(offset < 0)
				offset = -offset;

			#if 0
			offset = (short)(unsigned short)(0x7f & buf[4]);
			if (offset < 0)
				offset = -offset;
			#endif

	    	ALOGI("%d: offset %d   is %sok", frequency, offset, offset < AM_OFFSET ? "" : "NOT ");
			if (offset >= AM_OFFSET)
			return -1;

			}

			usleep(10000);
		}

		return 0;
	}
	
	param = (TEF6686_MODE_SEARCH << 16) | frequency;

	ret = ioctl(s_tef6686_fd, TEF6686_IOCTL_CMD_TURN_TO_FREQ, &param);
	if (ret < 0)
		return -1;

	usleep(40000);
	
	
	for (i = 0; i < 5; i++) {
		ret = ioctl(s_tef6686_fd, TEF6686_IOCTL_CMD_GET_QUALITY_STATUS, buf);
		if (ret < 0)
			return -1;

		status = (unsigned short)(buf[0]&0xE0);

		if (status >= 0xA0) {
			ret = ioctl(s_tef6686_fd, TEF6686_IOCTL_CMD_GET_QUALITY_DATA, buf);
			if (ret < 0)
				return -1;

			ALOGI("%d: standard is level=%d usn=%d wam=%d offset=%d", frequency,FM_LEVEL,FM_USN,FM_WAM,FM_OFFSET);
			ALOGI("%d: level=%d usn=%d wam=%d offset=%d bandwidth=%d", frequency,buf[1],buf[2],buf[3],buf[4],buf[5]);


			level = ((short)buf[1] - 16)/2;
			usn = ((short)(100 * (unsigned short)buf[2]) >> 8);
			wam = ((short)(100 * (unsigned short)buf[3]) >> 8);
			
			ALOGI("%d: level=%d usn=%d wam=%d", frequency,level,usn,wam);

	    	ALOGI("%d: level     is %sok", frequency, level >= FM_LEVEL ? "" : "NOT ");
	    	ALOGI("%d: usn       is %sok", frequency, usn < FM_USN ? "" : "NOT ");
	    	ALOGI("%d: wam       is %sok", frequency, wam < FM_WAM ? "" : "NOT ");

			if (level < FM_LEVEL || usn >= FM_USN || wam >= FM_WAM)
				j++;

			if (j>=3) {
				return -1;
			}

			if (i >= 0) {
			offset = 10 * (short)(unsigned short)(0x7f & buf[4]);
			if (offset < 0)
				offset = -offset;

	    		ALOGI("%d: offset    is %sok", frequency, offset < FM_OFFSET ? "" : "NOT ");

				if (offset >= FM_OFFSET)
					k++;

				if (k>=3) {
					return -1;
				}

			bandwidth = (unsigned short)(buf[5]);
//			modulation = (unsigned short)(buf[12] << 8 | buf[13]);

		//	ALOGI("%d: bandwidth is %sok, %d %s 970", frequency, bandwidth >= 970 ? "" : "NOT ", bandwidth, bandwidth >= 970 ? ">=" : "<");
#if 0
			if (bandwidth < 970)
				l++;	
			if(l>=3){
				return -1;
			}
#endif			
		}

			ok = 1;
		}

		usleep(10000);
	}

	return ok ? 0 : -1;
}

static jint
Java_com_hwatong_radio_RadioService_radioStartSeek(JNIEnv*env, jobject thiz, jboolean fm, jint frequency)
{
	unsigned int param;
	int ret;

	Java_com_hwatong_radio_RadioService_radioOpen(env, thiz);

	if (s_tef6686_fd < 0)
		return -1;

	param = (TEF6686_MODE_PRESET << 16) | frequency;

	ret = ioctl(s_tef6686_fd, fm ? TEF6686_IOCTL_CMD_TURN_TO_FREQ : TEF6686_IOCTL_CMD_TURN_TO_FREQ + TEF6686_IOCTL_CMD_AM_OFFSET, &param);
	if (ret < 0)
		return -1;

	param = (1 << 16) | 970;

	ret = ioctl(s_tef6686_fd, fm ? TEF6686_IOCTL_CMD_SET_BANDWIDTH : TEF6686_IOCTL_CMD_SET_BANDWIDTH + TEF6686_IOCTL_CMD_AM_OFFSET, &param);
	if (ret < 0)
		return -1;

	usleep(10000);

	return 0;
}

static jint
Java_com_hwatong_radio_RadioService_radioEndSeek(JNIEnv *env, jobject thiz, jboolean fm, jint frequency)
{
	unsigned int param;
	int ret;

	Java_com_hwatong_radio_RadioService_radioOpen(env, thiz);

	if (s_tef6686_fd < 0)
		return -1;

	param = (TEF6686_MODE_PRESET << 16) | frequency;

	ret = ioctl(s_tef6686_fd, fm ? TEF6686_IOCTL_CMD_TURN_TO_FREQ : TEF6686_IOCTL_CMD_TURN_TO_FREQ + TEF6686_IOCTL_CMD_AM_OFFSET, &param);
	if (ret < 0)
		return -1;

	param = (1 << 16) | 2360;

	ret = ioctl(s_tef6686_fd, fm ? TEF6686_IOCTL_CMD_SET_BANDWIDTH : TEF6686_IOCTL_CMD_SET_BANDWIDTH + TEF6686_IOCTL_CMD_AM_OFFSET, &param);
	if (ret < 0)
		return -1;

	usleep(10000);

	return 0;
}

static jint
Java_com_hwatong_radio_RadioService_radioGetQuality(JNIEnv *env, jobject thiz, jboolean fm, jshortArray arr)
{
	int ret;

	Java_com_hwatong_radio_RadioService_radioOpen(env, thiz);

	if (s_tef6686_fd < 0)
		return -1;

	jshort *pRets = env->GetShortArrayElements(arr, NULL);
	if (NULL == pRets) {
		env->ReleaseShortArrayElements(arr, pRets,0);
		return -1;
	}

	int length = env->GetArrayLength(arr);
	if (length < 7) {
		env->ReleaseShortArrayElements(arr, pRets,0);
		return -1;
	}

	unsigned char buf[14];
	
	short level;
	short offset;
	unsigned short status;
	unsigned short usn, wam, bandwidth, modulation;

	ret = ioctl(s_tef6686_fd, fm ? TEF6686_IOCTL_CMD_GET_QUALITY_DATA : TEF6686_IOCTL_CMD_GET_QUALITY_DATA + TEF6686_IOCTL_CMD_AM_OFFSET, buf);
	if (ret < 0) {
		env->ReleaseShortArrayElements(arr, pRets,0);
		return -1;
	}

	status = (unsigned short)(buf[0] << 8 | buf[1]);
	level = (short)(unsigned short)(buf[2] << 8 | buf[3]);
	usn = (unsigned short)(buf[4] << 8 | buf[5]);
	wam = (unsigned short)(buf[6] << 8 | buf[7]);
	offset = (short)(unsigned short)(buf[8] << 8 | buf[9]);
	bandwidth = (unsigned short)(buf[10] << 8 | buf[11]);
	modulation = (unsigned short)(buf[12] << 8 | buf[13]);

	pRets[0] = level;
	pRets[1] = offset;
	pRets[2] = status;
	pRets[3] = usn;
	pRets[4] = wam;
	pRets[5] = bandwidth;
	pRets[6] = modulation;
	env->ReleaseShortArrayElements(arr, pRets,0);
	return 0;
}

static void
Java_com_hwatong_radio_RadioService_sync(JNIEnv *env, jobject thiz)
{
	//sync();
}

static void
Java_com_hwatong_radio_RadioService_tinymix(JNIEnv *env, jobject thiz, jint id, jint value)
{
    struct mixer *mixer;
    int card = 0;

    mixer = mixer_open(card);
    if (mixer != NULL) {
	    struct mixer_ctl *ctl;
	    unsigned int num_values;
	    unsigned int i;

	    ctl = mixer_get_ctl(mixer, id);
	    num_values = mixer_ctl_get_num_values(ctl);

        for (i = 0; i < num_values; i++) {
            if (mixer_ctl_set_value(ctl, i, value)) {
            	break;
            }
        }

	    mixer_close(mixer);
	}
}

static AudioPlayer *g_player;
void f_write_string(const char *filename, const char *format, ...) {
    va_list args;
    FILE *fp = fopen(filename, "w");
    if(fp) {
        va_start(args, format);
        vfprintf(fp, format, args);
        va_end(args);
        fclose(fp);
    }
}
static jboolean audioOpen(JNIEnv *env, jobject thiz)
{
    ALOGI("audioOpen()");
#if 0
	if (g_player == NULL) {
		AudioPlayer *player = makeAudioPlayer(AUDIO_STREAM_MUSIC, 44100, 2);
		if (player == NULL) {
	    	ALOGE("Failed to makeAudioPlayer");
	    	return false;
		}
		
		player->start();

		g_player = player;
	}
#else
    if(audio_opened == false) {
        audio_opened = true;
        f_write_string("/sys/bus/i2c/devices/0-0063/channel_func", "1");
    }
#endif
    return true;
}

static void audioClose(JNIEnv *env, jobject thiz)
{
    ALOGI("audioClose()");

    if (env->ExceptionOccurred() != NULL) {
        return;
    }
#if 0
	if (g_player) {
		g_player->close();
		g_player = NULL;
	}
#else
    audio_opened = false;
    f_write_string("/sys/bus/i2c/devices/0-0063/channel_func", "2");
	f_write_string("/sys/devices/platform/imx-i2c.0/i2c-0/0-0063/volume_func","26");
#endif
}

static void audioSetVolume(JNIEnv *env, jobject thiz, jfloat leftVol, jfloat rightVol)
{
    ALOGI("audioSetVolume(%f, %f)", leftVol, rightVol);

    if (env->ExceptionOccurred() != NULL) {
        return;
    }

	if (g_player) {
		g_player->setVolume(leftVol, rightVol);
	}
}

static JNINativeMethod method_table[] = {
    { "radioOpen", "()I", (void *)Java_com_hwatong_radio_RadioService_radioOpen },
    { "radioClose", "()I", (void *)Java_com_hwatong_radio_RadioService_radioClose },

    { "radioTurnToFreq", "(ZI)I", (void *)Java_com_hwatong_radio_RadioService_radioTurnToFreq },
    //{ "radioSetVolume", "(I)I", (void *)Java_com_hwatong_radio_RadioService_radioSetVolume },

    { "radioSetMute", "(Z)I", (void *)Java_com_hwatong_radio_RadioService_radioSetMute },
    { "radioScanFreq", "(ZI)I", (void *)Java_com_hwatong_radio_RadioService_radioScanFreq },
    { "radioStartSeek", "(ZI)I", (void *)Java_com_hwatong_radio_RadioService_radioStartSeek },

    { "radioEndSeek", "(ZI)I", (void *)Java_com_hwatong_radio_RadioService_radioEndSeek },
    //{ "radioGetQuality", "(Z[S)I", (void *)Java_com_hwatong_radio_RadioService_radioGetQuality },

    { "sync", "()V", (void *)Java_com_hwatong_radio_RadioService_sync },
    { "tinymix", "(II)V", (void *)Java_com_hwatong_radio_RadioService_tinymix },


    { "audioOpen", "()Z", (void *)audioOpen },
    { "audioClose", "()V", (void *)audioClose },
    { "audioSetVolume", "(FF)V", (void *)audioSetVolume },
};

/* Library init */
extern "C" jint JNI_OnLoad(JavaVM* vm, void* reserved)
{
    JNIEnv *env;
    jclass clazz;

    if (vm->GetEnv((void**) &env, JNI_VERSION_1_4) != JNI_OK) {
        ALOGE("Failed to get the environment using GetEnv()");
        return -1;
    }

    clazz = env->FindClass("com/hwatong/radio/RadioService");
    if (clazz == NULL) {
        ALOGE("Can't find com/hwatong/radio/RadioService");
        return -1;
    }

    jniRegisterNativeMethods(env, "com/hwatong/radio/RadioService",
            method_table, NELEM(method_table));

    return JNI_VERSION_1_4;
}
