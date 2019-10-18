package gyro.azure.identity;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.msi.Identity;
import gyro.azure.AzureFinder;
import gyro.core.Type;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Query identity.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    identity: $(external-query azure::identity {})
 */
@Type("identity")
public class IdentityFinder extends AzureFinder<Identity, IdentityResource> {
    private String id;

    /**
     * The ID of the Identity.
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    protected List<Identity> findAllAzure(Azure client) {
        return client.identities().list();
    }

    @Override
    protected List<Identity> findAzure(Azure client, Map<String, String> filters) {
        Identity identity = client.identities().getById(filters.get("id"));

        if (identity == null) {
            return Collections.emptyList();
        } else {
            return Collections.singletonList(identity);
        }
    }
}
