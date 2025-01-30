/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * In applying the License, ECMWF does not waive the privileges and immunities
 * granted to it by virtue of its status as an inter-governmental organization
 * nor does it submit to any jurisdiction.
 */

#include <fcntl.h>
#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/wait.h>
#include <sys/types.h>
#include <sys/select.h>
#include <sys/socket.h>
#include <netinet/tcp.h>
#include <netinet/in.h>
#include <unistd.h>
#include <iconv.h>

/*
 * ECMWF Product Data Store (OpenECPDS) Project
 *
 * @author Laurent Gougeon - syi@ecmwf.int, ECMWF.
 * @version 6.7.7
 * @since 2024-07-01
 */

// Generic method to retrieve the file descriptor
int getSocketDescriptor(JNIEnv *env, jobject socket) {
    jclass socketClass = (*env)->GetObjectClass(env, socket);
    
    // Get the getImpl method ID
    jmethodID getImplMethod = (*env)->GetMethodID(env, socketClass, "getImpl", "()Ljava/net/SocketImpl;");
    if (getImplMethod == NULL) {
        return -2;
    }

    // Invoke the private getImpl method to get the SocketImpl object
    jobject socketImpl = (*env)->CallObjectMethod(env, socket, getImplMethod);
    if (socketImpl == NULL) {
        return -3;
    }

    // Get the SocketImpl class
    jclass socketImplClass = (*env)->GetObjectClass(env, socketImpl);
    if (socketImplClass == NULL) {
        return -4;
    }

    // Get the getFileDescriptor method ID
    jmethodID getFileDescriptorMethod = (*env)->GetMethodID(env, socketImplClass, "getFileDescriptor", "()Ljava/io/FileDescriptor;");
    if (getFileDescriptorMethod == NULL) {
        return -5;
    }

    // Invoke the getFileDescriptor method to get the FileDescriptor object
    jobject fileDescriptor = (*env)->CallObjectMethod(env, socketImpl, getFileDescriptorMethod);
    if (fileDescriptor == NULL) {
        return -6;
    }

    // Get the int fd from the FileDescriptor object
    jclass fileDescriptorClass = (*env)->GetObjectClass(env, fileDescriptor);
    jfieldID fdField = (*env)->GetFieldID(env, fileDescriptorClass, "fd", "I");
    if (fdField == NULL) {
        return -7;
    }

    return (*env)->GetIntField(env, fileDescriptor, fdField);
}

// Generic method to set socket options
int setSocketOption(JNIEnv *env, jobject socket, int optionLevel, int optionName, const void *optionValue, socklen_t optionLength) {
    int socketDescriptor = getSocketDescriptor(env, socket);
    if (socketDescriptor < 0) {
        return socketDescriptor; // Propagate the error
    }

    int result = setsockopt(socketDescriptor, optionLevel, optionName, optionValue, optionLength);
    if (result < 0) {
        return -8; // Indicate failure
    }

    return 0; // Indicate success
}

// Method to set TCP_CONGESTION option
JNIEXPORT jint JNICALL Java_ecmwf_common_rmi_SocketOptions_setTCPCongestion(JNIEnv *env, jobject obj, jobject socket, jstring algorithm) {
    const char *algo = (*env)->GetStringUTFChars(env, algorithm, 0);
    int result = setSocketOption(env, socket, IPPROTO_TCP, TCP_CONGESTION, algo, strlen(algo) + 1);
    (*env)->ReleaseStringUTFChars(env, algorithm, algo);

    return result;
}

// Method to set SO_MAX_PACING_RATE option
JNIEXPORT jint JNICALL Java_ecmwf_common_rmi_SocketOptions_setSOMaxPacingRate(JNIEnv *env, jobject obj, jobject socket, jint pacingRate) {
    return setSocketOption(env, socket, SOL_SOCKET, SO_MAX_PACING_RATE, &pacingRate, sizeof(pacingRate));
}

// Method to set TCP_MAXSEG option
JNIEXPORT jint JNICALL Java_ecmwf_common_rmi_SocketOptions_setTCPMaxSegment(JNIEnv *env, jobject obj, jobject socket, jint maxSegmentSize) {
    return setSocketOption(env, socket, IPPROTO_TCP, TCP_MAXSEG, &maxSegmentSize, sizeof(maxSegmentSize));
}

// Method to set TCP_TIMESTAMP option
JNIEXPORT jint JNICALL Java_ecmwf_common_rmi_SocketOptions_setTCPTimeStamp(JNIEnv *env, jobject obj, jobject socket, jboolean enable) {
    int optionValue = enable ? 1 : 0;
    return setSocketOption(env, socket, IPPROTO_TCP, TCP_TIMESTAMP, &optionValue, sizeof(optionValue));
}

// Method to set TCP_WINDOW_CLAMP option
JNIEXPORT jint JNICALL Java_ecmwf_common_rmi_SocketOptions_setTCPWindowClamp(JNIEnv *env, jobject obj, jobject socket, jint windowSize) {
    return setSocketOption(env, socket, IPPROTO_TCP, TCP_WINDOW_CLAMP, &windowSize, sizeof(windowSize));
}

// Method to set TCP_KEEPALIVE_TIME option
JNIEXPORT jint JNICALL Java_ecmwf_common_rmi_SocketOptions_setTCPKeepAliveTime(JNIEnv *env, jobject obj, jobject socket, jint keepAliveTime) {
    return setSocketOption(env, socket, IPPROTO_TCP, TCP_KEEPIDLE, &keepAliveTime, sizeof(keepAliveTime));
}

// Method to set TCP_KEEPALIVE_INTVL option
JNIEXPORT jint JNICALL Java_ecmwf_common_rmi_SocketOptions_setTCPKeepAliveInterval(JNIEnv *env, jobject obj, jobject socket, jint keepAliveInterval) {
    return setSocketOption(env, socket, IPPROTO_TCP, TCP_KEEPINTVL, &keepAliveInterval, sizeof(keepAliveInterval));
}

// Method to set TCP_KEEPALIVE_PROBES option
JNIEXPORT jint JNICALL Java_ecmwf_common_rmi_SocketOptions_setTCPKeepAliveProbes(JNIEnv *env, jobject obj, jobject socket, jint keepAliveProbes) {
    return setSocketOption(env, socket, IPPROTO_TCP, TCP_KEEPCNT, &keepAliveProbes, sizeof(keepAliveProbes));
}

// Method to set TCP_LINGER option
JNIEXPORT jint JNICALL Java_ecmwf_common_rmi_SocketOptions_setTCPLinger(JNIEnv *env, jobject obj, jobject socket, jboolean enable, jint lingerTime) {
    struct linger lingerOption;
    lingerOption.l_onoff = enable ? 1 : 0;
    lingerOption.l_linger = lingerTime;
    return setSocketOption(env, socket, SOL_SOCKET, SO_LINGER, &lingerOption, sizeof(lingerOption));
}

// Method to set TCP_USER_TIMEOUT option
JNIEXPORT jint JNICALL Java_ecmwf_common_rmi_SocketOptions_setTCPUserTimeout(JNIEnv *env, jobject obj, jobject socket, jint userTimeout) {
    return setSocketOption(env, socket, IPPROTO_TCP, TCP_USER_TIMEOUT, &userTimeout, sizeof(userTimeout));
}

// Method to set TCP_QUICKACK option
JNIEXPORT jint JNICALL Java_ecmwf_common_rmi_SocketOptions_setTCPQuickAck(JNIEnv *env, jobject obj, jobject socket, jboolean enable) {
    int optionValue = enable ? 1 : 0;
    return setSocketOption(env, socket, IPPROTO_TCP, TCP_QUICKACK, &optionValue, sizeof(optionValue));
}

// Method to get socket descriptor
JNIEXPORT jint JNICALL Java_ecmwf_common_rmi_SocketOptions_getSocketDescriptor(JNIEnv *env, jobject obj, jobject socket) {
    return getSocketDescriptor(env, socket);
}


JNIEXPORT jstring JNICALL Java_ecmwf_common_rmi_SocketOptions_getSSOutput(
    JNIEnv *env, jobject obj, jint localPort, jint port) {
    
    char command[256];
    snprintf(command, sizeof(command),
             "ss -ntepi state established --inet-sockopt -O -H | grep -E ']:%d "
             ".*]:%d '",
             localPort, port);

    int pipefd[2];
    if (pipe(pipefd) == -1) {
        return (*env)->NewStringUTF(env, "exception:pipe");
    }

    pid_t pid = fork();
    if (pid < 0) {
        return (*env)->NewStringUTF(env, "exception:fork");
    }

    if (pid == 0) {  // Child process
        close(pipefd[0]); // Close read end
        dup2(pipefd[1], STDOUT_FILENO); // Redirect stdout to pipe
        close(pipefd[1]);

        execl("/bin/sh", "sh", "-c", command, (char *)NULL);
        _exit(127);  // Exec failed
    }

    // Parent process
    close(pipefd[1]);  // Close write end
    char buffer[4096];
    memset(buffer, 0, sizeof(buffer));

    fd_set set;
    struct timeval timeout;
    FD_ZERO(&set);
    FD_SET(pipefd[0], &set);
    timeout.tv_sec = 2;
    timeout.tv_usec = 0;

    int ready = select(pipefd[0] + 1, &set, NULL, NULL, &timeout);
    if (ready > 0) {
        read(pipefd[0], buffer, sizeof(buffer) - 1);
    } else {
        // Timeout occurred, clean up the child process
        kill(pid, SIGKILL);
        waitpid(pid, NULL, 0);
        close(pipefd[0]);
        return (*env)->NewStringUTF(env, "exception:timeout");
    }

    waitpid(pid, NULL, 0);  // Reap the child process
    close(pipefd[0]);

    // Charset conversion
    iconv_t cd = iconv_open("UTF-8", "ANSI_X3.4-1968");
    if (cd == (iconv_t)-1) {
        return (*env)->NewStringUTF(env, "exception:iconv_open");
    }

    size_t inSize = strlen(buffer);
    size_t outSize = sizeof(buffer);
    char *inBuf = buffer;
    char *outBuf = buffer;

    if (iconv(cd, &inBuf, &inSize, &outBuf, &outSize) == (size_t)-1) {
        iconv_close(cd);
        return (*env)->NewStringUTF(env, "exception:iconv_close");
    }

    iconv_close(cd);

    return (*env)->NewStringUTF(env, buffer);
}
