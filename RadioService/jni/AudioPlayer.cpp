#define LOG_TAG "AudioPlayer"
//#define ALOGV(...)   do {} while (0)
//#define ALOGD(...)   do {} while (0)
//#define ALOGI(...)   do {} while (0)
//#define ALOGE(...)   do {} while (0)

#include <utils/Log.h>

#include <binder/IPCThreadState.h>
#include <media/AudioTrack.h>
//#include <media/stagefright/foundation/ADebug.h>
//#include <media/stagefright/foundation/ALooper.h>
//#include <media/stagefright/AudioPlayer.h>
//#include <media/stagefright/MediaDefs.h>
//#include <media/stagefright/MediaErrors.h>
//#include <media/stagefright/MediaSource.h>
//#include <media/stagefright/MetaData.h>

using namespace android;

#include "AudioSource.h"
#include "AudioPlayer.h"

class AudioPlayerImpl : public AudioPlayer {
public:
	virtual void close();

    virtual status_t start();

    virtual void pause(bool playPendingSamples = false);
    virtual void resume();

	virtual status_t setVolume(float left, float right);

private:
    enum {
        REACHED_EOS,
        SEEK_COMPLETE
    };

	friend AudioPlayer *makeAudioPlayer(audio_stream_type_t streamType, uint32_t sampleRate, uint32_t channelCount);

    AudioPlayerImpl(uint32_t sampleRate);
	bool init(audio_stream_type_t streamType, uint32_t channelCount);

    ~AudioPlayerImpl();

    // Returns true iff a mapping is established, i.e. the AudioPlayer
    // has played at least one frame of audio.
    bool getMediaTimeMapping(int64_t *realtime_us, int64_t *mediatime_us);

    status_t seekTo(int64_t time_us);

    bool isSeeking();
    bool reachedEOS(status_t *finalStatus);

    status_t setPlaybackRatePermille(int32_t ratePermille);

private:
    AudioSource *mSource;
    sp<AudioTrack> mAudioTrack;

    int mSampleRate;
    int64_t mLatencyUs;
    size_t mFrameSize;

    Mutex mLock;
    int64_t mNumFramesPlayed;

    int64_t mPositionTimeMediaUs;
    int64_t mPositionTimeRealUs;

    bool mSeeking;
    bool mReachedEOS;
    status_t mFinalStatus;
    int64_t mSeekTimeUs;

    bool mStarted;

    static void AudioCallback(int event, void *user, void *info);
    void AudioCallback(int event, void *info);

    size_t fillBuffer(void *data, size_t size);

    void reset();

    uint32_t getNumFramesPendingPlayout() const;
};


AudioPlayerImpl::AudioPlayerImpl(uint32_t sampleRate)
    : mSource(NULL),
      mSampleRate(sampleRate),
      mLatencyUs(0),
      mFrameSize(0),
      mNumFramesPlayed(0),
      mPositionTimeMediaUs(-1),
      mPositionTimeRealUs(-1),
      mSeeking(false),
      mReachedEOS(false),
      mFinalStatus(OK),
      mStarted(false)
{
}

AudioPlayerImpl::~AudioPlayerImpl()
{
    if (mStarted) {
        reset();
    }
}

void AudioPlayerImpl::close()
{
	reset();
	delete this;
}

bool AudioPlayerImpl::init(audio_stream_type_t streamType, uint32_t channelCount)
{
    status_t err;

    ALOGI("streamType %d, sampleRate: %d, channelCount: %d", streamType, mSampleRate, channelCount);

    int frameCount;
//    size_t frameCount;
    if (AudioTrack::getMinFrameCount(&frameCount, streamType, mSampleRate) != NO_ERROR) {
        return false;
    }

	ALOGI("bufferSize %d", frameCount * 2 * 2);

    // We allow an optional INFO_FORMAT_CHANGED at the very beginning
    // of playback, if there is one, getFormat below will retrieve the
    // updated format, if there isn't, we'll stash away the valid buffer
    // of data to be used on the first audio callback.

    mAudioTrack = new AudioTrack(
            streamType, mSampleRate, AUDIO_FORMAT_PCM_16_BIT, 
            audio_channel_out_mask_from_count(channelCount),
            frameCount * 2 * 2,
            AUDIO_OUTPUT_FLAG_NONE, &AudioCallback, this, 0);

    if ((err = mAudioTrack->initCheck()) != OK) {
        mAudioTrack.clear();

        return err;
    }

    ALOGI("sampleRate %d", mAudioTrack->getSampleRate());

    mLatencyUs = (int64_t)mAudioTrack->latency() * 1000;
    mFrameSize = mAudioTrack->frameSize();

    return true;
}

status_t AudioPlayerImpl::start() {
    mAudioTrack->start();
    mAudioTrack->setVolume(0.0f, 0.0f);

    mStarted = true;

    ALOGI("started, sampleRate %d", mAudioTrack->getSampleRate());

    return OK;
}

void AudioPlayerImpl::pause(bool playPendingSamples) {
    if (playPendingSamples) {
        mAudioTrack->stop();

        mNumFramesPlayed = 0;
    } else {
        mAudioTrack->pause();
    }
}

void AudioPlayerImpl::resume() {
    mAudioTrack->start();
}

status_t AudioPlayerImpl::setVolume(float left, float right)
{
    ALOGI("setVolume(%f, %f)", left, right);
    return mAudioTrack->setVolume(left, right);
}

void AudioPlayerImpl::reset()
{
    ALOGI("reset: mStarted %d", mStarted);

    mAudioTrack->stop();
    mAudioTrack.clear();

    // Make sure to release any buffer we hold onto so that the
    // source is able to stop().

	if (mSource != NULL) {
    	mSource->stop();
    	mSource->close();
    }

    mNumFramesPlayed = 0;
    mPositionTimeMediaUs = -1;
    mPositionTimeRealUs = -1;
    mSeeking = false;
    mReachedEOS = false;
    mFinalStatus = OK;
    mStarted = false;
}

// static
void AudioPlayerImpl::AudioCallback(int event, void *user, void *info) {
    static_cast<AudioPlayerImpl *>(user)->AudioCallback(event, info);
}

bool AudioPlayerImpl::isSeeking() {
    Mutex::Autolock autoLock(mLock);
    return mSeeking;
}

bool AudioPlayerImpl::reachedEOS(status_t *finalStatus) {
    *finalStatus = OK;

    Mutex::Autolock autoLock(mLock);
    *finalStatus = mFinalStatus;
    return mReachedEOS;
}

status_t AudioPlayerImpl::setPlaybackRatePermille(int32_t ratePermille)
{
    if (mAudioTrack != NULL){
        return mAudioTrack->setSampleRate(ratePermille * mSampleRate / 1000);
    } else {
        return NO_INIT;
    }
}

void AudioPlayerImpl::AudioCallback(int event, void *info) {
    if (event != AudioTrack::EVENT_MORE_DATA) {
        return;
    }

    AudioTrack::Buffer *buffer = (AudioTrack::Buffer *)info;
    size_t numBytesWritten = fillBuffer(buffer->raw, buffer->size);

    buffer->size = numBytesWritten;
}

uint32_t AudioPlayerImpl::getNumFramesPendingPlayout() const {
    uint32_t numFramesPlayedOut;
    status_t err;

    err = mAudioTrack->getPosition(&numFramesPlayedOut);

    if (err != OK || mNumFramesPlayed < numFramesPlayedOut) {
        return 0;
    }

    // mNumFramesPlayed is the number of frames submitted
    // to the audio sink for playback, but not all of them
    // may have played out by now.
    return mNumFramesPlayed - numFramesPlayedOut;
}

size_t AudioPlayerImpl::fillBuffer(void *data, size_t size)
{
//    ALOGI("fillBuffer: %d", size);
//    ALOGI("fillBuffer: %x", (int)mSource);

    if (mSource == NULL) {
		AudioSource *source = makeAudioSource(44100, 2);
		if (source) {
	        status_t err = source->start();

	        if (err != OK) {
				source->close();
				source = NULL;

	            usleep(100 * 1000);
	            return 0;
	        }

	        mSource = source;
	    } else {
            usleep(100 * 1000);
            return 0;
	    }
    }

    size_t size_done = 0;
    size_t size_remaining = size;

    while (size_remaining > 0) {
        int copy = mSource->read((unsigned char *)data + size_done, size_remaining);
        if (copy <= 0)
        	break;

        size_done += copy;
        size_remaining -= copy;
    }

    return size_done;
}

bool AudioPlayerImpl::getMediaTimeMapping(
        int64_t *realtime_us, int64_t *mediatime_us) {
    Mutex::Autolock autoLock(mLock);

    *realtime_us = mPositionTimeRealUs;
    *mediatime_us = mPositionTimeMediaUs;

    return mPositionTimeRealUs != -1 && mPositionTimeMediaUs != -1;
}

AudioPlayer *makeAudioPlayer(audio_stream_type_t streamType, uint32_t sampleRate, uint32_t channelCount)
{
	AudioPlayerImpl *impl = new AudioPlayerImpl(sampleRate);

	if (!impl)
		return NULL;

	if (!impl->init(streamType, channelCount)) {
		delete impl;
		return NULL;
	}

	return impl;
}

