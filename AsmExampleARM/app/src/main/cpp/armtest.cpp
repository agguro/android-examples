#include <jni.h>
#include <string>
#include <sstream>

template <typename T> std::string intToString(T value)
{
    std::ostringstream os ;
    os << value ;
    return os.str() ;
}

int Add(int a, int b) {
    int result;
    asm(
    "mov   r0, %1\n\t"
    "mov   r1, %2\n\t"
    "add   %0, r0, r1"
    : "=r" (result) // %0
    : "r" (a),      // %1
    "r" (b)         // %2
    );
    return result;
}

extern "C" int _increment(int a);

extern "C"
JNIEXPORT jstring JNICALL
Java_net_agguro_asmexamplearm_MainActivity_stringFromJNI(JNIEnv *env,jobject /* this */) {
    // call assembly function
    int x = Add(10, 15);
    x = _increment(x);          // result should be 26=10+15+1
    return env->NewStringUTF((intToString(x)).c_str());
}