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

package org.apache.flink.kubernetes.operator.crd.status;

import org.apache.flink.annotation.Experimental;
import org.apache.flink.kubernetes.operator.crd.spec.FlinkDeploymentSpec;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Status of the last reconcile step for the deployment. */
@Experimental
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class ReconciliationStatus {
    /** True if last reconciliation step was successful. */
    private boolean success;

    /** If success == false, error information about the reconciliation failure. */
    private String error;

    /**
     * Last reconciled deployment spec. Used to decide whether further reconciliation steps are
     * necessary.
     */
    private FlinkDeploymentSpec lastReconciledSpec;
}
