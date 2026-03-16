package com.sedmelluq.discord.lavaplayer.natives;

import com.sedmelluq.discord.lavaplayer.natives.aac.AacDecoder;
import com.sedmelluq.discord.lavaplayer.natives.mp3.Mp3Decoder;
import com.sedmelluq.discord.lavaplayer.natives.opus.OpusDecoder;
import com.sedmelluq.discord.lavaplayer.natives.samplerate.SampleRateConverter;
import com.sedmelluq.discord.lavaplayer.natives.vorbis.VorbisDecoder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class NativeSmokeTest {

    @Test
    void connectorNativeLibraryLoads() {
        assertDoesNotThrow(ConnectorNativeLibLoader::loadConnectorLibrary);
    }

    @Test
    void mp3DecoderCreatesAndCloses() {
        assertDoesNotThrow(() -> {
            Mp3Decoder decoder = new Mp3Decoder();
            decoder.close();
        });
    }

    @Test
    void opusDecoderCreatesAndCloses() {
        assertDoesNotThrow(() -> {
            OpusDecoder decoder = new OpusDecoder(48000, 2);
            decoder.close();
        });
    }

    @Test
    void aacDecoderCreatesAndCloses() {
        assertDoesNotThrow(() -> {
            AacDecoder decoder = new AacDecoder();
            decoder.close();
        });
    }

    @Test
    void vorbisDecoderCreatesAndCloses() {
        assertDoesNotThrow(() -> {
            VorbisDecoder decoder = new VorbisDecoder();
            decoder.close();
        });
    }

    @Test
    void sampleRateConverterCreatesAndCloses() {
        assertDoesNotThrow(() -> {
            SampleRateConverter converter = new SampleRateConverter(SampleRateConverter.ResamplingType.LINEAR, 2, 44100, 48000);
            converter.close();
        });
    }
}
