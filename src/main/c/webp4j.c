#include <jni.h>
#include <stdlib.h>
#include <webp/encode.h>
#include "dev_matrixlab_webp4j_WebP4j.h"

/*
 * Utility function to convert a Java byte array to a native uint8_t array.
 */
uint8_t* jByteArrayToUint8(JNIEnv *env, jbyteArray array) {
    jsize len = (*env)->GetArrayLength(env, array);
    jbyte* data = (*env)->GetByteArrayElements(env, array, 0);
    uint8_t* result = (uint8_t*) malloc(len * sizeof(uint8_t));
    for (int i = 0; i < len; i++) {
        result[i] = (uint8_t) data[i];
    }
    (*env)->ReleaseByteArrayElements(env, array, data, 0);
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

    // Output buffer
    uint8_t* output = NULL;

    // Call WebPEncodeRGB function
    size_t output_size = WebPEncodeRGB(rgb, width, height, stride, quality, &output);

    // Free the input RGB array
    freeUint8(rgb);

    // Check if output is NULL
    if (output_size == 0) {
        return NULL;
    }

    // Create a new Java byte array for the output
    jbyteArray result = (*env)->NewByteArray(env, output_size);

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

    // Output buffer
    uint8_t* output = NULL;

    // Call WebPEncodeRGBA function
    size_t output_size = WebPEncodeRGBA(rgba, width, height, stride, quality, &output);

    // Free the input RGBA array
    freeUint8(rgba);

    // Check if output is NULL
    if (output_size == 0) {
        return NULL;
    }

    // Create a new Java byte array for the output
    jbyteArray result = (*env)->NewByteArray(env, output_size);

    // Copy the encoded output to the Java byte array
    (*env)->SetByteArrayRegion(env, result, 0, output_size, (jbyte*) output);

    // Free the WebP output
    WebPFree(output);

    // Return the result
    return result;
}
