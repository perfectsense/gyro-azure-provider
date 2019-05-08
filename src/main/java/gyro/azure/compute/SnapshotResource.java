package gyro.azure.compute;

import gyro.azure.AzureResource;
import gyro.core.GyroException;
import gyro.core.resource.Resource;
import gyro.core.resource.ResourceUpdatable;
import gyro.core.resource.ResourceType;
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

/**
 * Creates a snapshot.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *        azure::snapshot snapshot-example
 *            provider: "Disk"
 *            disk-id: $(azure::disk disk-example | disk-id)
 *            name: "snapshot-name"
 *            resource-group-name: $(azure::resource-group resource-group-snapshot-example | resource-group-name)
 *            size: 10
 *            sku: "Standard_LRS"
 *            source: "Data"
 *            tags: {
 *                 Name: "snapshot-example"
 *            }
 *        end
 */
@ResourceType("snapshot")
public class SnapshotResource extends AzureResource {

    private static final String SOURCE_DATA = "Data";
    private static final String SOURCE_LINUX = "Linux";
    private static final String SOURCE_WINDOWS = "Windows";
    private static final String DISK = "Disk";
    private static final String SNAPSHOT = "Snapshot";
    private static final String VHD = "Vhd";

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
     * Input disk id from existing disk. Used when "Disk" is the provider. (Conditional)
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
     * Specifies the sku type. Options include Premium_LRS, Standard_LRS, Standard_ZRS. (Optional)
     */
    @ResourceUpdatable
    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    /**
     * Specifies the disk size in GB. (Optional)
     */
    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    /**
     * The id of the source data managed snapshot. Used when "Snapshot" is the provider. (Conditional)
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
     * The tags associated with the snapshot. (Optional)
     */
    @ResourceUpdatable
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
     * The url of the vhd. Used when "Vhd" is the provider. (Conditional)
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

        boolean invalidSource = false;

        if (SOURCE_DATA.equalsIgnoreCase(getSource())) {
            if (DISK.equalsIgnoreCase(getProvider())) {
                withCreate = withSnapshotSource.withDataFromDisk(getDiskId());
            } else if (SNAPSHOT.equalsIgnoreCase(getProvider())) {
                withCreate = withSnapshotSource.withDataFromSnapshot(getSnapshotId());
            } else if (VHD.equalsIgnoreCase(getProvider())) {
                withCreate = withSnapshotSource.withDataFromVhd(getVhdUrl());
            }
        } else if (SOURCE_LINUX.equalsIgnoreCase(getSource())) {
            if (DISK.equalsIgnoreCase(getProvider())) {
                withCreate = withSnapshotSource.withLinuxFromDisk(getDiskId());
            } else if (SNAPSHOT.equalsIgnoreCase(getProvider())) {
                withCreate = withSnapshotSource.withLinuxFromSnapshot(getSnapshotId());
            } else if (VHD.equalsIgnoreCase(getProvider())) {
                withCreate = withSnapshotSource.withLinuxFromVhd(getVhdUrl());
            }
        } else if (SOURCE_WINDOWS.equalsIgnoreCase(getSource())) {
            if (DISK.equalsIgnoreCase(getProvider())) {
                withCreate = withSnapshotSource.withWindowsFromDisk(getDiskId());
            } else if (SNAPSHOT.equalsIgnoreCase(getProvider())) {
                withCreate = withSnapshotSource.withWindowsFromSnapshot(getSnapshotId());
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

        setId(snapshot.id());
    }

    @Override
    public void update(Resource current, Set<String> changedFieldNames) {
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
}
