package dev.matrixlab.webp4j;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class WebP4j {

    // int WebPGetInfo(const uint8_t* data, size_t data_size, int* width, int* height);
    public native boolean getWebPInfo(byte[] data, int[] dimensions);

    // size_t WebPEncodeRGB(const uint8_t* rgb, int width, int height, int stride, float quality_factor, uint8_t** output);
    public native byte[] encodeRGB(byte[] image, int width, int height, int stride, float quality);

    // size_t WebPEncodeRGBA(const uint8_t* rgba, int width, int height, int stride, float quality_factor, uint8_t** output);
    public native byte[] encodeRGBA(byte[] image, int width, int height, int stride, float quality);

    // uint8_t* WebPDecodeRGBAInto(const uint8_t* data, size_t data_size, uint8_t* output_buffer, int output_buffer_size, int output_stride);
    public native boolean decodeRGBAInto(byte[] data, byte[] outputBuffer, int outputStride);

    // uint8_t* WebPDecodeRGBInto(const uint8_t* data, size_t data_size, uint8_t* output_buffer, int output_buffer_size, int output_stride);
    public native boolean decodeRGBInto(byte[] data, byte[] outputBuffer, int outputStride);

    private static volatile boolean NATIVE_LIBRARY_LOADED = false;

    // Use the NativeLibraryLoaderUtils to load the native library
    static void loadNativeLibrary() {
        if (!NATIVE_LIBRARY_LOADED) {
            synchronized (WebP4j.class) {
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
    public static void ensureNativeLibraryLoaded() {
        loadNativeLibrary();  // Ensure the native library is loaded
    }

    // When the class is loaded, try to load the native library
    static {
        // If you delete it, the library will be loaded when the first method that uses the native library is called.
        // loadNativeLibrary();
    }

    public WebP4j() {
    }

}

class NativeLibraryLoaderUtils {

    private static final String LIBWEBP_VERSION = "1.4.0";

    public static void loadLibrary() {
        String os = System.getProperty("os.name").toLowerCase();
        String arch = System.getProperty("os.arch").toLowerCase();

        String platform;
        String architecture;
        String libExtension;

        if (os.contains("win")) {
            platform = "windows";
            architecture = "x64";
            libExtension = "dll";
        } else if (os.contains("nux") || os.contains("linux")) {
            platform = "linux";
            architecture = arch.contains("aarch64") ? "aarch64" : "x86-64";
            libExtension = "so";
        } else if (os.contains("mac")) {
            platform = "mac";
            architecture = arch.contains("aarch64") ? "arm64" : "x86-64";
            libExtension = "dylib";
        } else {
            throw new UnsupportedOperationException(String.format("Unsupported os: %s, arch: %s", os, arch));
        }

        String libraryFileName = String.format("webp4j-%s-%s-%s.%s", LIBWEBP_VERSION, platform, architecture, libExtension);

        // Path to the library in the jar
        String resourcePath = String.format("/native/%s", libraryFileName);

        // Get the library from the jar
        try (InputStream in = NativeLibraryLoaderUtils.class.getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new RuntimeException(String.format("Could not find WebP native library(%s) for %s %s in the jar", libraryFileName, os, arch));
            }

            File tempLibraryFile = Files.createTempFile("", libraryFileName).toFile();
            tempLibraryFile.deleteOnExit();

            try (FileOutputStream out = new FileOutputStream(tempLibraryFile)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }

            // Load the library
            System.load(tempLibraryFile.getAbsolutePath());

        } catch (IOException e) {
            throw new RuntimeException("Could not load native WebP library", e);
        }
    }
}
