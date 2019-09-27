package gyro.azure.compute;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.compute.Disk;
import gyro.azure.AzureFinder;
import gyro.core.Type;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Query disk.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    disk: $(external-query azure::disk {})
 */
@Type("disk")
public class DiskFinder extends AzureFinder<Disk, DiskResource> {
    private String id;

    /**
     * The ID of the Disk.
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    protected List<Disk> findAllAzure(Azure client) {
        return client.disks().list();
    }

    @Override
    protected List<Disk> findAzure(Azure client, Map<String, String> filters) {
        Disk disk = client.disks().getById(filters.get("id"));
        if (disk == null) {
            return Collections.emptyList();
        } else {
            return Collections.singletonList(disk);
        }
    }
}
