package gyro.azure.cdn;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.cdn.CdnProfile;
import gyro.azure.AzureFinder;
import gyro.core.Type;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Query cdn profile.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    cdn-profile: $(external-query azure::cdn-profile {})
 */
@Type("cdn-profile")
public class CdnProfileFinder extends AzureFinder<CdnProfile, CdnProfileResource> {
    private String id;

    /**
     * The ID of the CDN Profile.
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    protected List<CdnProfile> findAllAzure(Azure client) {
        return client.cdnProfiles().list();
    }

    @Override
    protected List<CdnProfile> findAzure(Azure client, Map<String, String> filters) {
        CdnProfile cdnProfile = client.cdnProfiles().getById(filters.get("id"));
        if (cdnProfile == null) {
            return Collections.emptyList();
        } else {
            return Collections.singletonList(cdnProfile);
        }
    }
}
