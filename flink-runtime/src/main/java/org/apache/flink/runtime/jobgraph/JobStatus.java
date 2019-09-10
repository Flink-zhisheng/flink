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

package org.apache.flink.runtime.jobgraph;

/**
 * Possible states of a job once it has been accepted by the job manager.
 *
 * job 的状态（JobManager 调度该 Job 的时候该 Job 的状态）
 */
public enum JobStatus {

	/** Job is newly created, no task has started to run. */
	CREATED(TerminalState.NON_TERMINAL),

	/** Some tasks are scheduled or running, some may be pending, some may be finished. */
	//job 为 RUNNING 时，其实并不代表着这个 Job 就真的运行起来了
	RUNNING(TerminalState.NON_TERMINAL),

	/** The job has failed and is currently waiting for the cleanup to complete. */
	FAILING(TerminalState.NON_TERMINAL),

	/** The job has failed with a non-recoverable task failure. */
	FAILED(TerminalState.GLOBALLY),

	/** Job is being cancelled. */
	CANCELLING(TerminalState.NON_TERMINAL),

	/** Job has been cancelled. */
	CANCELED(TerminalState.GLOBALLY),

	/** All of the job's tasks have successfully finished. */
	FINISHED(TerminalState.GLOBALLY),

	/** The job is currently undergoing a reset and total restart. */
	RESTARTING(TerminalState.NON_TERMINAL),

	/**
	 * The job has been suspended which means that it has been stopped but not been removed from a
	 * potential HA job store.
	 */
	SUSPENDED(TerminalState.LOCALLY),

	/** The job is currently reconciling and waits for task execution report to recover state. */
	RECONCILING(TerminalState.NON_TERMINAL);

	// --------------------------------------------------------------------------------------------

	private enum TerminalState {
		NON_TERMINAL,
		LOCALLY,
		GLOBALLY
	}

	private final TerminalState terminalState;

	JobStatus(TerminalState terminalState) {
		this.terminalState = terminalState;
	}

	/**
	 * Checks whether this state is <i>globally terminal</i>. A globally terminal job
	 * is complete and cannot fail any more and will not be restarted or recovered by another
	 * standby master node.
	 *
	 * <p>When a globally terminal state has been reached, all recovery data for the job is
	 * dropped from the high-availability services.
	 *
	 * @return True, if this job status is globally terminal, false otherwise.
	 */
	public boolean isGloballyTerminalState() {
		return terminalState == TerminalState.GLOBALLY;
	}

	/**
	 * Checks whether this state is <i>locally terminal</i>. Locally terminal refers to the
	 * state of a job's execution graph within an executing JobManager. If the execution graph
	 * is locally terminal, the JobManager will not continue executing or recovering the job.
	 *
	 * <p>The only state that is locally terminal, but not globally terminal is {@link #SUSPENDED},
	 * which is typically entered when the executing JobManager looses its leader status.
	 *
	 * @return True, if this job status is terminal, false otherwise.
	 */
	public boolean isTerminalState() {
		return terminalState != TerminalState.NON_TERMINAL;
	}
}


