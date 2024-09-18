/*
 * Copyright 2019-present HiveMQ GmbH
 *
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
 */
package com.hivemq.bootstrap;

import com.hivemq.extension.sdk.api.annotations.NotNull;

import java.io.File;

/**
 * This class is responsible for all logging bootstrapping. This is only needed
 * at the very beginning of HiveMQs lifecycle and before bootstrapping other
 * resources.
 * 
 * This is a temporary fix before a solution is found to allow redirecting
 * hivemq internal logging to log4j.
 */
public class LoggingBootstrap {

	public static void prepareLogging() {
	}

	public static void initLogging(final @NotNull File configFolder) {
	}

	public static void resetLogging() {
	}

	public static void addLoglevelModifiers() {
	}
}