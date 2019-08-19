package gyro.azure.compute;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.compute.Snapshot;
import gyro.azure.AzureFinder;
import gyro.core.Type;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Type("snapshot")
public class SnapshotFinder extends AzureFinder<Snapshot, SnapshotResource> {
    private String id;

    /**
     * The ID of the Snapshot.
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    protected List<Snapshot> findAllAzure(Azure client) {
        return client.snapshots().list();
    }

    @Override
    protected List<Snapshot> findAzure(Azure client, Map<String, String> filters) {
        Snapshot snapshot = client.snapshots().getById(filters.get("id"));
        if (snapshot == null) {
            return Collections.emptyList();
        } else {
            return Collections.singletonList(snapshot);
        }
    }
}
