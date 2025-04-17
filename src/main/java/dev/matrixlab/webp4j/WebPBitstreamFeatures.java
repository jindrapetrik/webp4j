package dev.matrixlab.webp4j;

public class WebPBitstreamFeatures {

    // Image width
    public int width;

    // Image height
    public int height;

    // True if the bitstream contains an alpha channel.
    public boolean hasAlpha;

    // True if the bitstream is an animation.
    public boolean hasAnimation;

    // 0 = undefined (/mixed), 1 = lossy, 2 = lossless
    public int format;

    @Override
    public String toString() {
        return "WebPBitstreamFeatures{" +
                "width=" + width +
                ", height=" + height +
                ", hasAlpha=" + hasAlpha +
                ", hasAnimation=" + hasAnimation +
                ", format=" + format +
                '}';
    }
}
