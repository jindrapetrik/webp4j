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

    private static final String TEST_WEBP_FILE = "path/to/test_image.webp";
    private static final String TEST_IMAGE_FILE = "path/to/test_image.jpg";

    @BeforeAll
    public static void setup() {
        // Ensure the native library is loaded
        WebP4j.ensureNativeLibraryLoaded();
    }

    @Test
    public void testGetWebPInfo() {
        try {
            // Load WebP image file
            byte[] webPData = Files.readAllBytes(Paths.get(TEST_WEBP_FILE));

            // Instantiate WebP4j
            WebP4j webP4j = new WebP4j();

            // Create an array to store image dimensions
            int[] dimensions = new int[2];

            // Retrieve WebP image information
            boolean success = webP4j.getWebPInfo(webPData, dimensions);

            // Validate the retrieved image dimensions
            assertTrue(success, "Failed to retrieve WebP image information.");
            assertEquals(800, dimensions[0], "Width does not match expected value.");
            assertEquals(600, dimensions[1], "Height does not match expected value.");

        } catch (IOException e) {
            fail("Exception thrown during WebP info retrieval: " + e.getMessage());
        }
    }

    @Test
    public void testEncodeRGB() {
        try {
            // Load RGB image file
            BufferedImage bufferedImage = ImageIO.read(new File(TEST_IMAGE_FILE));
            assertNotNull(bufferedImage, "Failed to load test image.");

            // Get image width, height, and byte array
            int width = bufferedImage.getWidth();
            int height = bufferedImage.getHeight();
            boolean hasAlpha = bufferedImage.getColorModel().hasAlpha();
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
            try (FileOutputStream fos = new FileOutputStream("encoded_rgb_image.webp")) {
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
            BufferedImage bufferedImage = ImageIO.read(new File(TEST_IMAGE_FILE));
            assertNotNull(bufferedImage, "Failed to load test image.");

            // Get image width, height, and byte array
            int width = bufferedImage.getWidth();
            int height = bufferedImage.getHeight();
            boolean hasAlpha = bufferedImage.getColorModel().hasAlpha();
            byte[] imageBytes = extractImageBytes(bufferedImage, hasAlpha);

            // Instantiate WebP4j
            WebP4j webP4j = new WebP4j();

            // Encode image to WebP
            float quality = 75.0f;
            byte[] encodedWebP = webP4j.encodeRGBA(imageBytes, width, height, width * 4, quality);

            // Validate the encoded result
            assertNotNull(encodedWebP, "WebP encoding failed for RGBA.");
            assertTrue(encodedWebP.length > 0, "Encoded WebP file is empty.");

            // Save the encoded file for manual inspection
            try (FileOutputStream fos = new FileOutputStream("encoded_rgba_image.webp")) {
                fos.write(encodedWebP);
            }

        } catch (IOException e) {
            fail("Exception thrown during WebP encoding: " + e.getMessage());
        }
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