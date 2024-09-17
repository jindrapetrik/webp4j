package dev.matrixlab.webp4j;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.imageio.ImageIO;

import static org.junit.jupiter.api.Assertions.*;

public class WebP4jTest {

    public static final String IMAGE_RGB_FILE = Paths.get(System.getProperty("user.dir"), "/src/test/resources", "test_rgb.jpg").toString();

    public static final String IMAGE_RGB_WEBP_FILE = Paths.get(System.getProperty("user.dir"), "/src/test/resources", "test_rgb.webp").toString();

    public static final String IMAGE_RGBA_FILE = Paths.get(System.getProperty("user.dir"), "/src/test/resources", "test_rgba.png").toString();

    @BeforeAll
    public static void setup() {
        // Ensure the native library is loaded
        WebP4j.ensureNativeLibraryLoaded();
    }

    @Test
    public void testGetWebPInfo() {
        try {
            // Load WebP image file
            byte[] webPData = Files.readAllBytes(Paths.get(IMAGE_RGB_WEBP_FILE));

            // Instantiate WebP4j
            WebP4j webP4j = new WebP4j();

            // Create an array to store image dimensions
            int[] dimensions = new int[2];

            // Retrieve WebP image information
            boolean success = webP4j.getWebPInfo(webPData, dimensions);

            // Validate the retrieved image dimensions
            assertTrue(success, "Failed to retrieve WebP image information.");
            assertEquals(5152, dimensions[0], "Width does not match expected value.");
            assertEquals(6440, dimensions[1], "Height does not match expected value.");

        } catch (IOException e) {
            fail("Exception thrown during WebP info retrieval: " + e.getMessage());
        }
    }

    @Test
    public void testEncodeRGB() {
        try {
            // Load RGB image file
            BufferedImage bufferedImage = ImageIO.read(new File(IMAGE_RGB_FILE));
            assertNotNull(bufferedImage, "Failed to load test image.");

            // Get image width, height, and byte array
            int width = bufferedImage.getWidth();
            int height = bufferedImage.getHeight();
            byte[] imageBytes = extractImageBytes(bufferedImage, false);

            // Instantiate WebP4j
            WebP4j webP4j = new WebP4j();

            // Encode image to WebP
            float quality = 75.0f;
            byte[] encodedWebP = webP4j.encodeRGB(imageBytes, width, height, width * 3, quality);

            // Validate the encoded result
            assertNotNull(encodedWebP, "WebP encoding failed for RGB.");
            assertTrue(encodedWebP.length > 0, "Encoded WebP file is empty.");

            // Save the encoded file for manual inspection
            try (FileOutputStream fos = new FileOutputStream(IMAGE_RGB_FILE.replace("test_rgb.jpg", "encode_rgb.webp"))) {
                fos.write(encodedWebP);
            }

        } catch (IOException e) {
            fail("Exception thrown during WebP encoding: " + e.getMessage());
        }
    }

    @Test
    public void testEncodeRGBA() {
        try {
            // Load RGBA image file
            BufferedImage bufferedImage = ImageIO.read(new File(IMAGE_RGBA_FILE));
            assertNotNull(bufferedImage, "Failed to load test image.");

            // Get image width, height, and byte array
            int width = bufferedImage.getWidth();
            int height = bufferedImage.getHeight();

            if (!bufferedImage.getColorModel().hasAlpha()) {
                fail("Test image does not have an alpha channel.");
            }

            // Convert BufferedImage to byte array in RGBA format
            byte[] imageBytes = extractImageBytesFromBufferedImage(bufferedImage);

            // Instantiate WebP4j
            WebP4j webP4j = new WebP4j();

            // Encode image to WebP
            float quality = 75.0f;
            byte[] encodedWebP = webP4j.encodeRGBA(imageBytes, width, height, width * 4, quality);

            // Validate the encoded result
            assertNotNull(encodedWebP, "WebP encoding failed for RGBA.");
            assertTrue(encodedWebP.length > 0, "Encoded WebP file is empty.");

            // Save the encoded file for manual inspection
            try (FileOutputStream fos = new FileOutputStream(IMAGE_RGBA_FILE.replace("test_rgba.png", "encode_rgba.webp"))) {
                fos.write(encodedWebP);
            }

        } catch (IOException e) {
            fail("Exception thrown during WebP encoding: " + e.getMessage());
        }
    }

    // New test method for decodeRGBInto
    @Test
    public void testDecodeRGBInto() throws IOException {
        // Load WebP image file to decode
        byte[] webPData = Files.readAllBytes(Paths.get(IMAGE_RGB_FILE.replace("test_rgb.jpg", "encode_rgb.webp")));

        // Instantiate WebP4j
        WebP4j webP4j = new WebP4j();

        // Get image dimensions from the WebP image
        int[] dimensions = new int[2];
        boolean infoSuccess = webP4j.getWebPInfo(webPData, dimensions);
        assertTrue(infoSuccess, "Failed to get WebP info.");

        int width = dimensions[0];
        int height = dimensions[1];
        int outputStride = width * 3; // RGB has 3 bytes per pixel
        byte[] outputBuffer = new byte[height * outputStride];

        // Decode WebP into RGB buffer
        boolean result = webP4j.decodeRGBInto(webPData, outputBuffer, outputStride);
        assertTrue(result, "Failed to decode WebP into RGB.");

        // Create BufferedImage from decoded RGB data
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        int index = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int r = outputBuffer[index++] & 0xFF;
                int g = outputBuffer[index++] & 0xFF;
                int b = outputBuffer[index++] & 0xFF;
                int rgb = (r << 16) | (g << 8) | b;
                image.setRGB(x, y, rgb);
            }
        }

        // Save decoded image to a file
        ImageIO.write(image, "jpg", new File(IMAGE_RGB_FILE.replace("test_rgb.jpg", "decode_rgb.jpg")));
    }

    @Test
    public void testDecodeRGBAInto() throws IOException {
        // Load WebP image file to decode
        byte[] webPData = Files.readAllBytes(Paths.get(IMAGE_RGBA_FILE.replace("test_rgba.png", "encode_rgba.webp")));

        // Instantiate WebP4j
        WebP4j webP4j = new WebP4j();

        // Get image dimensions from the WebP image
        int[] dimensions = new int[2];
        boolean infoSuccess = webP4j.getWebPInfo(webPData, dimensions);
        assertTrue(infoSuccess, "Failed to get WebP info.");

        int width = dimensions[0];
        int height = dimensions[1];
        int outputStride = width * 4; // RGBA has 4 bytes per pixel
        byte[] outputBuffer = new byte[height * outputStride];

        // Decode WebP into RGBA buffer
        boolean result = webP4j.decodeRGBAInto(webPData, outputBuffer, outputStride);
        assertTrue(result, "Failed to decode WebP into RGBA.");

        // Create BufferedImage from decoded RGBA data
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        int index = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int r = outputBuffer[index++] & 0xFF;
                int g = outputBuffer[index++] & 0xFF;
                int b = outputBuffer[index++] & 0xFF;
                int a = outputBuffer[index++] & 0xFF;

                // Create ARGB value (alpha first, then RGB)
                int argb = (a << 24) | (r << 16) | (g << 8) | b;

                image.setRGB(x, y, argb);
            }
        }

        // Save decoded image to a file
        ImageIO.write(image, "png", new File(IMAGE_RGBA_FILE.replace("test_rgba.png", "decoded_rgba.png")));
    }

    private byte[] extractImageBytesFromBufferedImage(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        byte[] rgbaBytes = new byte[width * height * 4];

        int index = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb = image.getRGB(x, y); // Extract pixel as ARGB integer

                // Extract the individual color channels from the ARGB pixel
                rgbaBytes[index++] = (byte) ((argb >> 16) & 0xFF); // Red
                rgbaBytes[index++] = (byte) ((argb >> 8) & 0xFF);  // Green
                rgbaBytes[index++] = (byte) (argb & 0xFF);         // Blue
                rgbaBytes[index++] = (byte) ((argb >> 24) & 0xFF); // Alpha
            }
        }
        return rgbaBytes;
    }

    // Helper method to extract image bytes
    private static byte[] extractImageBytes(BufferedImage image, boolean hasAlpha) {
        int width = image.getWidth();
        int height = image.getHeight();
        int bytesPerPixel = hasAlpha ? 4 : 3;
        byte[] imageBytes = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();

        // Convert RGB(A) to BGR(A)
        byte[] output = new byte[width * height * bytesPerPixel];
        for (int i = 0; i < width * height; i++) {
            int index = i * bytesPerPixel;
            output[index] = imageBytes[index + 2];      // R
            output[index + 1] = imageBytes[index + 1];  // G
            output[index + 2] = imageBytes[index];      // B
            if (hasAlpha) {
                output[index + 3] = imageBytes[index + 3];  // A
            }
        }

        return output;
    }
}