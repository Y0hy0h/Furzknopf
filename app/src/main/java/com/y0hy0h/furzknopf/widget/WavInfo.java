package com.y0hy0h.furzknopf.widget;

public class WavInfo {

    public final int mChannels;
    public final int mRate;
    public final int mBits;
    public final int mDataSize;

    public WavInfo(int channels, int rate, int bits, int dataSize) {
        mChannels = channels;
        mRate = rate;
        mBits = bits;
        mDataSize = dataSize;
    }
}
