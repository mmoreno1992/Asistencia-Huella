#include "ftrwsqandroidjni.h"
#include "ftrwsqandroid.h"

JNIEXPORT jboolean JNICALL Java_futronictech_com_ftrwsqandroidhelper_JNIGetImageParameters(JNIEnv *env, jobject obj, jbyteArray wsqImg)
{
	int wsqLength = env->GetArrayLength(wsqImg);
	if( wsqLength < 0 )
		return JNI_FALSE;
    jboolean isCopy;
    jbyte *pJData = env->GetByteArrayElements( wsqImg, &isCopy );
    FTRIMGPARMS params;
    params.WSQ_size = wsqLength;
	if( !ftrWSQ_GetImageParameters((unsigned char *)pJData, &params) )
	{
		env->ReleaseByteArrayElements( wsqImg, pJData, 0 );
		return JNI_FALSE;
	}
	env->ReleaseByteArrayElements( wsqImg, pJData, 0 );

	jclass cls = env->GetObjectClass(obj);
	jfieldID fidw = NULL;
	jfieldID fidh = NULL;
    fidw = env->GetFieldID( cls, "mWidth", "I" );
    if( fidw == NULL )
        return JNI_FALSE;
	env->SetIntField( obj, fidw, params.Width );
	//
    fidh = env->GetFieldID( cls, "mHeight", "I" );
    if( fidh == NULL )
        return JNI_FALSE;
	env->SetIntField( obj, fidh, params.Height );
	return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL Java_futronictech_com_ftrwsqandroidhelper_JNIWsqToRawImage(JNIEnv *env, jobject obj, jbyteArray wsqImg, jbyteArray rawImg)
{
	int wsqLength = env->GetArrayLength(wsqImg);
	if( wsqLength < 0 )
		return JNI_FALSE;
    jboolean isCopy;
    jbyte *pJWSQ = env->GetByteArrayElements( wsqImg, &isCopy );
    jbyte *pJRAW = env->GetByteArrayElements( rawImg, &isCopy );
    FTRIMGPARMS params;
    params.WSQ_size = wsqLength;
	if( !ftrWSQ_ToRawImage((unsigned char *)pJWSQ, &params, (unsigned char *)pJRAW) )
	{
		env->ReleaseByteArrayElements( wsqImg, pJWSQ, 0 );
		env->ReleaseByteArrayElements( rawImg, pJRAW, 0 );
		return JNI_FALSE;
	}
	env->ReleaseByteArrayElements( wsqImg, pJWSQ, 0 );
	env->ReleaseByteArrayElements( rawImg, pJRAW, 0 );
	return JNI_TRUE;
}

