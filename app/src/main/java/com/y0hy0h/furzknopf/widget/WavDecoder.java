package com.y0hy0h.furzknopf.widget;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Code partly taken from
 * http://mindtherobot.com/blog/580/android-audio-play-a-wav-file-on-an-audiotrack/.
 */
public class WavDecoder {

    private static final String LOG_TAG = WavDecoder.class.getSimpleName();

    private static final String RIFF_HEADER = "RIFF";
    private static final String WAVE_HEADER = "WAVE";
    private static final String FMT_HEADER = "fmt ";
    private static final String DATA_HEADER = "data";

    private static final int HEADER_SIZE = 44;

    private static final String CHARSET = "ASCII";

    public static WavInfo readHeader(InputStream wavStream)
            throws IOException {

        ByteBuffer buffer = ByteBuffer.allocate(HEADER_SIZE);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        int read = wavStream.read(buffer.array(), 0, HEADER_SIZE);
        if (read != HEADER_SIZE) {
            Log.e(LOG_TAG, "Error reading header of wav file");
        }
        buffer.rewind();

        buffer.position(20);
        int format = buffer.getShort();
        checkFormat(format == 1, "Unsupported encoding: " + format); // 1 means Linear PCM
        int channels = buffer.getShort();
        checkFormat(channels == 1, "Unsupported channels: " + channels);
        int rate = buffer.getInt();
        checkFormat(rate == 44100, "Unsupported rate: " + rate);

        buffer.position(34);
        int bits = buffer.getShort();
        checkFormat(bits == 16, "Unsupported bits: " + bits);

        buffer.position(40);
        int dataSize = buffer.getInt();
        checkFormat(dataSize > 0, "wrong datasize: " + dataSize);

        return new WavInfo(channels, rate, bits, dataSize);
    }

    private static void checkFormat(boolean valid, String errorMessage) throws IOException{
        if (!valid) {
            IOException e = new IOException();
            Log.e(LOG_TAG, errorMessage, e);
            throw e;
        }

    }

    public static byte[] readWavPcm(WavInfo info, InputStream stream) throws IOException {
        byte[] data = new byte[info.mDataSize];
        int read = stream.read(data, 0, data.length);
        if (read != data.length) {
            Log.e(LOG_TAG, "Error reading wav file");
        }

        return data;
    }
}
