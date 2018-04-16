#define LOG_TAG "AudioSource"
//#define ALOGV(...)   do {} while (0)
//#define ALOGD(...)   do {} while (0)
//#define ALOGI(...)   do {} while (0)
//#define ALOGE(...)   do {} while (0)

#include <utils/Log.h>

#include <media/AudioRecord.h>
//#include <media/stagefright/AudioSource.h>
//#include <media/stagefright/MediaBuffer.h>
//#include <media/stagefright/MediaDefs.h>
//#include <media/stagefright/MetaData.h>
//#include <media/stagefright/foundation/ADebug.h>
//#include <media/stagefright/foundation/ALooper.h>
#include <cutils/properties.h>
#include <stdlib.h>
#include <tinyalsa/asoundlib.h>

using namespace android;

#include "AudioSource.h"

class AudioSourceImpl : public AudioSource {
public:
	virtual void close();
    virtual status_t start();
    virtual status_t stop() { return reset(); }

    virtual int read(unsigned char *buf, int size);

private:
	friend AudioSource *makeAudioSource(uint32_t sampleRate, uint32_t channelCount);

    // Note that the "channels" parameter _is_ the number of channels,
    // _not_ a bitmask of audio_channels_t constants.
    AudioSourceImpl();

	bool init(int card, int port, int rate, int channels);

    virtual ~AudioSourceImpl();

private:
    Mutex mLock;

	struct pcm *pcm;

    status_t reset();
};

static struct AudioSource *g_info;

static void AudioRecordCallbackFunction(int event, void *user, void *info)
{
    AudioSourceImpl *source = (AudioSourceImpl *) user;

    switch (event) {
    case AudioRecord::EVENT_MORE_DATA: {
        // set size to 0 to signal we're not using the callback to read more data
        AudioRecord::Buffer* pBuff = (AudioRecord::Buffer*)info;
        ALOGI("AudioRecord report data %d", pBuff->size);
        pBuff->size = 0;
        break;
    }
    case AudioRecord::EVENT_OVERRUN: {
        ALOGW("AudioRecord reported overrun!");
        break;
    }
    default:
        // does nothing
        break;
    }
}

AudioSourceImpl::AudioSourceImpl()
    : pcm(NULL) {
}

bool AudioSourceImpl::init(int card, int port, int rate, int channels)
{
    ALOGI("card: %d, rate: %d, channels %d", card, rate, channels);

	struct pcm_config config;

    config.channels = 2;
    config.rate = 44100;
    config.period_size = 512;
    config.period_count = 8;
    config.format = PCM_FORMAT_S16_LE;
    config.start_threshold = 0;
    config.avail_min = 0;

    config.stop_threshold = config.period_size * config.period_count;

    if (channels == 2) {
        config.channels = 2;
    } else if (channels == 1) {
        config.channels = 1;
    } else {
        ALOGW("can not get channels for in_device %d ", AUDIO_DEVICE_IN_USB_DEVICE);
        return false;
    }

    if (rate == 0) {
        ALOGW("can not get rate for in_device %d ", AUDIO_DEVICE_IN_USB_DEVICE);
        return false;
    }
    config.rate = rate;

    struct pcm *pcm = pcm_open(card, port, PCM_IN, &config);

    if (!pcm_is_ready(pcm)) {
        ALOGE("cannot open pcm_in driver: %s", pcm_get_error(pcm));
        pcm_close(pcm);

        struct mixer *mixer = mixer_open(card);
        mixer_close(mixer);

        return false;
    }

    this->pcm = pcm;

    return true;
}

AudioSourceImpl::~AudioSourceImpl()
{
    if (this->pcm) {
        pcm_close(this->pcm);
        this->pcm = NULL;
    }
}

void AudioSourceImpl::close()
{
	delete this;
}

status_t AudioSourceImpl::start() {
    Mutex::Autolock autoLock(mLock);

    return OK;
}

status_t AudioSourceImpl::reset()
{
    Mutex::Autolock autoLock(mLock);

    return OK;
}

int AudioSourceImpl::read(unsigned char *buf, int size)
{
    Mutex::Autolock autoLock(mLock);

    int ret = pcm_read(this->pcm, buf, size);

    if (ret != 0) {
        ALOGW("ret %d, pcm read %d error %s.", ret, size, pcm_get_error(this->pcm));

        switch (pcm_state(this->pcm)) {
        case PCM_STATE_SETUP:
        case PCM_STATE_XRUN:
            ret = pcm_prepare(this->pcm);
            if (ret == 0)
        		ret = pcm_read(this->pcm, buf, size);
            break;

        default:
            break;
        }
    }

	if (ret != 0)
		return 0;

    return size;
}

AudioSource *makeAudioSource(uint32_t sampleRate, uint32_t channelCount)
{
    int card, in_rate = 0, in_channels = 0, in_format = 0;
	int i;
	
    for (i = 0; i < 16; i++) {
        struct control *imx_control = control_open(i);

        if (imx_control) {
        	const char *driver_name = control_card_info_get_driver(imx_control);

            if (strstr(driver_name, "tef6638-audio") != NULL) {
                card = i;

                int rate = sampleRate;
                if (pcm_get_near_param(i, 0, PCM_IN, PCM_HW_PARAM_RATE, &rate) == 0)
                    in_rate = rate;

                int channels = channelCount;
                if (pcm_get_near_param(i, 0, PCM_IN, PCM_HW_PARAM_CHANNELS, &channels) == 0)
                    in_channels = channels;

                int format = PCM_FORMAT_S16_LE;
                if (pcm_check_param_mask(i, 0, PCM_IN, PCM_HW_PARAM_FORMAT, format)) {
                    in_format = format;
                } else {
                    format = PCM_FORMAT_S24_LE;
                    if (pcm_check_param_mask(i, 0, PCM_IN, PCM_HW_PARAM_FORMAT, format))
                        in_format = format;
                }

                ALOGW("card %d, rate %d, channels %d format %d", card, in_rate, in_channels, in_format);

    			AudioSourceImpl *impl = new AudioSourceImpl();

				if (!impl) {
                    control_close(imx_control);
					return NULL;
                }

				if (!impl->init(card, 0, in_rate, in_channels)) {
                    control_close(imx_control);
					delete impl;
					return NULL;
				}

				return impl;
            }
            control_close(imx_control);
        }
    }

    return NULL;
}

