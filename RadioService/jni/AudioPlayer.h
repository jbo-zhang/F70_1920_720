#include <media/MediaPlayerInterface.h>
//#include <media/stagefright/MediaBuffer.h>
//#include <media/stagefright/TimeSource.h>
#include <utils/threads.h>

class AudioPlayer {
public:
    virtual ~AudioPlayer() {}

	virtual void close() = 0;

    virtual status_t start() = 0;

    virtual void pause(bool playPendingSamples = false) = 0;
    virtual void resume() = 0;

	virtual status_t setVolume(float left, float right) = 0;
};

AudioPlayer *makeAudioPlayer(audio_stream_type_t streamType, uint32_t sampleRate, uint32_t channelCount);

