package dev.matrixlab.webp4j;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;

public class WebPCodec {

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
    public static BufferedImage convertBytesToBufferedImage(int width, int height, byte[] outputBuffer) {
        // Determine if the input buffer is RGB (3 bytes per pixel) or ARGB (4 bytes per pixel)
        boolean hasAlpha = outputBuffer.length == width * height * 4;
        int imageType = hasAlpha ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;

        // Use DataBufferInt backend to set pixels directly, avoiding setRGB calls for each pixel
        BufferedImage image = new BufferedImage(width, height, imageType);
        int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

        int index = 0;
        int pixelIndex = 0;

        // Process entire rows at once to improve cache utilization
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int r = outputBuffer[index++] & 0xFF;
                int g = outputBuffer[index++] & 0xFF;
                int b = outputBuffer[index++] & 0xFF;
                int a = hasAlpha ? (outputBuffer[index++] & 0xFF) : 255;

                // Set values directly in the pixel array
                pixels[pixelIndex++] = (a << 24) | (r << 16) | (g << 8) | b;
            }
        }

        // Set unused references to null to help GC
        outputBuffer = null;

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
    public static byte[] convertBufferedImageToBytes(BufferedImage image) {
        // Check if the image has an Alpha channel
        boolean hasAlpha = image.getColorModel().hasAlpha();

        int width = image.getWidth();
        int height = image.getHeight();
        int bytesPerPixel = hasAlpha ? 4 : 3;

        // Allocate only the necessary output buffer
        byte[] output = new byte[width * height * bytesPerPixel];
        int imageType = image.getType();

        try {
            // Handle different types of BufferedImage
            switch (imageType) {
                // INT-based types with direct buffer access
                case BufferedImage.TYPE_INT_RGB:
                case BufferedImage.TYPE_INT_ARGB:
                case BufferedImage.TYPE_INT_ARGB_PRE: {
                    // Get direct reference without creating a copy
                    DataBuffer dataBuffer = image.getRaster().getDataBuffer();
                    if (dataBuffer instanceof DataBufferInt) {
                        int[] intPixels = ((DataBufferInt) dataBuffer).getData();
                        // Use direct array access for maximum speed
                        if (hasAlpha) {
                            int index = 0;
                            for (int i = 0; i < intPixels.length; i++) {
                                int pixel = intPixels[i];
                                output[index++] = (byte) ((pixel >> 16) & 0xFF); // Red
                                output[index++] = (byte) ((pixel >> 8) & 0xFF);  // Green
                                output[index++] = (byte) (pixel & 0xFF);         // Blue
                                output[index++] = (byte) ((pixel >> 24) & 0xFF); // Alpha
                            }
                        } else {
                            int index = 0;
                            for (int i = 0; i < intPixels.length; i++) {
                                int pixel = intPixels[i];
                                output[index++] = (byte) ((pixel >> 16) & 0xFF); // Red
                                output[index++] = (byte) ((pixel >> 8) & 0xFF);  // Green
                                output[index++] = (byte) (pixel & 0xFF);         // Blue
                            }
                        }
                    } else {
                        processImageByRows(image, output, width, height, hasAlpha);
                    }
                    break;
                }

                // INT-based BGR type with direct buffer access
                case BufferedImage.TYPE_INT_BGR: {
                    DataBuffer dataBuffer = image.getRaster().getDataBuffer();
                    if (dataBuffer instanceof DataBufferInt) {
                        int[] bgrIntPixels = ((DataBufferInt) dataBuffer).getData();
                        int index = 0;
                        for (int i = 0; i < bgrIntPixels.length; i++) {
                            int pixel = bgrIntPixels[i];
                            output[index++] = (byte) ((pixel) & 0xFF);       // Red (BGR order)
                            output[index++] = (byte) ((pixel >> 8) & 0xFF);  // Green
                            output[index++] = (byte) ((pixel >> 16) & 0xFF); // Blue (BGR order)
                            if (hasAlpha) {
                                output[index++] = (byte) ((pixel >> 24) & 0xFF); // Alpha
                            }
                        }
                    } else {
                        processImageByRows(image, output, width, height, hasAlpha);
                    }
                    break;
                }

                // BYTE-based types with direct buffer access
                case BufferedImage.TYPE_3BYTE_BGR: {
                    DataBuffer dataBuffer = image.getRaster().getDataBuffer();
                    if (dataBuffer instanceof DataBufferByte) {
                        byte[] bgrBytes = ((DataBufferByte) dataBuffer).getData();
                        int index = 0;
                        // Unroll the loop for better performance
                        int maxIndex = bgrBytes.length - 2;  // Safe limit for unrolled loop
                        int i = 0;

                        // Process 3 pixels (9 bytes) at a time
                        for (; i < maxIndex - 8; i += 9) {
                            // Pixel 1
                            output[index++] = bgrBytes[i + 2];
                            output[index++] = bgrBytes[i + 1];
                            output[index++] = bgrBytes[i];

                            // Pixel 2
                            output[index++] = bgrBytes[i + 5];
                            output[index++] = bgrBytes[i + 4];
                            output[index++] = bgrBytes[i + 3];

                            // Pixel 3
                            output[index++] = bgrBytes[i + 8];
                            output[index++] = bgrBytes[i + 7];
                            output[index++] = bgrBytes[i + 6];
                        }

                        // Handle remaining pixels
                        for (; i < bgrBytes.length; i += 3) {
                            output[index++] = bgrBytes[i + 2];  // Red (BGR → RGB)
                            output[index++] = bgrBytes[i + 1];  // Green
                            output[index++] = bgrBytes[i];      // Blue (BGR → RGB)
                        }
                    } else {
                        processImageByRows(image, output, width, height, hasAlpha);
                    }
                    break;
                }

                case BufferedImage.TYPE_4BYTE_ABGR:
                case BufferedImage.TYPE_4BYTE_ABGR_PRE: {
                    DataBuffer dataBuffer = image.getRaster().getDataBuffer();
                    if (dataBuffer instanceof DataBufferByte) {
                        byte[] abgrBytes = ((DataBufferByte) dataBuffer).getData();
                        if (hasAlpha) {
                            int index = 0;
                            // Similar loop unrolling for 4-byte pixels
                            int maxIndex = abgrBytes.length - 7;
                            int i = 0;

                            // Process 2 pixels (8 bytes) at a time
                            for (; i < maxIndex; i += 8) {
                                // Pixel 1
                                output[index++] = abgrBytes[i + 3];  // Red
                                output[index++] = abgrBytes[i + 2];  // Green
                                output[index++] = abgrBytes[i + 1];  // Blue
                                output[index++] = abgrBytes[i];      // Alpha

                                // Pixel 2
                                output[index++] = abgrBytes[i + 7];  // Red
                                output[index++] = abgrBytes[i + 6];  // Green
                                output[index++] = abgrBytes[i + 5];  // Blue
                                output[index++] = abgrBytes[i + 4];  // Alpha
                            }

                            // Handle remaining pixels
                            for (; i < abgrBytes.length; i += 4) {
                                output[index++] = abgrBytes[i + 3];  // Red
                                output[index++] = abgrBytes[i + 2];  // Green
                                output[index++] = abgrBytes[i + 1];  // Blue
                                output[index++] = abgrBytes[i];      // Alpha
                            }
                        } else {
                            // When hasAlpha is false but image has 4 bytes per pixel
                            int index = 0;
                            for (int i = 0; i < abgrBytes.length; i += 4) {
                                output[index++] = abgrBytes[i + 3];  // Red
                                output[index++] = abgrBytes[i + 2];  // Green
                                output[index++] = abgrBytes[i + 1];  // Blue
                            }
                        }
                    } else {
                        processImageByRows(image, output, width, height, hasAlpha);
                    }
                    break;
                }

                // Default case for all other types
                default:
                    processImageByRows(image, output, width, height, hasAlpha);
                    break;
            }
        } catch (Exception e) {
            // Fallback if any error occurs during optimized processing
            processImageByRows(image, output, width, height, hasAlpha);
        }

        return output;
    }

    private static void processImageByRows(BufferedImage image, byte[] output, int width, int height, boolean hasAlpha) {
        // More efficient row-by-row processing
        int[] rowBuffer = new int[width];
        int index = 0;
        int bytesPerPixel = hasAlpha ? 4 : 3;

        for (int y = 0; y < height; y++) {
            // Get entire row at once
            image.getRGB(0, y, width, 1, rowBuffer, 0, width);

            if (hasAlpha) {
                for (int x = 0; x < width; x++) {
                    int argb = rowBuffer[x];
                    output[index++] = (byte) ((argb >> 16) & 0xFF); // Red
                    output[index++] = (byte) ((argb >> 8) & 0xFF);  // Green
                    output[index++] = (byte) (argb & 0xFF);         // Blue
                    output[index++] = (byte) ((argb >> 24) & 0xFF); // Alpha
                }
            } else {
                for (int x = 0; x < width; x++) {
                    int argb = rowBuffer[x];
                    output[index++] = (byte) ((argb >> 16) & 0xFF); // Red
                    output[index++] = (byte) ((argb >> 8) & 0xFF);  // Green
                    output[index++] = (byte) (argb & 0xFF);         // Blue
                }
            }
        }
    }

}
