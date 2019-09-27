package gyro.azure.compute;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.compute.AvailabilitySet;
import gyro.azure.AzureFinder;
import gyro.core.Type;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Query availability set.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    availability-set: $(external-query azure::availability-set {})
 */
@Type("availability-set")
public class AvailabilitySetFinder extends AzureFinder<AvailabilitySet, AvailabilitySetResource> {
    private String id;

    /**
     * The ID of the Availability Set.
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    @Override
    protected List<AvailabilitySet> findAllAzure(Azure client) {
        return client.availabilitySets().list();
    }

    @Override
    protected List<AvailabilitySet> findAzure(Azure client, Map<String, String> filters) {
        AvailabilitySet availabilitySet = client.availabilitySets().getById(filters.get("id"));
        if (availabilitySet == null) {
            return Collections.emptyList();
        } else {
            return Collections.singletonList(availabilitySet);
        }
    }
}
