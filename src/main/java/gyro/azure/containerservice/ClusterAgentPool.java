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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.azure.resourcemanager.containerservice.models.KubernetesClusterAgentPool;
import gyro.azure.Copyable;
import gyro.azure.network.NetworkResource;
import gyro.azure.network.SubnetResource;
import gyro.core.resource.Diffable;
import gyro.core.resource.Output;
import gyro.core.resource.Updatable;
import gyro.core.validation.DependsOn;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;

public class ClusterAgentPool extends Diffable implements Copyable<KubernetesClusterAgentPool> {

    private String name;
    private String size;
    private Integer count;
    private List<Integer> availabilityZones;
    private Map<String, String> tags;
    private String mode;
    private Boolean autoScalingEnabled;
    private String kubeletDiskType;
    private Integer maximumNodeSize;
    private Integer minimumNodeSize;
    private Integer maximumPodsPerNode;
    private NetworkResource network;
    private String subnet;
    private Map<String, String> nodeLabels;
    private Integer nodeSize;
    private List<String> nodeTaints;
    private Integer osDiskSizeInGb;
    private String osDiskType;
    private String osType;
    private String powerState;
    private String provisioningState;
    private String type;
    private String virtualMachineEvictionPolicy;
    private Double virtualMachineMaximumPrice;
    private String virtualMachinePriority;

    /**
     * The name of the agent pool.
     */
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The node size of the agent pool.
     */
    @Required
    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    /**
     * The node count of the agent pool.
     */
    @Required
    @Updatable
    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    /**
     * A list of availability zones to start the agent pool node on.
     */
    public List<Integer> getAvailabilityZones() {
        if (availabilityZones == null) {
            availabilityZones = new ArrayList<>();
        }

        return availabilityZones;
    }

    public void setAvailabilityZones(List<Integer> availabilityZones) {
        this.availabilityZones = availabilityZones;
    }

    /**
     * The tags of the agent pool.
     */
    @Updatable
    public Map<String, String> getTags() {
        if (tags == null) {
            tags = new HashMap<>();
        }

        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    /**
     * The mode of the agent pool.
     */
    @Required
    @Updatable
    @ValidStrings({"System", "User"})
    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    /**
     * If set to ``true`` enables autoscaling. Defaults to ``false``.
     */
    @Updatable
    public Boolean getAutoScalingEnabled() {
        if (autoScalingEnabled == null) {
            autoScalingEnabled = false;
        }

        return autoScalingEnabled;
    }

    public void setAutoScalingEnabled(Boolean autoScalingEnabled) {
        this.autoScalingEnabled = autoScalingEnabled;
    }

    /**
     * The kublet disk type for the agent pool.
     */
    @Updatable
    @ValidStrings({"OS", "Temporary"})
    public String getKubeletDiskType() {
        return kubeletDiskType;
    }

    public void setKubeletDiskType(String kubeletDiskType) {
        this.kubeletDiskType = kubeletDiskType;
    }

    /**
     * The max node size for the agent pool.
     */
    @Updatable
    @DependsOn("auto-scaling-enabled")
    public Integer getMaximumNodeSize() {
        return maximumNodeSize;
    }

    public void setMaximumNodeSize(Integer maximumNodeSize) {
        this.maximumNodeSize = maximumNodeSize;
    }

    /**
     * The max node size for the agent pool.
     */
    @Updatable
    @DependsOn("auto-scaling-enabled")
    public Integer getMinimumNodeSize() {
        return minimumNodeSize;
    }

    public void setMinimumNodeSize(Integer minimumNodeSize) {
        this.minimumNodeSize = minimumNodeSize;
    }

    /**
     * The max pods per node for the agent pool.
     */
    @Required
    public Integer getMaximumPodsPerNode() {
        return maximumPodsPerNode;
    }

    public void setMaximumPodsPerNode(Integer maximumPodsPerNode) {
        this.maximumPodsPerNode = maximumPodsPerNode;
    }

    /**
     * The network for the agent pool.
     */
    @Required
    public NetworkResource getNetwork() {
        return network;
    }


    public void setNetwork(NetworkResource network) {
        this.network = network;
    }

    /**
     * The subnet for the agent pool.
     */
    @Required
    public String getSubnet() {
        return subnet;
    }

    public void setSubnet(String subnet) {
        this.subnet = subnet;
    }

    /**
     * The node labels for the agent pool.
     */
    public Map<String, String> getNodeLabels() {
        if (nodeLabels == null) {
            nodeLabels = new HashMap<>();
        }

        return nodeLabels;
    }

    public void setNodeLabels(Map<String, String> nodeLabels) {
        this.nodeLabels = nodeLabels;
    }

    /**
     * The node size of the agent pool.
     */
    @Output
    public Integer getNodeSize() {
        return nodeSize;
    }

    public void setNodeSize(Integer nodeSize) {
        this.nodeSize = nodeSize;
    }

    /**
     * The list of node taints of the agent pool.
     */
    public List<String> getNodeTaints() {
        if (nodeTaints == null) {
            nodeTaints = new ArrayList<>();
        }

        return nodeTaints;
    }

    public void setNodeTaints(List<String> nodeTaints) {
        this.nodeTaints = nodeTaints;
    }

    /**
     * The os disk size of the agent pool.
     */
    @Required
    public Integer getOsDiskSizeInGb() {
        return osDiskSizeInGb;
    }

    public void setOsDiskSizeInGb(Integer osDiskSizeInGb) {
        this.osDiskSizeInGb = osDiskSizeInGb;
    }

    /**
     * The os disk type of the agent pool.
     */
    @Required
    @ValidStrings({"Managed", "Ephemeral"})
    public String getOsDiskType() {
        return osDiskType;
    }

    public void setOsDiskType(String osDiskType) {
        this.osDiskType = osDiskType;
    }

    /**
     * The OS type of the agent pool.
     */
    @Required
    @ValidStrings({"Linux", "Windows"})
    public String getOsType() {
        return osType;
    }

    public void setOsType(String osType) {
        this.osType = osType;
    }

    /**
     * The power state of the agent pool.
     */
    @Output
    public String getPowerState() {
        return powerState;
    }

    public void setPowerState(String powerState) {
        this.powerState = powerState;
    }

    /**
     * The provisioning state of the agent pool.
     */
    @Output
    public String getProvisioningState() {
        return provisioningState;
    }

    public void setProvisioningState(String provisioningState) {
        this.provisioningState = provisioningState;
    }

    /**
     * The type of the agent pool.
     */
    @Required
    @ValidStrings({"VirtualMachineScaleSets", "AvailabilitySet"})
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * The eviction policy of a spot instance for the node of the agent pool.
     */
    @ValidStrings({"Delete", "Deallocate"})
    public String getVirtualMachineEvictionPolicy() {
        return virtualMachineEvictionPolicy;
    }

    public void setVirtualMachineEvictionPolicy(String virtualMachineEvictionPolicy) {
        this.virtualMachineEvictionPolicy = virtualMachineEvictionPolicy;
    }

    /**
     * The max price for virtual machine for the node of the agent pool.
     */
    @DependsOn("virtual-machine-eviction-policy")
    public Double getVirtualMachineMaximumPrice() {
        return virtualMachineMaximumPrice;
    }

    public void setVirtualMachineMaximumPrice(Double virtualMachineMaximumPrice) {
        this.virtualMachineMaximumPrice = virtualMachineMaximumPrice;
    }

    /**
     * The priority for virtual machine for the node of the agent pool.
     */
    @DependsOn("virtual-machine-eviction-policy")
    @ValidStrings({"Spot", "Regular"})
    public String getVirtualMachinePriority() {
        return virtualMachinePriority;
    }

    public void setVirtualMachinePriority(String virtualMachinePriority) {
        this.virtualMachinePriority = virtualMachinePriority;
    }

    @Override
    public void copyFrom(KubernetesClusterAgentPool model) {
        setName(model.name());
        setCount(model.count());
        setAvailabilityZones(model.availabilityZones().stream().map(Integer::valueOf).collect(Collectors.toList()));
        setTags(model.tags());
        setMode(model.mode().toString());
        setAutoScalingEnabled(model.isAutoScalingEnabled());
        setKubeletDiskType(model.kubeletDiskType().toString());
        setMaximumNodeSize(model.maximumNodeSize());
        setMaximumPodsPerNode(model.maximumPodsPerNode());
        setMinimumNodeSize(model.minimumNodeSize());
        setNetwork(findById(NetworkResource.class, model.networkId()));
        setNodeLabels(model.nodeLabels());
        setNodeSize(model.nodeSize());
        setNodeTaints(model.nodeTaints());
        setOsDiskSizeInGb(model.osDiskSizeInGB());
        setOsDiskType(model.osDiskType().toString());
        setOsType(model.osType().toString());
        setPowerState(model.powerState().code().toString());
        setProvisioningState(model.provisioningState());
        setSubnet(model.subnetName());
        setType(model.type().toString());
        setVirtualMachineEvictionPolicy(model.virtualMachineEvictionPolicy() != null ? model.virtualMachineEvictionPolicy().toString() : null);
        setVirtualMachineMaximumPrice(model.innerModel().spotMaxPrice() != null ? model.virtualMachineMaximumPrice() : null);
        setVirtualMachinePriority(model.virtualMachinePriority() != null ? model.virtualMachinePriority().toString() : null);
        setSize(model.vmSize() != null ? model.vmSize().toString() : null);

    }

    @Override
    public String primaryKey() {
        return getName();
    }
}
