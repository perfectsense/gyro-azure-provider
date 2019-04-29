package gyro.azure.compute;

import gyro.azure.AzureResource;
import gyro.core.resource.Resource;
import gyro.core.resource.ResourceDiffProperty;
import gyro.core.resource.ResourceName;
import gyro.core.resource.ResourceOutput;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.compute.Snapshot;
import com.microsoft.azure.management.compute.SnapshotSkuType;
import com.microsoft.azure.management.compute.SnapshotStorageAccountTypes;
import com.microsoft.azure.management.compute.Snapshot.DefinitionStages.WithCreate;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@ResourceName("snapshot")
public class SnapshotResource extends AzureResource {

    private String diskId;
    private String id;
    private String name;
    private String provider;
    private String resourceGroupName;
    private String sku;
    private Integer size;
    private String snapshotId;
    private String source;
    private Map<String, String> tags;
    private String vhdUrl;

    /**
     * Input disk id from existing disk. (Optional)
     */
    public String getDiskId() {
        return diskId;
    }

    public void setDiskId(String diskId) {
        this.diskId = diskId;
    }

    /**
     * Id associated with the snapshot.
     */
    @ResourceOutput
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * The name of the snapshot. (Required)
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Determines what data type is used. Options include disk, snapshot, or vhd. (Required)
     */
    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    /**
     * The input resource group name. (Required)
     */
    public String getResourceGroupName() {
        return resourceGroupName;
    }

    public void setResourceGroupName(String resourceGroupName) {
        this.resourceGroupName = resourceGroupName;
    }

    /**
     * Specifies the sku type. Options include Premium_LRS, Standard_LRS, Standard_ZRS (Optional)
     */
    @ResourceDiffProperty(updatable = true)
    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    /**
     * Specifies the disk size in GB (Optional)
     */
    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    /**
     * The id of the source data managed snapshot. (Optional)
     */
    public String getSnapshotId() {
        return snapshotId;
    }

    public void setSnapshotId(String snapshotId) {
        this.snapshotId = snapshotId;
    }

    /**
     * The type of the disk, snapshot, or vhd used. Options include Linux, Windows, or Data. (Required)
     */
    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    /**
     * The tags associated with the snapshot. (Required)
     */
    @ResourceDiffProperty(updatable = true)
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
     * The url of the vhd. (Optional)
     */
    public String getVhdUrl() {
        return vhdUrl;
    }

    public void setVhdUrl(String vhdUrl) {
        this.vhdUrl = vhdUrl;
    }

    @Override
    public boolean refresh() {
        Azure client = createClient();

        Snapshot snapshot = client.snapshots().getById(getId());

        if (snapshot == null) {
            return false;
        }

        setId(snapshot.id());
        setName(snapshot.name());
        setResourceGroupName(snapshot.resourceGroupName());
        setSku(snapshot.skuType().toString());
        setTags(snapshot.tags());

        return true;
    }

    @Override
    public void create() {
        Azure client = createClient();

        Snapshot.DefinitionStages.WithSnapshotSource withSnapshotSource = client.snapshots().define(getName())
                .withRegion(Region.fromName(getRegion()))
                .withExistingResourceGroup(getResourceGroupName());

        WithCreate withCreate = null;

        if (getSource().equals("Data")) {
            if (getProvider().equals("Disk")) {
                withCreate = withSnapshotSource.withDataFromDisk(getDiskId());
            } else if (getProvider().equals("Snapshot")) {
                withCreate = withSnapshotSource.withDataFromSnapshot(getSnapshotId());
            } else if (getProvider().equals("Vhd")) {
                withCreate = withSnapshotSource.withDataFromVhd(getVhdUrl());
            }
        } else if (getSource().equals("Linux")) {
            if (getProvider().equals("Disk")) {
                withCreate = withSnapshotSource.withLinuxFromDisk(getDiskId());
            } else if (getProvider().equals("Snapshot")) {
                withCreate = withSnapshotSource.withLinuxFromSnapshot(getSnapshotId());
            } else if (getProvider().equals("Vhd")) {
                withCreate = withSnapshotSource.withLinuxFromVhd(getVhdUrl());
            }
        } else if (getSource().equals("Windows")) {
            if (getProvider().equals("Disk")) {
                withCreate = withSnapshotSource.withWindowsFromDisk(getDiskId());
            } else if (getProvider().equals("Snapshot")) {
                withCreate = withSnapshotSource.withWindowsFromSnapshot(getSnapshotId());
            } else if (getProvider().equals("Vhd")) {
                withCreate = withSnapshotSource.withWindowsFromVhd(getVhdUrl());
            }
        }

        Snapshot snapshot = withCreate.withSizeInGB(getSize())
                .withSku(SnapshotSkuType.fromStorageAccountType(SnapshotStorageAccountTypes.fromString(getSku())))
                .withTags(getTags())
                .create();

        setId(snapshot.id());
    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {
        Azure client = createClient();

        client.snapshots().getById(getId())
                .update()
                .withSku(SnapshotSkuType.fromStorageAccountType(SnapshotStorageAccountTypes.fromString(getSku())))
                .withTags(getTags())
                .apply();
    }

    @Override
    public void delete() {
        Azure client = createClient();

        client.snapshots().deleteById(getId());
    }

    @Override
    public String toDisplayString() {
        return "snapshot " + getName();
    }

    /*
    private void parseOutput(Snapshot snap) {
        CreationSourceType type = snap.source().type();
        if (type == CreationSourceType.COPIED_FROM_DISK ||
            type == CreationSourceType.FROM_DATA_DISK_IMAGE ||
                type == CreationSourceType.FROM_OS_DISK_IMAGE) {
            //setSource("Disk");
            setDiskId(snap.source().sourceId());
        } else if (type == CreationSourceType.COPIED_FROM_SNAPSHOT) {
            //setSource("Snapshot");
            setSnapshotId(snap.source().sourceId());
        } else if (type == CreationSourceType.IMPORTED_FROM_VHD) {
            //setProvider("Vhd");
            setVhdUrl(snap.source().sourceId());
            //setSource(snap.osType().toString());
        }
    }*/
}
