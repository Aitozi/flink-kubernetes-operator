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

import org.apache.flink.configuration.Configuration;
import org.apache.flink.kubernetes.operator.TestUtils;
import org.apache.flink.kubernetes.operator.TestingFlinkService;
import org.apache.flink.kubernetes.operator.config.FlinkOperatorConfiguration;
import org.apache.flink.kubernetes.operator.crd.FlinkDeployment;
import org.apache.flink.kubernetes.operator.crd.FlinkSessionJob;
import org.apache.flink.kubernetes.operator.crd.status.JobManagerDeploymentStatus;
import org.apache.flink.kubernetes.operator.reconciler.ReconciliationUtils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.function.Supplier;

/** Tests for {@link FlinkSessionJobReconciler}. */
public class FlinkSessionJobReconcilerTest {

    private final FlinkOperatorConfiguration operatorConfiguration =
            FlinkOperatorConfiguration.fromConfiguration(new Configuration());

    @Test
    public void testSubmitAndCleanUp() throws Exception {
        TestingFlinkService flinkService = new TestingFlinkService();
        FlinkSessionJob sessionJob = TestUtils.buildSessionJob();

        FlinkSessionJobReconciler reconciler =
                new FlinkSessionJobReconciler(null, flinkService, operatorConfiguration);
        reconciler.reconcile(
                sessionJob,
                new SessionJobReconcilerContext(
                        new Configuration(),
                        TestUtils.createEmptyContext(),
                        ReconciliationUtils.clone(sessionJob),
                        Optional.empty()));
        Assertions.assertEquals(0, flinkService.listJobs().size());
        reconciler.reconcile(
                sessionJob,
                new SessionJobReconcilerContext(
                        new Configuration(),
                        TestUtils.createEmptyContext(),
                        ReconciliationUtils.clone(sessionJob),
                        Optional.of(
                                get(
                                        () -> {
                                            var session = TestUtils.buildSessionCluster();
                                            session.getStatus()
                                                    .setJobManagerDeploymentStatus(
                                                            JobManagerDeploymentStatus.MISSING);
                                            return session;
                                        }))));

        Assertions.assertEquals(0, flinkService.listJobs().size());
        reconciler.reconcile(
                sessionJob,
                new SessionJobReconcilerContext(
                        new Configuration(),
                        TestUtils.createEmptyContext(),
                        ReconciliationUtils.clone(sessionJob),
                        Optional.of(
                                get(
                                        () -> {
                                            var session = TestUtils.buildSessionCluster();
                                            session.getStatus()
                                                    .setJobManagerDeploymentStatus(
                                                            JobManagerDeploymentStatus.READY);
                                            return session;
                                        }))));
        Assertions.assertEquals(1, flinkService.listJobs().size());
        // clean up
        sessionJob
                .getStatus()
                .getJobStatus()
                .setJobId(flinkService.listJobs().get(0).f1.getJobId().toHexString());
        reconciler.cleanup(
                sessionJob,
                new SessionJobReconcilerContext(
                        new Configuration(),
                        TestUtils.createEmptyContext(),
                        ReconciliationUtils.clone(sessionJob),
                        Optional.of(
                                get(
                                        () -> {
                                            var session = TestUtils.buildSessionCluster();
                                            session.getStatus()
                                                    .setJobManagerDeploymentStatus(
                                                            JobManagerDeploymentStatus.READY);
                                            return session;
                                        }))));
        Assertions.assertEquals(0, flinkService.listJobs().size());
    }

    private FlinkDeployment get(Supplier<FlinkDeployment> supplier) {
        return supplier.get();
    }
}
