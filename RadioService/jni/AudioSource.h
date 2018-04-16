#include <media/AudioRecord.h>
#include <media/AudioSystem.h>
//#include <media/stagefright/MediaSource.h>
//#include <media/stagefright/MediaBuffer.h>
#include <utils/List.h>

#include <system/audio.h>

class AudioSource {
public:
    virtual ~AudioSource() {}

	virtual void close() = 0;

    virtual status_t start() = 0;
    virtual status_t stop() = 0;

    virtual int read(unsigned char *buf, int size) = 0;
};

AudioSource *makeAudioSource(uint32_t sampleRate, uint32_t channelCount);

