#include <jni.h>
#include <stdlib.h>
#include <webp/encode.h>
#include <webp/decode.h>
#include "dev_matrixlab_webp4j_WebP4j.h"

/*
 * Utility function to convert a Java byte array to a native uint8_t array.
 */
uint8_t* jByteArrayToUint8(JNIEnv *env, jbyteArray array) {
    jsize len = (*env)->GetArrayLength(env, array);
    jbyte* data = (*env)->GetByteArrayElements(env, array, 0);
    if (data == NULL) {
        return NULL;  // Failed to get byte array
    }

    uint8_t* result = (uint8_t*) malloc(len * sizeof(uint8_t));
    if (result == NULL) {
        (*env)->ReleaseByteArrayElements(env, array, data, JNI_ABORT);
        return NULL;  // Memory allocation failed
    }

    for (int i = 0; i < len; i++) {
        result[i] = (uint8_t) data[i];
    }

    (*env)->ReleaseByteArrayElements(env, array, data, JNI_ABORT);
    return result;
}

/*
 * Free the native uint8_t array.
 */
void freeUint8(uint8_t* ptr) {
    if (ptr) {
        free(ptr);
    }
}

/*
 * Implementation of the encodeRGB method.
 */
JNIEXPORT jbyteArray JNICALL Java_dev_matrixlab_webp4j_WebP4j_encodeRGB
  (JNIEnv *env, jobject obj, jbyteArray image, jint width, jint height, jint stride, jfloat quality) {

    // Convert Java byte array to native uint8_t array
    uint8_t* rgb = jByteArrayToUint8(env, image);
    if (rgb == NULL) {
        return NULL;  // Failed to convert byte array
    }

    // Output buffer
    uint8_t* output = NULL;

    // Call WebPEncodeRGB function
    size_t output_size = WebPEncodeRGB(rgb, width, height, stride, quality, &output);

    // Free the input RGB array
    freeUint8(rgb);

    // Check if encoding was successful
    if (output_size == 0 || output == NULL) {
        return NULL;  // Encoding failed
    }

    // Create a new Java byte array for the output
    jbyteArray result = (*env)->NewByteArray(env, output_size);
    if (result == NULL) {
        WebPFree(output);  // Ensure the output buffer is freed
        return NULL;  // Memory allocation failed
    }

    // Copy the encoded output to the Java byte array
    (*env)->SetByteArrayRegion(env, result, 0, output_size, (jbyte*) output);

    // Free the WebP output
    WebPFree(output);

    // Return the result
    return result;
}

/*
 * Implementation of the encodeRGBA method.
 */
JNIEXPORT jbyteArray JNICALL Java_dev_matrixlab_webp4j_WebP4j_encodeRGBA
  (JNIEnv *env, jobject obj, jbyteArray image, jint width, jint height, jint stride, jfloat quality) {

    // Convert Java byte array to native uint8_t array
    uint8_t* rgba = jByteArrayToUint8(env, image);
    if (rgba == NULL) {
        return NULL;  // Failed to convert byte array
    }

    // Output buffer
    uint8_t* output = NULL;

    // Call WebPEncodeRGBA function
    size_t output_size = WebPEncodeRGBA(rgba, width, height, stride, quality, &output);

    // Free the input RGBA array
    freeUint8(rgba);

    // Check if encoding was successful
    if (output_size == 0 || output == NULL) {
        return NULL;  // Encoding failed
    }

    // Create a new Java byte array for the output
    jbyteArray result = (*env)->NewByteArray(env, output_size);
    if (result == NULL) {
        WebPFree(output);  // Ensure the output buffer is freed
        return NULL;  // Memory allocation failed
    }

    // Copy the encoded output to the Java byte array
    (*env)->SetByteArrayRegion(env, result, 0, output_size, (jbyte*) output);

    // Free the WebP output
    WebPFree(output);

    // Return the result
    return result;
}

/*
 * Implementation of the getWebPInfo method.
 * This method retrieves the width and height of a WebP image.
 */
JNIEXPORT jboolean JNICALL Java_dev_matrixlab_webp4j_WebP4j_getWebPInfo
  (JNIEnv *env, jobject obj, jbyteArray data, jintArray dimensions) {

    // Convert Java byte array to native uint8_t array
    jbyte* webp_data = (*env)->GetByteArrayElements(env, data, NULL);
    if (webp_data == NULL) {
        return JNI_FALSE;  // Failed to convert byte array
    }

    // Retrieve the length of the WebP data
    jsize data_size = (*env)->GetArrayLength(env, data);

    // Declare width and height variables
    int width = 0;
    int height = 0;

    // WebP feature structure
    WebPBitstreamFeatures features;

    // Use WebPGetFeatures to retrieve the width and height of the WebP image
    VP8StatusCode status = WebPGetFeatures((const uint8_t*)webp_data, (size_t)data_size, &features);

    if (status != VP8_STATUS_OK) {
        (*env)->ReleaseByteArrayElements(env, data, webp_data, JNI_ABORT);
        return JNI_FALSE;  // Failed to get WebP features
    }

    // Set width and height
    width = features.width;
    height = features.height;

    // Get the dimensions array from Java
    jint* dims = (*env)->GetIntArrayElements(env, dimensions, NULL);
    if (dims == NULL) {
        (*env)->ReleaseByteArrayElements(env, data, webp_data, JNI_ABORT);
        return JNI_FALSE;  // Failed to get int array
    }

    // Store the width and height in the dimensions array
    dims[0] = width;
    dims[1] = height;

    // Release the dimensions array and the WebP data
    (*env)->ReleaseIntArrayElements(env, dimensions, dims, 0);
    (*env)->ReleaseByteArrayElements(env, data, webp_data, JNI_ABORT);

    return JNI_TRUE;  // Success
}

/*
 * Implementation of the decodeRGBAInto method.
 */
JNIEXPORT jboolean JNICALL Java_dev_matrixlab_webp4j_WebP4j_decodeRGBAInto
  (JNIEnv *env, jobject obj, jbyteArray data, jbyteArray outputBuffer, jint outputStride) {

    // Get data size
    jsize data_size = (*env)->GetArrayLength(env, data);

    // Get webp data
    jbyte* webp_data = (*env)->GetByteArrayElements(env, data, NULL);
    if (webp_data == NULL) {
        return JNI_FALSE;  // Failed to get data
    }

    // Get output buffer size
    jsize output_buffer_size = (*env)->GetArrayLength(env, outputBuffer);

    // Get output buffer
    jbyte* output_buffer = (*env)->GetByteArrayElements(env, outputBuffer, NULL);
    if (output_buffer == NULL) {
        (*env)->ReleaseByteArrayElements(env, data, webp_data, JNI_ABORT);
        return JNI_FALSE;
    }

    // Call WebPDecodeRGBAInto
    uint8_t* result = WebPDecodeRGBAInto(
        (const uint8_t*)webp_data,
        (size_t)data_size,
        (uint8_t*)output_buffer,
        (int)output_buffer_size,
        (int)outputStride
    );

    // Release the input data
    (*env)->ReleaseByteArrayElements(env, data, webp_data, JNI_ABORT);

    // Release the output buffer and commit changes
    (*env)->ReleaseByteArrayElements(env, outputBuffer, output_buffer, 0);

    // Check if decoding was successful
    if (result == NULL) {
        return JNI_FALSE;
    }

    return JNI_TRUE;
}

/*
 * Implementation of the decodeRGBInto method.
 */
JNIEXPORT jboolean JNICALL Java_dev_matrixlab_webp4j_WebP4j_decodeRGBInto
  (JNIEnv *env, jobject obj, jbyteArray data, jbyteArray outputBuffer, jint outputStride) {

    // Get data size
    jsize data_size = (*env)->GetArrayLength(env, data);

    // Get webp data
    jbyte* webp_data = (*env)->GetByteArrayElements(env, data, NULL);
    if (webp_data == NULL) {
        return JNI_FALSE;  // Failed to get data
    }

    // Get output buffer size
    jsize output_buffer_size = (*env)->GetArrayLength(env, outputBuffer);

    // Get output buffer
    jbyte* output_buffer = (*env)->GetByteArrayElements(env, outputBuffer, NULL);
    if (output_buffer == NULL) {
        (*env)->ReleaseByteArrayElements(env, data, webp_data, JNI_ABORT);
        return JNI_FALSE;
    }

    // Call WebPDecodeRGBInto
    uint8_t* result = WebPDecodeRGBInto(
        (const uint8_t*)webp_data,
        (size_t)data_size,
        (uint8_t*)output_buffer,
        (int)output_buffer_size,
        (int)outputStride
    );

    // Release the input data
    (*env)->ReleaseByteArrayElements(env, data, webp_data, JNI_ABORT);

    // Release the output buffer and commit changes
    (*env)->ReleaseByteArrayElements(env, outputBuffer, output_buffer, 0);

    // Check if decoding was successful
    if (result == NULL) {
        return JNI_FALSE;
    }

    return JNI_TRUE;
}
