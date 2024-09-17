# WebP4j

**WebP4j** is a Java library based on JNI (Java Native Interface) that supports WebP image encoding and decoding in Java projects. This project utilizes Google’s [libwebp](https://developers.google.com/speed/webp) library and exposes its functionality to Java applications.

## Features

- Supports WebP encoding of RGB and RGBA images.
- Supports decoding WebP images to RGB and RGBA formats.
- Provides efficient image compression and decompression using libwebp.
- Compatible with multiple platforms (supports x86 and ARM).
- Developed and tested with **JDK 17**.

## Prerequisites

Before using WebP4j, ensure you have the following requirements in place:

- Java Development Kit (JDK) 17 or higher

### Important Note:

My current development and compilation environment is based on JDK 17. If you’re using a version of JDK below 17, please perform your own testing to ensure compatibility. If you encounter any issues, feel free to provide feedback, and I will do my best to assist.

## Supported platforms

WebP4j currently supports the following platforms:

- Windows(x86-64)
- Linux(x86-64)
- Linux(arm64)
- MacOS(arm64)

### macOS (x86-64) Support:

Unfortunately, I do not have access to a macOS (x86-64) device, so I am unable to compile the native library for this platform. If you have access to a macOS (x86-64) device, I would greatly appreciate it if you could compile the native library for me and share it with the project. Thank you in advance for your contribution!

## API

```java
public native boolean getWebPInfo(byte[] data, int[] dimensions);
public native byte[] encodeRGB(byte[] image, int width, int height, int stride, float quality);
public native byte[] encodeRGBA(byte[] image, int width, int height, int stride, float quality);
public native boolean decodeRGBAInto(byte[] data, byte[] outputBuffer, int outputStride);
public native boolean decodeRGBInto(byte[] data, byte[] outputBuffer, int outputStride);
```

## Future Work

At present, WebP4j has been developed with native methods for JNI calls, supporting encoding and decoding of RGB and RGBA images. In the future, we plan to further encapsulate and extend the functionality, aiming to provide a **high-level API** that is easy to use and ready out-of-the-box. The goal is to make the library even more user-friendly by abstracting low-level operations, allowing developers to focus on integration without needing to manage native resources directly.

Stay tuned for upcoming updates!

## License

This project is licensed under the [MIT License](https://opensource.org/licenses/MIT).
