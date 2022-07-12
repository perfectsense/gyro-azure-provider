/*
 * Copyright 2022, Brightspot, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gyro.azure.containerservice;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.containerservice.models.KubernetesCluster;
import gyro.azure.AzureFinder;
import gyro.core.Type;

/**
 * Query kubernetes-cluster.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    kubernetes-cluster: $(external-query azure::kubernetes-cluster {})
 */
@Type("kubernetes-cluster")
public class KubernetesClusterFinder extends AzureFinder<KubernetesCluster, KubernetesClusterResource> {

    private String id;

    /**
     * The id of the kubernetes cluster.
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    protected List<KubernetesCluster> findAllAzure(AzureResourceManager client) {
        return client.kubernetesClusters().list().stream().collect(Collectors.toList());
    }

    @Override
    protected List<KubernetesCluster> findAzure(
        AzureResourceManager client, Map<String, String> filters) {

        List<KubernetesCluster> clusters= new ArrayList<>();

        KubernetesCluster cluster = client.kubernetesClusters().getById(filters.get("id"));

        if (cluster != null) {
            clusters.add(cluster);
        }

        return clusters;
    }
}
