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

    private static final int HEADER_SIZE = 44;

    /**
     * Returns a WavInfo object containing the header information
     * of the .wav file provided as an InputStream.
     * @param wavStream The InputStream of the .wav file to have the header information extracted.
     * @return A WavInfo object containing the header information of the InputStream,
     * if it is usable by the app.
     * @throws IOException
     */
    public static WavInfo readHeader(InputStream wavStream)
            throws IOException {

        // Initialize buffer.
        ByteBuffer buffer = ByteBuffer.allocate(HEADER_SIZE);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        // Load header into buffer.
        int read = wavStream.read(buffer.array(), 0, HEADER_SIZE);
        if (read != HEADER_SIZE) {
            Log.e(LOG_TAG, "Error reading header of wav file");
        }
        buffer.rewind();

        // Read format, channels, rate, bits and datsize.
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

    /**
     * Logs the provided error message if not valid.
     * @param valid The condition to be checked.
     * @param errorMessage The error message to be printed.
     * @throws IOException
     */
    private static void checkFormat(boolean valid, String errorMessage) throws IOException{
        if (!valid) {
            IOException e = new IOException();
            Log.e(LOG_TAG, errorMessage, e);
            throw e;
        }

    }

    /**
     * Reads the PCM data from the provided InputStream
     * using information from the provided WavInfo.
     * @param info The header information on the provided InputStream.
     * @param stream The PCM data to load.
     * @return A byte array containing the raw PCM data.
     * @throws IOException
     */
    public static byte[] readWavPcm(WavInfo info, InputStream stream) throws IOException {
        byte[] data = new byte[info.mDataSize];
        int read = stream.read(data, 0, data.length);
        if (read != data.length) {
            Log.e(LOG_TAG, "Error reading wav file");
        }

        return data;
    }
}
