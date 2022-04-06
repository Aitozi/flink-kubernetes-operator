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

package org.apache.flink.kubernetes.operator.reconciler.deployment;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.kubernetes.operator.crd.FlinkDeployment;

import io.javaoperatorsdk.operator.api.reconciler.Context;

/** The context for {@link FlinkDeployment} reconciler. */
public class DeploymentReconcilerContext {

    private final Configuration effectiveConfig;

    private final Context ctx;

    private final FlinkDeployment originalImmutableCopy;

    public DeploymentReconcilerContext(
            Context ctx, Configuration effectiveConfig, FlinkDeployment originalImmutableCopy) {
        this.ctx = ctx;
        this.effectiveConfig = effectiveConfig;
        this.originalImmutableCopy = originalImmutableCopy;
    }

    public Configuration getEffectiveConfig() {
        return effectiveConfig;
    }

    public Context getCtx() {
        return ctx;
    }

    public FlinkDeployment getOriginalImmutableCopy() {
        return originalImmutableCopy;
    }
}
