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

package org.apache.flink.streaming.runtime.io;

import org.apache.flink.annotation.Internal;
import org.apache.flink.runtime.checkpoint.CheckpointException;
import org.apache.flink.runtime.checkpoint.CheckpointFailureReason;
import org.apache.flink.runtime.io.network.api.CancelCheckpointMarker;
import org.apache.flink.runtime.io.network.api.CheckpointBarrier;
import org.apache.flink.runtime.jobgraph.tasks.AbstractInvokable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;

import java.io.IOException;

/**
 * {@link CheckpointBarrierAligner} keep tracks of received {@link CheckpointBarrier} on given
 * channels and controls the alignment, by deciding which channels should be blocked and when to
 * release blocked channels.
 */
@Internal
public class CheckpointBarrierAligner extends CheckpointBarrierHandler {

	private static final Logger LOG = LoggerFactory.getLogger(CheckpointBarrierAligner.class);

	/** Flags that indicate whether a channel is currently blocked/buffered. */
	private final boolean[] blockedChannels;

	/** The total number of channels that this buffer handles data from. */
	private final int totalNumberOfInputChannels;

	private final String taskName;

	/** The ID of the checkpoint for which we expect barriers. */
	private long currentCheckpointId = -1L;

	/**
	 * The number of received barriers (= number of blocked/buffered channels) IMPORTANT: A canceled
	 * checkpoint must always have 0 barriers.
	 */
	private int numBarriersReceived;

	/** The number of already closed channels. */
	private int numClosedChannels;

	/** The timestamp as in {@link System#nanoTime()} at which the last alignment started. */
	private long startOfAlignmentTimestamp;

	/** The time (in nanoseconds) that the latest alignment took. */
	private long latestAlignmentDurationNanos;

	CheckpointBarrierAligner(
			int totalNumberOfInputChannels,
			String taskName,
			@Nullable AbstractInvokable toNotifyOnCheckpoint) {
		super(toNotifyOnCheckpoint);
		this.totalNumberOfInputChannels = totalNumberOfInputChannels;
		this.taskName = taskName;

		this.blockedChannels = new boolean[totalNumberOfInputChannels];
	}

	@Override
	public void releaseBlocksAndResetBarriers() throws IOException {
		LOG.debug("{}: End of stream alignment, feeding buffered data back.", taskName);

		for (int i = 0; i < blockedChannels.length; i++) {
			blockedChannels[i] = false;
		}

		// the next barrier that comes must assume it is the first
		numBarriersReceived = 0;

		if (startOfAlignmentTimestamp > 0) {
			latestAlignmentDurationNanos = System.nanoTime() - startOfAlignmentTimestamp;
			startOfAlignmentTimestamp = 0;
		}
	}

	@Override
	public boolean isBlocked(int channelIndex) {
		return blockedChannels[channelIndex];
	}

	@Override
	public boolean processBarrier(CheckpointBarrier receivedBarrier, int channelIndex, long bufferedBytes) throws Exception {
		final long barrierId = receivedBarrier.getId();

		// fast path for single channel cases
		//如果是只有一个输入流
		if (totalNumberOfInputChannels == 1) {
			if (barrierId > currentCheckpointId) {
				// new checkpoint
				currentCheckpointId = barrierId;
				notifyCheckpoint(receivedBarrier, bufferedBytes, latestAlignmentDurationNanos);
			}
			return false;
		}

		boolean checkpointAborted = false;

		//如果是多个输入流

		// -- general code path for multiple input channels --
		//如果收到的 Barrier 大于 0
		if (numBarriersReceived > 0) {
			// this is only true if some alignment is already progress and was not canceled

			if (barrierId == currentCheckpointId) {
				// regular case
				onBarrier(channelIndex);
			}
			//todo：这里会不会出现这种情况（checkpoint 时间间隔设置的较短，但是状态较大，一次 checkpoint 需要的时间比较久，
			// checkpoint 处于未完成状态，但总是接收到新的 checkpoint 请求，这样就一直处于 checkpoint 状态，那么就会导致从没有生成
			// 完整的 checkpoint）？？？
			else if (barrierId > currentCheckpointId) {
				// we did not complete the current checkpoint, another started before
				//当前的 checkpoint 还没有完成呢，你咋又来了一个新的 checkpoint 请求
				LOG.warn("{}: Received checkpoint barrier for checkpoint {} before completing current checkpoint {}. " +
						"Skipping current checkpoint.",
					taskName,
					barrierId,
					currentCheckpointId);

				// let the task know we are not completing this
				//然后通知 task 会取消当前的 checkpoint，因为接收到了一个新的 checkpoint
				notifyAbort(currentCheckpointId,
					new CheckpointException(
						"Barrier id: " + barrierId,
						CheckpointFailureReason.CHECKPOINT_DECLINED_SUBSUMED));

				// abort the current checkpoint
				//丢弃当前的 checkpoint
				releaseBlocksAndResetBarriers();
				checkpointAborted = true;

				// begin a the new checkpoint
				//开始一个新的 checkpoint
				beginNewAlignment(barrierId, channelIndex);
			}
			else {
				// ignore trailing barrier from an earlier checkpoint (obsolete now)
				return false;
			}
		}
		//之前如果还没有收到 Barrier，新来的 Barrier id 大于之前的 Barrier id，那么这意味着这是一个新的 checkpoint 请求的第一个过来的 barrier
		else if (barrierId > currentCheckpointId) {
			// first barrier of a new checkpoint
			beginNewAlignment(barrierId, channelIndex);
		}
		else {
			// either the current checkpoint was canceled (numBarriers == 0) or
			// this barrier is from an old subsumed checkpoint
			return false;
		}

		// check if we have all barriers - since canceled checkpoints always have zero barriers
		// this can only happen on a non canceled checkpoint
		//检查是否所有的 barrier 都到齐了（和输入流都个数一致）
		if (numBarriersReceived + numClosedChannels == totalNumberOfInputChannels) {
			// actually trigger checkpoint
			if (LOG.isDebugEnabled()) {
				LOG.debug("{}: Received all barriers, triggering checkpoint {} at {}.",
					taskName,
					receivedBarrier.getId(),
					receivedBarrier.getTimestamp());
			}

			releaseBlocksAndResetBarriers();
			//开始执行 checkpoint
			notifyCheckpoint(receivedBarrier, bufferedBytes, latestAlignmentDurationNanos);
			return true;
		}
		return checkpointAborted;
	}

	protected void beginNewAlignment(long checkpointId, int channelIndex) throws IOException {
		currentCheckpointId = checkpointId;
		onBarrier(channelIndex);

		startOfAlignmentTimestamp = System.nanoTime();

		if (LOG.isDebugEnabled()) {
			LOG.debug("{}: Starting stream alignment for checkpoint {}.", taskName, checkpointId);
		}
	}

	/**
	 * 阻塞该输入流通道，从该通道接收 barrier
	 *
	 * Blocks the given channel index, from which a barrier has been received.
	 *
	 * @param channelIndex The channel index to block.
	 */
	protected void onBarrier(int channelIndex) throws IOException {
		if (!blockedChannels[channelIndex]) {
			blockedChannels[channelIndex] = true;

			numBarriersReceived++;

			if (LOG.isDebugEnabled()) {
				LOG.debug("{}: Received barrier from channel {}.", taskName, channelIndex);
			}
		}
		else {
			throw new IOException("Stream corrupt: Repeated barrier for same checkpoint on input " + channelIndex);
		}
	}

	@Override
	public boolean processCancellationBarrier(CancelCheckpointMarker cancelBarrier) throws Exception {
		final long barrierId = cancelBarrier.getCheckpointId();

		// fast path for single channel cases
		if (totalNumberOfInputChannels == 1) {
			if (barrierId > currentCheckpointId) {
				// new checkpoint
				currentCheckpointId = barrierId;
				notifyAbortOnCancellationBarrier(barrierId);
			}
			return false;
		}

		// -- general code path for multiple input channels --

		if (numBarriersReceived > 0) {
			// this is only true if some alignment is in progress and nothing was canceled

			if (barrierId == currentCheckpointId) {
				// cancel this alignment
				if (LOG.isDebugEnabled()) {
					LOG.debug("{}: Checkpoint {} canceled, aborting alignment.", taskName, barrierId);
				}

				releaseBlocksAndResetBarriers();
				notifyAbortOnCancellationBarrier(barrierId);
				return true;
			}
			else if (barrierId > currentCheckpointId) {
				// we canceled the next which also cancels the current
				LOG.warn("{}: Received cancellation barrier for checkpoint {} before completing current checkpoint {}. " +
						"Skipping current checkpoint.",
					taskName,
					barrierId,
					currentCheckpointId);

				// this stops the current alignment
				releaseBlocksAndResetBarriers();

				// the next checkpoint starts as canceled
				currentCheckpointId = barrierId;
				startOfAlignmentTimestamp = 0L;
				latestAlignmentDurationNanos = 0L;

				notifyAbortOnCancellationBarrier(barrierId);
				return true;
			}

			// else: ignore trailing (cancellation) barrier from an earlier checkpoint (obsolete now)

		}
		else if (barrierId > currentCheckpointId) {
			// first barrier of a new checkpoint is directly a cancellation

			// by setting the currentCheckpointId to this checkpoint while keeping the numBarriers
			// at zero means that no checkpoint barrier can start a new alignment
			currentCheckpointId = barrierId;

			startOfAlignmentTimestamp = 0L;
			latestAlignmentDurationNanos = 0L;

			if (LOG.isDebugEnabled()) {
				LOG.debug("{}: Checkpoint {} canceled, skipping alignment.", taskName, barrierId);
			}

			notifyAbortOnCancellationBarrier(barrierId);
			return false;
		}

		// else: trailing barrier from either
		//   - a previous (subsumed) checkpoint
		//   - the current checkpoint if it was already canceled
		return false;
	}

	@Override
	public boolean processEndOfPartition() throws Exception {
		numClosedChannels++;

		if (numBarriersReceived > 0) {
			// let the task know we skip a checkpoint
			notifyAbort(currentCheckpointId,
				new CheckpointException(CheckpointFailureReason.CHECKPOINT_DECLINED_INPUT_END_OF_STREAM));
			// no chance to complete this checkpoint
			releaseBlocksAndResetBarriers();
			return true;
		}
		return false;
	}

	@Override
	public long getLatestCheckpointId() {
		return currentCheckpointId;
	}

	@Override
	public long getAlignmentDurationNanos() {
		if (startOfAlignmentTimestamp <= 0) {
			return latestAlignmentDurationNanos;
		} else {
			return System.nanoTime() - startOfAlignmentTimestamp;
		}
	}

	@Override
	public String toString() {
		return String.format("%s: last checkpoint: %d, current barriers: %d, closed channels: %d",
			taskName,
			currentCheckpointId,
			numBarriersReceived,
			numClosedChannels);
	}

	@Override
	public void checkpointSizeLimitExceeded(long maxBufferedBytes) throws Exception {
		releaseBlocksAndResetBarriers();
		notifyAbort(currentCheckpointId,
			new CheckpointException(
				"Max buffered bytes: " + maxBufferedBytes,
				CheckpointFailureReason.CHECKPOINT_DECLINED_ALIGNMENT_LIMIT_EXCEEDED));
	}
}
