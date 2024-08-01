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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.azure.core.management.Region;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.compute.models.Disk;
import com.azure.resourcemanager.compute.models.DiskSkuTypes;
import com.azure.resourcemanager.compute.models.DiskStorageAccountTypes;
import com.azure.resourcemanager.compute.models.OperatingSystemTypes;
import com.psddev.dari.util.ObjectUtils;
import gyro.azure.AzureResource;
import gyro.azure.Copyable;
import gyro.azure.resources.ResourceGroupResource;
import gyro.azure.storage.StorageAccountResource;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import gyro.core.validation.ValidationError;

/**
 * Creates a disk.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    azure::disk disk-example
 *         name: "disk-example"
 *         type: "Standard_LRS"
 *         os-type: "LINUX"
 *         size: 10
 *         resource-group: $(azure::resource-group resource-group-disk-example)
 *         tags: {
 *             Name: "disk-example"
 *         }
 *    end
 */
@Type("disk")
public class DiskResource extends AzureResource implements Copyable<Disk> {

    private String name;
    private String id;
    private ResourceGroupResource resourceGroup;
    private Integer size;
    private String osType;
    private String type;
    private String dataLoadSourceType;
    private String dataLoadSource;
    private StorageAccountResource dataLoadSourceStorageAccount;
    private Map<String, String> tags;

    /**
     * Name of the Disk.
     */
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The ID of the the Disk.
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
     * The resource group under which the Disk would reside.
     */
    @Required
    public ResourceGroupResource getResourceGroup() {
        return resourceGroup;
    }

    public void setResourceGroup(ResourceGroupResource resourceGroup) {
        this.resourceGroup = resourceGroup;
    }

    /**
     * Size of the Disk in Gb.
     */
    @Required
    @Updatable
    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    /**
     * Type of OS.
     */
    @Required
    @ValidStrings({ "LINUX", "WINDOWS" })
    @Updatable
    public String getOsType() {
        return osType != null ? osType.toUpperCase() : null;
    }

    public void setOsType(String osType) {
        this.osType = osType;
    }

    /**
     * Type of Disk.
     */
    @Required
    @ValidStrings({ "STANDARD_LRS", "PREMIUM_LRS", "STANDARDSSD_LRS", "ULTRASSD_LRS" })
    @Updatable
    public String getType() {
        return type != null ? type.toUpperCase() : null;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * Type of data source. Defaults to ``disk``.
     */
    @ValidStrings({ "disk", "vhd", "snapshot" })
    public String getDataLoadSourceType() {
        if (dataLoadSourceType == null) {
            dataLoadSourceType = "disk";
        }
        return dataLoadSourceType;
    }

    public void setDataLoadSourceType(String dataLoadSourceType) {
        this.dataLoadSourceType = dataLoadSourceType;
    }

    /**
     * The actual data source.
     */
    public String getDataLoadSource() {
        return dataLoadSource;
    }

    public void setDataLoadSource(String dataLoadSource) {
        this.dataLoadSource = dataLoadSource;
    }

    /**
     * The storage account where data source resides. Required only when `data-load-source-type` is set to `vhd`.
     */
    public StorageAccountResource getDataLoadSourceStorageAccount() {
        return dataLoadSourceStorageAccount;
    }

    public void setDataLoadSourceStorageAccount(StorageAccountResource dataLoadSourceStorageAccount) {
        this.dataLoadSourceStorageAccount = dataLoadSourceStorageAccount;
    }

    /**
     * Tags for the Disk.
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

    @Override
    public void copyFrom(Disk disk) {
        setId(disk.id());
        setName(disk.name());
        setOsType(disk.osType() != null ? disk.osType().name() : null);
        setSize(disk.sizeInGB());
        setType(disk.sku().accountType().toString());
        setTags(disk.tags());
        setResourceGroup(findById(ResourceGroupResource.class, disk.resourceGroupName()));
    }

    @Override
    public boolean refresh() {
        AzureResourceManager client = createClient(AzureResourceManager.class);

        Disk disk = client.disks().getById(getId());

        if (disk == null) {
            return false;
        }

        copyFrom(disk);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) {
        AzureResourceManager client = createClient(AzureResourceManager.class);

        Disk.DefinitionStages.WithDiskSource diskDefWithoutData = client.disks()
            .define(getName())
            .withRegion(Region.fromName(getRegion()))
            .withExistingResourceGroup(getResourceGroup().getName());

        Disk disk;

        if (!ObjectUtils.isBlank(getDataLoadSource())) {
            Disk.DefinitionStages.WithCreateAndSize diskDefWithData;
            if (getOsType().equals("LINUX")) {
                //Linux
                if (getDataLoadSourceType().equals("vhd")) {
                    diskDefWithData = diskDefWithoutData.withLinuxFromVhd(getDataLoadSource())
                        .withStorageAccountName(getDataLoadSourceStorageAccount().getName());
                } else if (getDataLoadSourceType().equals("snapshot")) {
                    diskDefWithData = diskDefWithoutData.withLinuxFromSnapshot(getDataLoadSource());
                } else {
                    //disk
                    diskDefWithData = diskDefWithoutData.withLinuxFromDisk(getDataLoadSource());
                }
            } else {
                //Windows
                if (getDataLoadSourceType().equals("vhd")) {
                    diskDefWithData = diskDefWithoutData.withWindowsFromVhd(getDataLoadSource())
                        .withStorageAccountName(getDataLoadSourceStorageAccount().getName());
                } else if (getDataLoadSourceType().equals("snapshot")) {
                    diskDefWithData = diskDefWithoutData.withWindowsFromSnapshot(getDataLoadSource());
                } else {
                    //disk
                    diskDefWithData = diskDefWithoutData.withWindowsFromDisk(getDataLoadSource());
                }
            }

            disk = diskDefWithData.withSizeInGB(getSize())
                .withTags(getTags())
                .withSku(DiskSkuTypes.fromStorageAccountType(DiskStorageAccountTypes.fromString(getType())))
                .create();

        } else {
            disk = diskDefWithoutData.withData()
                .withSizeInGB(getSize())
                .withTags(getTags())
                .withSku(DiskSkuTypes.fromStorageAccountType(DiskStorageAccountTypes.fromString(getType())))
                .create();

            disk.update().withOSType(OperatingSystemTypes.fromString(getOsType())).apply();
        }

        setId(disk.id());
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {
        AzureResourceManager client = createClient(AzureResourceManager.class);

        Disk disk = client.disks().getById(getId());

        int changeCount = 0;

        if (changedFieldNames.contains("os-type")) {
            if (!ObjectUtils.isBlank(getOsType())) {
                disk.update()
                    .withOSType(OperatingSystemTypes.fromString(getOsType()))
                    .apply();
            }

            changeCount++;
        }

        if (changedFieldNames.size() > changeCount) {
            disk.update()
                .withSizeInGB(getSize())
                .withSku(DiskSkuTypes.fromStorageAccountType(DiskStorageAccountTypes.fromString(getType())))
                .withTags(getTags())
                .apply();
        }
    }

    @Override
    public void delete(GyroUI ui, State state) {
        AzureResourceManager client = createClient(AzureResourceManager.class);

        client.disks().deleteById(getId());
    }

    @Override
    public List<ValidationError> validate() {
        List<ValidationError> errors = new ArrayList<>();

        if (getDataLoadSourceType().equals("vhd") && getDataLoadSourceStorageAccount() == null) {
            errors.add(new ValidationError(
                this,
                "data-load-source-storage-account",
                "required when `data-load-source-type` is set to `vhd`."));
        }

        return errors;
    }
}
