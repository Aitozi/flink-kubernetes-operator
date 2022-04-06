/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flink.kubernetes.operator.reconciler.sessionjob;

import org.apache.flink.api.common.JobID;
import org.apache.flink.kubernetes.operator.config.FlinkOperatorConfiguration;
import org.apache.flink.kubernetes.operator.crd.FlinkSessionJob;
import org.apache.flink.kubernetes.operator.crd.spec.FlinkSessionJobSpec;
import org.apache.flink.kubernetes.operator.crd.status.JobManagerDeploymentStatus;
import org.apache.flink.kubernetes.operator.reconciler.Reconciler;
import org.apache.flink.kubernetes.operator.reconciler.ReconciliationUtils;
import org.apache.flink.kubernetes.operator.reconciler.deployment.ApplicationReconciler;
import org.apache.flink.kubernetes.operator.service.FlinkService;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.javaoperatorsdk.operator.api.reconciler.DeleteControl;
import io.javaoperatorsdk.operator.api.reconciler.UpdateControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** The reconciler for the {@link FlinkSessionJob}. */
public class FlinkSessionJobReconciler
        implements Reconciler<FlinkSessionJob, SessionJobReconcilerContext> {

    private static final Logger LOG = LoggerFactory.getLogger(ApplicationReconciler.class);

    private final FlinkOperatorConfiguration operatorConfiguration;
    private final KubernetesClient kubernetesClient;
    private final FlinkService flinkService;

    public FlinkSessionJobReconciler(
            KubernetesClient kubernetesClient,
            FlinkService flinkService,
            FlinkOperatorConfiguration operatorConfiguration) {
        this.kubernetesClient = kubernetesClient;
        this.flinkService = flinkService;
        this.operatorConfiguration = operatorConfiguration;
    }

    @Override
    public UpdateControl<FlinkSessionJob> reconcile(
            FlinkSessionJob flinkSessionJob, SessionJobReconcilerContext context) throws Exception {

        FlinkSessionJobSpec lastReconciledSpec =
                flinkSessionJob.getStatus().getReconciliationStatus().getLastReconciledSpec();

        if (lastReconciledSpec == null) {
            submitFlinkJob(flinkSessionJob, context);
            return ReconciliationUtils.toUpdateControl(
                            context.getOriginalImmutableCopy(), flinkSessionJob)
                    .rescheduleAfter(operatorConfiguration.getReconcileInterval().toMillis());
        }

        boolean specChanged = !flinkSessionJob.getSpec().equals(lastReconciledSpec);

        if (specChanged) {
            // TODO reconcile other spec change.
            LOG.info("Other spec change have not supported");
        }
        return ReconciliationUtils.toUpdateControl(
                        context.getOriginalImmutableCopy(), flinkSessionJob)
                .rescheduleAfter(operatorConfiguration.getReconcileInterval().toMillis());
    }

    @Override
    public DeleteControl cleanup(FlinkSessionJob sessionJob, SessionJobReconcilerContext context) {
        var sessionOpt = context.getSession();

        if (sessionOpt.isPresent()) {
            String jobID = sessionJob.getStatus().getJobStatus().getJobId();
            if (jobID != null) {
                try {
                    flinkService.cancelSessionJob(
                            JobID.fromHexString(jobID), context.getEffectiveConfig());
                } catch (Exception e) {
                    LOG.error("Failed to cancel job.", e);
                }
            }
        } else {
            LOG.info("Session cluster deployment not available");
        }
        return DeleteControl.defaultDelete();
    }

    private void submitFlinkJob(FlinkSessionJob sessionJob, SessionJobReconcilerContext context)
            throws Exception {
        var sessionOpt = context.getSession();
        if (sessionOpt.isPresent()) {
            var session = sessionOpt.get();
            var jobDeploymentStatus = session.getStatus().getJobManagerDeploymentStatus();
            if (jobDeploymentStatus == JobManagerDeploymentStatus.READY) {
                flinkService.submitJobToSessionCluster(sessionJob, context.getEffectiveConfig());
                ReconciliationUtils.updateForSpecReconciliationSuccess(sessionJob);
            } else {
                LOG.info(
                        "Session cluster deployment is in {} status, not ready for serve",
                        jobDeploymentStatus);
            }
        } else {
            LOG.info("Session cluster deployment is not found");
        }
    }
}
