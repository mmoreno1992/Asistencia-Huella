#include <string.h>
#include <jni.h>
#ifndef _Included_futronictech_com_ftrwsqandroidhelper
#define _Included_futronictech_com_ftrwsqandroidhelper

#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     futronictech_com_ftrwsqandroidhelper
 * Method:    JNIGetImageParameters
 * Signature: ([B)Z
 */
JNIEXPORT jboolean JNICALL Java_futronictech_com_ftrwsqandroidhelper_JNIGetImageParameters(JNIEnv *env, jobject obj, jbyteArray wsqImg);

/*
 * Class:     futronictech_com_ftrwsqandroidhelper
 * Method:    JNIWsqToRawImage
 * Signature: ([B[B)Z
 */
JNIEXPORT jboolean JNICALL Java_futronictech_com_ftrwsqandroidhelper_JNIWsqToRawImage(JNIEnv *env, jobject obj, jbyteArray wsqImg, jbyteArray rawImg);

#ifdef __cplusplus
}
#endif

#endif
