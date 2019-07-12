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

package org.apache.flink.runtime.rpc;

/**
 * Handler for fatal errors.
 *
 * 严重错误的处理器
 */
public interface FatalErrorHandler {

	/**
	 * Being called when a fatal error occurs.
	 *
	 * 当出现严重错误的时候就会回调该方法
	 *
	 * 这个调用永远不会阻塞，因为它可能是从 RpcEndpoint 的主线程中调用的
	 *
	 * <p>IMPORTANT: This call should never be blocking since it might be called from within
	 * the main thread of an {@link RpcEndpoint}.
	 *
	 * @param exception cause
	 */
	void onFatalError(Throwable exception);
}
