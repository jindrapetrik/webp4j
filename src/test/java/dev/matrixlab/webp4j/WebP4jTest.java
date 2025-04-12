package dev.matrixlab.webp4j;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.imageio.ImageIO;

import static org.junit.jupiter.api.Assertions.*;

public class WebP4jTest {

    private static WebP4j webP4j;

    public static final String IMAGE_RGB_FILE = Paths.get(System.getProperty("user.dir"), "/src/test/resources", "test_rgb.jpg").toString();

    public static final String IMAGE_RGB_WEBP_FILE = Paths.get(System.getProperty("user.dir"), "/src/test/resources", "test_rgb.webp").toString();

    public static final String IMAGE_RGBA_FILE = Paths.get(System.getProperty("user.dir"), "/src/test/resources", "test_rgba.png").toString();

    @BeforeAll
    public static void setup() {
        // Ensure the native library is loaded
        WebP4j.ensureNativeLibraryLoaded();
        // Initialize the singleton instance
        webP4j = new WebP4j();
    }

    @Test
    public void testGetWebPInfo() {
        try {
            // Load WebP image file
            byte[] webPData = Files.readAllBytes(Paths.get(IMAGE_RGB_WEBP_FILE));

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
            byte[] imageBytes = convertBufferedImageToBytes(bufferedImage);

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
            byte[] imageBytes = convertBufferedImageToBytes(bufferedImage);

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
        BufferedImage image = convertBytesToBufferedImage(width, height, outputBuffer);

        // Save decoded image to a file
        ImageIO.write(image, "jpg", new File(IMAGE_RGB_FILE.replace("test_rgb.jpg", "decode_rgb.jpg")));
    }

    @Test
    public void testDecodeRGBAInto() throws IOException {
        // Load WebP image file to decode
        byte[] webPData = Files.readAllBytes(Paths.get(IMAGE_RGBA_FILE.replace("test_rgba.png", "encode_rgba.webp")));

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
        BufferedImage image = convertBytesToBufferedImage(width, height, outputBuffer);

        // Save decoded image to a file
        ImageIO.write(image, "png", new File(IMAGE_RGBA_FILE.replace("test_rgba.png", "decoded_rgba.png")));
    }

    /**
     * Creates a BufferedImage from a byte array containing pixel data.
     * <p>
     * This method supports both RGB (3 bytes per pixel) and ARGB (4 bytes per pixel) formats.
     * It determines the format based on the length of the input byte array and the image dimensions.
     *
     * @param width        The width of the image.
     * @param height       The height of the image.
     * @param outputBuffer A byte array containing the pixel data.
     *                     - For RGB format: Each pixel is represented by 3 consecutive bytes (R, G, B).
     *                     - For ARGB format: Each pixel is represented by 4 consecutive bytes (R, G, B, A).
     * @return A BufferedImage object representing the image with the specified width, height, and pixel data.
     *         - If the input buffer is RGB, the image will be of type BufferedImage.TYPE_INT_RGB.
     *         - If the input buffer is ARGB, the image will be of type BufferedImage.TYPE_INT_ARGB.
     * @throws IllegalArgumentException if the length of the outputBuffer does not match the expected size
     *                                  for the given width, height, and pixel format.
     */
    private static BufferedImage convertBytesToBufferedImage(int width, int height, byte[] outputBuffer) {
        // Determine if the input buffer is RGB (3 bytes per pixel) or ARGB (4 bytes per pixel)
        boolean hasAlpha = outputBuffer.length == width * height * 4;
        int imageType = hasAlpha ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;

        BufferedImage image = new BufferedImage(width, height, imageType);
        int index = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int r = outputBuffer[index++] & 0xFF;
                int g = outputBuffer[index++] & 0xFF;
                int b = outputBuffer[index++] & 0xFF;
                int a = hasAlpha ? (outputBuffer[index++] & 0xFF) : 255; // Default alpha to 255 if not present

                // Create ARGB or RGB value
                int argb = (a << 24) | (r << 16) | (g << 8) | b;
                image.setRGB(x, y, argb);
            }
        }

        return image;
    }

    /**
     * Extracts pixel data from a BufferedImage into a byte array.
     * <p>
     * This method automatically detects the image's color type, channel order, and alpha presence,
     * then extracts pixel data accordingly.
     *
     * @param image The BufferedImage to extract pixel data from.
     * @return A byte array containing the pixel data in the appropriate color format.
     */
    private static byte[] convertBufferedImageToBytes(BufferedImage image) {
        // Check if the image has an Alpha channel (transparency).
        // If the image has an Alpha channel, each pixel will have 4 bytes (RGBA).
        // Otherwise, each pixel will have 3 bytes (RGB).
        boolean hasAlpha = image.getColorModel().hasAlpha();

        int width = image.getWidth();
        int height = image.getHeight();
        int bytesPerPixel = hasAlpha ? 4 : 3; // Use 4 bytes per pixel for RGBA, 3 bytes per pixel for RGB.
        byte[] output = new byte[width * height * bytesPerPixel]; // Allocate the output byte array.

        // Retrieve all pixel data from the image using the getRGB method.
        // The getRGB method always returns pixel data in ARGB format, regardless of the image's internal storage format.
        // This means that even if the image is stored internally in BGR or ABGR format, the data retrieved here will be in ARGB format.
        int[] argbPixels = image.getRGB(0, 0, width, height, null, 0, width);
        int index = 0;

        // Loop through each pixel in the ARGB array and extract the color channels.
        for (int argb : argbPixels) {
            // Extract the Red, Green, and Blue channels from the ARGB value.
            // The ARGB format stores the Alpha channel in the highest 8 bits, followed by Red, Green, and Blue.
            output[index++] = (byte) ((argb >> 16) & 0xFF); // Extract the Red channel.
            output[index++] = (byte) ((argb >> 8) & 0xFF);  // Extract the Green channel.
            output[index++] = (byte) (argb & 0xFF);         // Extract the Blue channel.

            // If the image has an Alpha channel, extract it as well.
            if (hasAlpha) {
                output[index++] = (byte) ((argb >> 24) & 0xFF); // Extract the Alpha channel.
            }
        }

        // Return the byte array containing the extracted pixel data.
        return output;
    }

}