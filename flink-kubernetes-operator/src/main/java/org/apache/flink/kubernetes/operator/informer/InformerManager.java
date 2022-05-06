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

package org.apache.flink.kubernetes.operator.informer;

import org.apache.flink.kubernetes.operator.crd.FlinkSessionJob;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.informers.SharedIndexInformer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/** InformerManager. */
public class InformerManager {
    private static final String CLUSTER_ID_INDEX = "clusterId_index";
    private static final String ALL_NAMESPACE = "allNamespace";

    private final KubernetesClient kubernetesClient;
    private final Set<String> effectiveNamespaces;

    private final Map<String, SharedIndexInformer<FlinkSessionJob>> sessionJobInformers;

    public InformerManager(Set<String> effectiveNamespaces, KubernetesClient kubernetesClient) {
        this.kubernetesClient = kubernetesClient;
        this.effectiveNamespaces = effectiveNamespaces;
        this.sessionJobInformers = createInformers();
    }

    public SharedIndexInformer<FlinkSessionJob> getInformer() {
        return sessionJobInformers.get(ALL_NAMESPACE);
    }

    public SharedIndexInformer<FlinkSessionJob> getInformer(String namespace) {
        return sessionJobInformers.get(namespace);
    }

    /**
     * Create informers for session job to build indexer for cluster to session job relations.
     *
     * @return The different namespace's index informer.
     */
    private Map<String, SharedIndexInformer<FlinkSessionJob>> createInformers() {
        if (effectiveNamespaces.isEmpty()) {
            return Map.of(
                    ALL_NAMESPACE,
                    kubernetesClient
                            .resources(FlinkSessionJob.class)
                            .inAnyNamespace()
                            .withIndexers(clusterToSessionJobIndexer())
                            .inform());
        } else {
            var informers = new HashMap<String, SharedIndexInformer<FlinkSessionJob>>();
            for (String effectiveNamespace : effectiveNamespaces) {
                informers.put(
                        effectiveNamespace,
                        kubernetesClient
                                .resources(FlinkSessionJob.class)
                                .inNamespace(effectiveNamespace)
                                .withIndexers(clusterToSessionJobIndexer())
                                .inform());
            }
            return informers;
        }
    }

    private Map<String, Function<FlinkSessionJob, List<String>>> clusterToSessionJobIndexer() {
        return Map.of(
                CLUSTER_ID_INDEX, sessionJob -> List.of(sessionJob.getSpec().getDeploymentName()));
    }
}
