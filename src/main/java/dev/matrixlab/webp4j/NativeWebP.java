package dev.matrixlab.webp4j;

public class NativeWebP {

    private static volatile boolean NATIVE_LIBRARY_LOADED = false;

    // int WebPGetInfo(const uint8_t* data, size_t data_size, int* width, int* height);
    public native boolean getInfo(byte[] data, int[] dimensions);

    // VP8StatusCode WebPGetFeatures(const uint8_t* data, size_t data_size, WebPBitstreamFeatures* features);
    public native int getFeatures(byte[] data, int dataSize, WebPBitstreamFeatures features);

    // size_t WebPEncodeRGB(const uint8_t* rgb, int width, int height, int stride, float quality_factor, uint8_t** output);
    public native byte[] encodeRGB(byte[] image, int width, int height, int stride, float quality);

    // size_t WebPEncodeRGBA(const uint8_t* rgba, int width, int height, int stride, float quality_factor, uint8_t** output);
    public native byte[] encodeRGBA(byte[] image, int width, int height, int stride, float quality);

    // uint8_t* WebPDecodeRGBInto(const uint8_t* data, size_t data_size, uint8_t* output_buffer, int output_buffer_size, int output_stride);
    public native boolean decodeRGBInto(byte[] data, byte[] outputBuffer, int outputStride);

    // uint8_t* WebPDecodeRGBAInto(const uint8_t* data, size_t data_size, uint8_t* output_buffer, int output_buffer_size, int output_stride);
    public native boolean decodeRGBAInto(byte[] data, byte[] outputBuffer, int outputStride);

    // Use the NativeLibraryLoaderUtils to load the native library
    static void loadNativeLibrary() {
        if (!NATIVE_LIBRARY_LOADED) {
            synchronized (NativeWebP.class) {
                if (!NATIVE_LIBRARY_LOADED) {
                    try {
                        NativeLibraryLoaderUtils.loadLibrary();
                        NATIVE_LIBRARY_LOADED = true;
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to load native library", e);
                    }
                }
            }
        }
    }

    // Any method that needs to use the native library can call this function to lazy load it.
    // public static void ensureNativeLibraryLoaded() {
    //     loadNativeLibrary();  // Ensure the native library is loaded
    // }

    // When the class is loaded, try to load the native library
    static {
        try {
            loadNativeLibrary();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load native library", e);
        }
    }

    public NativeWebP() {
    }

}
