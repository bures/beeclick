/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class d3scomp_beeclick_D2XX */

#ifndef _Included_d3scomp_beeclick_D2XX
#define _Included_d3scomp_beeclick_D2XX
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     d3scomp_beeclick_D2XX
 * Method:    _getLibraryVersion
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_d3scomp_beeclick_D2XX__1getLibraryVersion
  (JNIEnv *, jclass);

/*
 * Class:     d3scomp_beeclick_D2XX
 * Method:    _init
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_d3scomp_beeclick_D2XX__1init
  (JNIEnv *, jclass);

/*
 * Class:     d3scomp_beeclick_D2XX
 * Method:    _open
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_d3scomp_beeclick_D2XX__1open
  (JNIEnv *, jclass);

/*
 * Class:     d3scomp_beeclick_D2XX
 * Method:    _close
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_d3scomp_beeclick_D2XX__1close
  (JNIEnv *, jclass, jlong);

/*
 * Class:     d3scomp_beeclick_D2XX
 * Method:    _write
 * Signature: (JLjava/nio/ByteBuffer;I)I
 */
JNIEXPORT jint JNICALL Java_d3scomp_beeclick_D2XX__1write
  (JNIEnv *, jclass, jlong, jobject, jint);

/*
 * Class:     d3scomp_beeclick_D2XX
 * Method:    _read
 * Signature: (JLjava/nio/ByteBuffer;I)I
 */
JNIEXPORT jint JNICALL Java_d3scomp_beeclick_D2XX__1read
  (JNIEnv *, jclass, jlong, jobject, jint);

/*
 * Class:     d3scomp_beeclick_D2XX
 * Method:    _readAvailable
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_d3scomp_beeclick_D2XX__1readAvailable
  (JNIEnv *, jclass, jlong);

#ifdef __cplusplus
}
#endif
#endif