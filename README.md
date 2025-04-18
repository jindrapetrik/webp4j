# WebP4j

**WebP4j** is a Java library based on JNI (Java Native Interface) that supports WebP image encoding and decoding in Java projects. This project utilizes Google’s [libwebp](https://developers.google.com/speed/webp) library and exposes its functionality to Java applications.

## Features

- Supports WebP encoding of RGB and RGBA images.
- Supports decoding WebP images to RGB and RGBA formats.
- Provides efficient image compression and decompression using libwebp.
- Compatible with multiple platforms (supports x86 and ARM).
- Developed and tested with **JDK 17**.
- Published WebP4j to Maven Central Repository.

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

### Maven Dependency

To use WebP4j in your project, add the following dependency to your `pom.xml` file:

```xml
<dependency>
    <groupId>dev.matrixlab</groupId>
    <artifactId>webp4j</artifactId>
    <version>1.1.0</version>
</dependency>
```

### Native methods

```java
public native boolean getInfo(byte[] data, int[] dimensions);
public native int getFeatures(byte[] data, int dataSize, WebPBitstreamFeatures features);
public native byte[] encodeRGB(byte[] image, int width, int height, int stride, float quality);
public native byte[] encodeRGBA(byte[] image, int width, int height, int stride, float quality);
public native boolean decodeRGBInto(byte[] data, byte[] outputBuffer, int outputStride);
public native boolean decodeRGBAInto(byte[] data, byte[] outputBuffer, int outputStride);
```

### Encoding and Decoding methods

```java
public static byte[] encodeImage(BufferedImage bufferedImage, float quality) throws IOException;
public static BufferedImage decodeImage(byte[] webPData) throws IOException;
```

You can use the `encodeImage()` and `decodeImage()` methods of the `WebPCodec` class to convert image formats such as JPG/PNG to WEBP format.

### Example

```java
public void encodeToWebP() throws IOException {
    // Read the source image from disk
    BufferedImage bufferedImage = ImageIO.read(new File(IMAGE_FILE));

    // Set the compression quality (0.0f = worst, 100.0f = best)
    float quality = 75.0f;

    // Encode the BufferedImage into WebP byte array
    byte[] encodedWebP = WebPCodec.encodeImage(bufferedImage, quality);

    // Open an output stream to write the WebP data
    FileOutputStream fos = new FileOutputStream(WEBP_FILE);

    // Write the encoded bytes to the output file
    fos.write(encodedWebP);

    // Close the stream to release system resources
    fos.close();
}

public void decodeFromWebP() throws IOException {
    // Read all bytes from the WebP file into memory
    byte[] webPData = Files.readAllBytes(Paths.get(WEBP_FILE));

    // Decode the WebP byte array into a BufferedImage
    BufferedImage image = WebPCodec.decodeImage(webPData);

    // Write the decoded image as a JPEG file
    ImageIO.write(image, "jpg", new File(IMAGE_FILE));
}
```

## Future Work

Currently, WebP4j has encapsulated native methods. We will continue to update, develop more efficient APIs, and continuously improve documentation.

## License

This project is licensed under the [MIT License](https://opensource.org/licenses/MIT).
