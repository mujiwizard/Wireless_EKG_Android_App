#include <jni.h>
#include <string>

extern "C"
jstring
Java_com_g13_mano_g13_1wireless_1ekg_SignInActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}
