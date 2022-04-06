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
import org.apache.flink.kubernetes.operator.crd.FlinkDeployment;
import org.apache.flink.kubernetes.operator.crd.FlinkSessionJob;
import org.apache.flink.kubernetes.operator.utils.FlinkUtils;

import io.javaoperatorsdk.operator.api.reconciler.Context;

import java.util.Optional;

/** The context for {@link FlinkSessionJob} reconciler. */
public class SessionJobReconcilerContext {

    private final Configuration defaultConfig;

    private final Context ctx;

    private final FlinkSessionJob originalImmutableCopy;

    private final Optional<FlinkDeployment> sessionOpt;

    private final Optional<Configuration> effectiveConfigOpt;

    public SessionJobReconcilerContext(
            Configuration defaultConfig,
            Context ctx,
            FlinkSessionJob originalImmutableCopy,
            Optional<FlinkDeployment> sessionOpt) {
        this.defaultConfig = defaultConfig;
        this.ctx = ctx;
        this.originalImmutableCopy = originalImmutableCopy;
        this.sessionOpt = sessionOpt;
        this.effectiveConfigOpt =
                sessionOpt.map(session -> FlinkUtils.getEffectiveConfig(session, defaultConfig));
    }

    public Configuration getEffectiveConfig() {
        return effectiveConfigOpt.orElse(defaultConfig);
    }

    public Context getCtx() {
        return ctx;
    }

    public FlinkSessionJob getOriginalImmutableCopy() {
        return originalImmutableCopy;
    }

    public Optional<FlinkDeployment> getSession() {
        return sessionOpt;
    }
}
