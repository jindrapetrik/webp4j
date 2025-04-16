package dev.matrixlab.webp4j;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

public class WebP4jTest {

    public static final String IMAGE_RGB_FILE = Paths.get(System.getProperty("user.dir"), "/src/test/resources", "test_rgb.jpg").toString();

    public static final String IMAGE_RGB_WEBP_FILE = Paths.get(System.getProperty("user.dir"), "/src/test/resources", "test_rgb.webp").toString();

    public static final String IMAGE_RGBA_FILE = Paths.get(System.getProperty("user.dir"), "/src/test/resources", "test_rgba.png").toString();

    @Test
    public void testGetWebPInfo() {
        try {
            // Load WebP image file
            byte[] webPData = Files.readAllBytes(Paths.get(IMAGE_RGB_WEBP_FILE));

            // Retrieve WebP image information
            int[] dimensions = WebPCodec.getWebPInfo(webPData);

            // Validate the retrieved image dimensions
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

            // Encode image to WebP
            float quality = 75.0f;
            byte[] encodedWebP = WebPCodec.encodeImage(bufferedImage, quality);

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

            // Encode image to WebP
            float quality = 75.0f;
            byte[] encodedWebP = WebPCodec.encodeImage(bufferedImage, quality);

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

    /**
     * Test method for decoding an RGB WebP image.
     */
    @Test
    public void testDecodeRGBInto() throws IOException {
        // Load WebP image file to decode
        byte[] webPData = Files.readAllBytes(Paths.get(IMAGE_RGB_FILE.replace("test_rgb.jpg", "encode_rgb.webp")));
        assertNotNull(webPData, "WebP data should not be null.");
        assertTrue(webPData.length > 0, "WebP data should not be empty.");

        // Create BufferedImage from decoded RGB data
        BufferedImage image = WebPCodec.decodeImage(webPData, false);
        assertNotNull(image, "Decoded RGB image should not be null.");
        assertTrue(image.getWidth() > 0 && image.getHeight() > 0, "Decoded RGB image dimensions must be positive.");
        // Construct the output file path for saving the decoded RGB image.
        assertFalse(image.getColorModel().hasAlpha(), "Decoded RGB image should not have alpha.");

        // Save decoded image to a file
        ImageIO.write(image, "jpg", new File(IMAGE_RGB_FILE.replace("test_rgb.jpg", "decode_rgb.jpg")));
    }

    /**
     * Test method for decoding an RGBA WebP image.
     */
    @Test
    public void testDecodeRGBAInto() throws IOException {
        // Load WebP image file to decode
        byte[] webPData = Files.readAllBytes(Paths.get(IMAGE_RGBA_FILE.replace("test_rgba.png", "encode_rgba.webp")));
        assertNotNull(webPData, "WebP data should not be null.");
        assertTrue(webPData.length > 0, "WebP data should not be empty.");

        // Create BufferedImage from decoded RGBA data
        BufferedImage image = WebPCodec.decodeImage(webPData, true);
        assertNotNull(image, "Decoded RGBA image should not be null.");
        assertTrue(image.getWidth() > 0 && image.getHeight() > 0, "Decoded RGBA image dimensions must be positive.");
        // Verify that the image has an alpha channel.
        assertTrue(image.getColorModel().hasAlpha(), "Decoded RGBA image should have alpha.");

        // Save decoded image to a file
        ImageIO.write(image, "png", new File(IMAGE_RGBA_FILE.replace("test_rgba.png", "decoded_rgba.png")));
    }

}