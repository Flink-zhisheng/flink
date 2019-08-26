/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flink.runtime.checkpoint;

import org.apache.flink.annotation.Internal;

/**
 * Policy for whether checkpoints are retained after a job terminates.
 * 作业终止后是否保留 checkpoint 的策略
 */
@Internal
public enum CheckpointRetentionPolicy {

	/** Checkpoints should be retained on cancellation and failure.
	 *  在作业取消和失败时应该保留 Checkpoint
	 * */
	RETAIN_ON_CANCELLATION,

	/** Checkpoints should be retained on failure, but not on cancellation.
	 *  Checkpoint 应该在失败时保留，而不是在取消时保留
	 * */
	RETAIN_ON_FAILURE,

	/** Checkpoints should always be cleaned up when an application reaches a terminal state.
	 *  当应用程序到达终端状态时，清理 Checkpoint
	 * */
	NEVER_RETAIN_AFTER_TERMINATION;
}
