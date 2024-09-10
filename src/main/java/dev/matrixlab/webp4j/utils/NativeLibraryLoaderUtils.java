package dev.matrixlab.webp4j.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class NativeLibraryLoaderUtils {

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
            architecture = arch.contains("arm") ? "aarch64" : "x64";
            libExtension = "so";
        } else if (os.contains("mac")) {
            platform = "mac";
            architecture = arch.contains("arm") ? "arm64" : "x86-64";
            libExtension = "dylib";
        } else {
            throw new UnsupportedOperationException(String.format("Unsupported os: %s, arch: %s", os, arch));
        }

        String libraryFileName = String.format("libwebp-webp4j-%s-%s-%s.%s", LIBWEBP_VERSION, platform, architecture, libExtension);

        // Path to the library in the jar
        String resourcePath = String.format("/native/%s", libraryFileName);

        // Get the library from the jar
        try (InputStream in = NativeLibraryLoaderUtils.class.getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new RuntimeException(String.format("Could not find WebP native library for %s %s in the jar", os, arch));
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
