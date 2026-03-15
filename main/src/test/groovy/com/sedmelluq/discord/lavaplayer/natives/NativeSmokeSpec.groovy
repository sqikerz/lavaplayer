package com.sedmelluq.discord.lavaplayer.natives

import com.sedmelluq.discord.lavaplayer.natives.aac.AacDecoder
import com.sedmelluq.discord.lavaplayer.natives.mp3.Mp3Decoder
import com.sedmelluq.discord.lavaplayer.natives.opus.OpusDecoder
import com.sedmelluq.discord.lavaplayer.natives.samplerate.SampleRateConverter
import com.sedmelluq.discord.lavaplayer.natives.vorbis.VorbisDecoder
import spock.lang.Specification

class NativeSmokeSpec extends Specification {

    def "connector native library loads"() {
        when:
        ConnectorNativeLibLoader.loadConnectorLibrary()

        then:
        noExceptionThrown()
    }

    def "Mp3Decoder creates and closes"() {
        when:
        def decoder = new Mp3Decoder()
        decoder.close()

        then:
        noExceptionThrown()
    }

    def "OpusDecoder creates and closes"() {
        when:
        def decoder = new OpusDecoder(48000, 2)
        decoder.close()

        then:
        noExceptionThrown()
    }

    def "AacDecoder creates and closes"() {
        when:
        def decoder = new AacDecoder()
        decoder.close()

        then:
        noExceptionThrown()
    }

    def "VorbisDecoder creates and closes"() {
        when:
        def decoder = new VorbisDecoder()
        decoder.close()

        then:
        noExceptionThrown()
    }

    def "SampleRateConverter creates and closes"() {
        when:
        def converter = new SampleRateConverter(SampleRateConverter.ResamplingType.LINEAR, 2, 44100, 48000)
        converter.close()

        then:
        noExceptionThrown()
    }
}
