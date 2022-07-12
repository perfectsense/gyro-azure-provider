/*
 * Copyright 2019, Perfect Sense, Inc.
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

package gyro.azure.compute;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.azure.core.management.Region;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.compute.models.CreationSourceType;
import com.azure.resourcemanager.compute.models.Snapshot;
import com.azure.resourcemanager.compute.models.SnapshotSkuType;
import com.azure.resourcemanager.compute.models.SnapshotStorageAccountTypes;
import gyro.azure.AzureResource;
import gyro.azure.Copyable;
import gyro.azure.resources.ResourceGroupResource;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;

/**
 * Creates a snapshot.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    azure::snapshot snapshot-example
 *        provider: "Disk"
 *        disk: $(azure::disk disk-example)
 *        name: "snapshot-name"
 *        resource-group: $(azure::resource-group resource-group-snapshot-example)
 *        size: 10
 *        sku: "Standard_LRS"
 *        source: "Data"
 *        tags: {
 *             Name: "snapshot-example"
 *        }
 *    end
 */
@Type("snapshot")
public class SnapshotResource extends AzureResource implements Copyable<Snapshot> {

    private static final String SOURCE_DATA = "Data";
    private static final String SOURCE_LINUX = "Linux";
    private static final String SOURCE_WINDOWS = "Windows";
    private static final String DISK = "Disk";
    private static final String SNAPSHOT = "Snapshot";
    private static final String VHD = "Vhd";

    private DiskResource disk;
    private String id;
    private String name;
    private String provider;
    private ResourceGroupResource resourceGroup;
    private String sku;
    private Integer size;
    private SnapshotResource snapshot;
    private String source;
    private Map<String, String> tags;
    private String vhdUrl;
    private Date creationTime;

    /**
     * Input disk from existing disk. Used when ``Disk`` is the provider. (Conditional)
     */
    public DiskResource getDisk() {
        return disk;
    }

    public void setDisk(DiskResource disk) {
        this.disk = disk;
    }

    /**
     * Id associated with the Snapshot.
     */
    @Id
    @Output
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * The name of the Snapshot.
     */
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Determines what data type is used.
     */
    @Required
    @ValidStrings({ "disk", "snapshot", "vhd" })
    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    /**
     * The Resource Group where the Snapshot resides in.
     */
    @Required
    public ResourceGroupResource getResourceGroup() {
        return resourceGroup;
    }

    public void setResourceGroup(ResourceGroupResource resourceGroup) {
        this.resourceGroup = resourceGroup;
    }

    /**
     * Specifies the sku type.
     */
    @ValidStrings({ "Premium_LRS", "Standard_LRS", "Standard_ZRS" })
    @Updatable
    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    /**
     * Specifies the disk size in GB.
     */
    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    /**
     * The source data managed snapshot. Used when ``Snapshot`` is the provider. (Conditional)
     */
    public SnapshotResource getSnapshot() {
        return snapshot;
    }

    public void setSnapshot(SnapshotResource snapshot) {
        this.snapshot = snapshot;
    }

    /**
     * The type of the disk, snapshot, or vhd used.
     */
    @Required
    @ValidStrings({ "Linux", "Windows", "Data" })
    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    /**
     * The tags associated with the Snapshot.
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
     * The url of the vhd. Used when ``Vhd`` is the provider. (Conditional)
     */
    public String getVhdUrl() {
        return vhdUrl;
    }

    public void setVhdUrl(String vhdUrl) {
        this.vhdUrl = vhdUrl;
    }

    /**
     * Creation time of the Snapshot.
     */
    @Output
    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    @Override
    public void copyFrom(Snapshot snapshot) {
        setId(snapshot.id());
        setName(snapshot.name());
        setResourceGroup(findById(ResourceGroupResource.class, snapshot.resourceGroupName()));
        setSku(snapshot.skuType().toString());
        setTags(snapshot.tags());
        setSize(snapshot.sizeInGB());
        setDisk(null);
        setSnapshot(null);
        if (snapshot.source().type().equals(CreationSourceType.COPIED_FROM_DISK)) {
            setDisk(findById(DiskResource.class, snapshot.innerModel().creationData().sourceResourceId()));
        } else if (snapshot.source().type().equals(CreationSourceType.COPIED_FROM_SNAPSHOT)) {
            setSnapshot(findById(SnapshotResource.class, snapshot.innerModel().creationData().sourceResourceId()));
        }
        setCreationTime(Date.from(snapshot.innerModel().timeCreated().toInstant()));
    }

    @Override
    public boolean refresh() {
        AzureResourceManager client = createClient();

        Snapshot snapshot = client.snapshots().getById(getId());

        if (snapshot == null) {
            return false;
        }

        copyFrom(snapshot);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) {
        AzureResourceManager client = createClient();

        Snapshot.DefinitionStages.WithSnapshotSource withSnapshotSource = client.snapshots().define(getName())
            .withRegion(Region.fromName(getRegion()))
            .withExistingResourceGroup(getResourceGroup().getName());

        Snapshot.DefinitionStages.WithCreate withCreate = null;

        boolean invalidSource = false;

        if (SOURCE_DATA.equalsIgnoreCase(getSource())) {
            if (DISK.equalsIgnoreCase(getProvider())) {
                withCreate = withSnapshotSource.withDataFromDisk(getDisk().getId());
            } else if (SNAPSHOT.equalsIgnoreCase(getProvider())) {
                withCreate = withSnapshotSource.withDataFromSnapshot(getSnapshot().getId());
            } else if (VHD.equalsIgnoreCase(getProvider())) {
                withCreate = withSnapshotSource.withDataFromVhd(getVhdUrl());
            }
        } else if (SOURCE_LINUX.equalsIgnoreCase(getSource())) {
            if (DISK.equalsIgnoreCase(getProvider())) {
                withCreate = withSnapshotSource.withLinuxFromDisk(getDisk().getId());
            } else if (SNAPSHOT.equalsIgnoreCase(getProvider())) {
                withCreate = withSnapshotSource.withLinuxFromSnapshot(getSnapshot().getId());
            } else if (VHD.equalsIgnoreCase(getProvider())) {
                withCreate = withSnapshotSource.withLinuxFromVhd(getVhdUrl());
            }
        } else if (SOURCE_WINDOWS.equalsIgnoreCase(getSource())) {
            if (DISK.equalsIgnoreCase(getProvider())) {
                withCreate = withSnapshotSource.withWindowsFromDisk(getDisk().getId());
            } else if (SNAPSHOT.equalsIgnoreCase(getProvider())) {
                withCreate = withSnapshotSource.withWindowsFromSnapshot(getSnapshot().getId());
            } else if (VHD.equalsIgnoreCase(getProvider())) {
                withCreate = withSnapshotSource.withWindowsFromVhd(getVhdUrl());
            }
        } else {
            invalidSource = true;
        }

        if (withCreate == null) {
            if (invalidSource) {
                throw new GyroException("Invalid source. Source options include Data, Linux, and Windows");
            } else {
                throw new GyroException("Invalid provider. Provider options include Disk, Snapshot, and Vhd");
            }
        }

        if (getSize() != null) {
            withCreate = withCreate.withSizeInGB(getSize());
        }
        if (getSku() != null) {
            withCreate.withSku(SnapshotSkuType.fromStorageAccountType(SnapshotStorageAccountTypes.fromString(getSku())));
        }

        Snapshot snapshot = withCreate.withTags(getTags())
            .create();

        copyFrom(snapshot);
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {
        AzureResourceManager client = createClient();

        client.snapshots().getById(getId())
            .update()
            .withSku(SnapshotSkuType.fromStorageAccountType(SnapshotStorageAccountTypes.fromString(getSku())))
            .withTags(getTags())
            .apply();
    }

    @Override
    public void delete(GyroUI ui, State state) {
        AzureResourceManager client = createClient();

        client.snapshots().deleteById(getId());
    }
}
