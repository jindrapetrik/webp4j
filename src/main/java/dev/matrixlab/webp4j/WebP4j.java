package dev.matrixlab.webp4j;

import dev.matrixlab.webp4j.utils.NativeLibraryLoaderUtils;

public class WebP4j {

    public native byte[] encode(byte[] image, int width, int height, float quality);

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

    public static void main(String[] args) {
        WebP4j webP4j = new WebP4j();
        loadNativeLibrary();
        String os = System.getProperty("os.name").toLowerCase();
        String arch = System.getProperty("os.arch").toLowerCase();

        System.out.println("Current platform: " + os);
        System.out.println("Current architecture: " + arch);
    }

}
