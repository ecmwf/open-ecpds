#!/bin/sh -x
gcc -shared -O2 -D_GNU_SOURCE -D_FILE_OFFSET_BITS=64 -o libsocketoptions.so \
	-I/usr/lib/jvm/graalvm-community-openjdk-22.0.1+8.1/include \
	-I/usr/lib/jvm/graalvm-community-openjdk-22.0.1+8.1/include/linux \
	-fPIC SocketOptions.c
