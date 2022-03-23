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

package org.apache.flink.kubernetes.operator.crd.sessionjob.spec;

import org.apache.flink.annotation.Experimental;
import org.apache.flink.kubernetes.operator.crd.spec.JobSpec;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Spec that describes a Flink session job. */
@Experimental
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlinkSessionJobSpec {

    /**
     * The cluster id of the target session cluster. When deployed using the operator the cluster id
     * is the name of the deployment.
     */
    private String clusterId;

    /** A specification of a job . */
    private JobSpec job;
}
