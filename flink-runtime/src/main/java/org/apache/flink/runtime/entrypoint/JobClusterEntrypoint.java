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

package org.apache.flink.runtime.entrypoint;

import org.apache.flink.api.common.cache.DistributedCache;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.core.fs.Path;
import org.apache.flink.runtime.blob.BlobClient;
import org.apache.flink.runtime.blob.BlobServer;
import org.apache.flink.runtime.blob.TransientBlobService;
import org.apache.flink.runtime.client.ClientUtils;
import org.apache.flink.runtime.clusterframework.ApplicationStatus;
import org.apache.flink.runtime.concurrent.ScheduledExecutor;
import org.apache.flink.runtime.dispatcher.ArchivedExecutionGraphStore;
import org.apache.flink.runtime.dispatcher.Dispatcher;
import org.apache.flink.runtime.dispatcher.DispatcherGateway;
import org.apache.flink.runtime.dispatcher.HistoryServerArchivist;
import org.apache.flink.runtime.dispatcher.MemoryArchivedExecutionGraphStore;
import org.apache.flink.runtime.dispatcher.MiniDispatcher;
import org.apache.flink.runtime.heartbeat.HeartbeatServices;
import org.apache.flink.runtime.highavailability.HighAvailabilityServices;
import org.apache.flink.runtime.jobgraph.JobGraph;
import org.apache.flink.runtime.jobmaster.MiniDispatcherRestEndpoint;
import org.apache.flink.runtime.leaderelection.LeaderElectionService;
import org.apache.flink.runtime.metrics.groups.JobManagerMetricGroup;
import org.apache.flink.runtime.resourcemanager.ResourceManagerGateway;
import org.apache.flink.runtime.rest.RestServerEndpointConfiguration;
import org.apache.flink.runtime.rest.handler.RestHandlerConfiguration;
import org.apache.flink.runtime.rpc.FatalErrorHandler;
import org.apache.flink.runtime.rpc.LeaderShipLostHandler;
import org.apache.flink.runtime.rpc.RpcService;
import org.apache.flink.runtime.webmonitor.retriever.LeaderGatewayRetriever;
import org.apache.flink.runtime.webmonitor.retriever.MetricQueryServiceRetriever;
import org.apache.flink.util.FlinkException;

import javax.annotation.Nullable;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 *
 * Base class for per-job cluster entry points.
 */
public abstract class JobClusterEntrypoint extends ClusterEntrypoint {

	public JobClusterEntrypoint(Configuration configuration) {
		super(configuration);
	}

	@Override
	protected MiniDispatcherRestEndpoint createRestEndpoint(
			Configuration configuration,
			LeaderGatewayRetriever<DispatcherGateway> dispatcherGatewayRetriever,
			LeaderGatewayRetriever<ResourceManagerGateway> resourceManagerGatewayRetriever,
			TransientBlobService transientBlobService,
			Executor executor,
			MetricQueryServiceRetriever metricQueryServiceRetriever,
			LeaderElectionService leaderElectionService) throws Exception {
		final RestHandlerConfiguration restHandlerConfiguration = RestHandlerConfiguration.fromConfiguration(configuration);

		return new MiniDispatcherRestEndpoint(
			RestServerEndpointConfiguration.fromConfiguration(configuration),
			dispatcherGatewayRetriever,
			configuration,
			restHandlerConfiguration,
			resourceManagerGatewayRetriever,
			transientBlobService,
			executor,
			metricQueryServiceRetriever,
			leaderElectionService,
			this);
	}

	@Override
	protected ArchivedExecutionGraphStore createSerializableExecutionGraphStore(
			Configuration configuration,
			ScheduledExecutor scheduledExecutor) {
		return new MemoryArchivedExecutionGraphStore();
	}

	@Override
	protected Dispatcher createDispatcher(
			Configuration configuration, RpcService rpcService, HighAvailabilityServices highAvailabilityServices,
			ResourceManagerGateway resourceManagerGateway, BlobServer blobServer, HeartbeatServices heartbeatServices,
			JobManagerMetricGroup jobManagerMetricGroup, @Nullable String metricQueryServicePath,
			ArchivedExecutionGraphStore archivedExecutionGraphStore, FatalErrorHandler fatalErrorHandler,
			@Nullable String restAddress, HistoryServerArchivist historyServerArchivist, LeaderShipLostHandler leaderShipLostHandler) throws Exception {

		final JobGraph jobGraph = retrieveJobGraph(configuration);

		final String executionModeValue = configuration.getString(EXECUTION_MODE);

		final ExecutionMode executionMode = ExecutionMode.valueOf(executionModeValue);

		final MiniDispatcher dispatcher = new MiniDispatcher(rpcService, Dispatcher.DISPATCHER_NAME, configuration, highAvailabilityServices,
			resourceManagerGateway, blobServer, heartbeatServices, jobManagerMetricGroup, metricQueryServicePath,
			archivedExecutionGraphStore, Dispatcher.DefaultJobManagerRunnerFactory.INSTANCE, fatalErrorHandler,
			restAddress, historyServerArchivist, jobGraph, executionMode, leaderShipLostHandler);

		registerShutdownActions(dispatcher.getJobTerminationFuture());

		// Only in detach mode for perjob, need to upload user artifacts to the blob server
		// Do not need to upload user libjars, as it has been processed with user jar files together
		if (executionMode == ExecutionMode.DETACHED && jobGraph.getUserArtifacts() != null) {
			final InetSocketAddress address = new InetSocketAddress(blobServer.getPort());
			List<Tuple2<String, Path>> userArtifacts = new ArrayList<>();
			for (Map.Entry<String, DistributedCache.DistributedCacheEntry> entry : jobGraph.getUserArtifacts().entrySet()) {
				if (!new Path(entry.getValue().filePath).getFileSystem().isDistributedFS()) {
					userArtifacts.add(new Tuple2<>(entry.getKey(), new Path(entry.getKey())));
				} else {
					userArtifacts.add(new Tuple2<>(entry.getKey(), new Path(entry.getValue().filePath)));
				}
			}
			try {
				ClientUtils.uploadJobGraphFiles(jobGraph, Collections.emptyList(), userArtifacts, () -> new BlobClient(address, configuration));
			} catch (FlinkException e) {
				throw new FlinkException("Failed to upload artifacts.", e);
			}
		}

		return dispatcher;
	}

	protected abstract JobGraph retrieveJobGraph(Configuration configuration) throws FlinkException;

	protected abstract void registerShutdownActions(CompletableFuture<ApplicationStatus> terminationFuture);
}
