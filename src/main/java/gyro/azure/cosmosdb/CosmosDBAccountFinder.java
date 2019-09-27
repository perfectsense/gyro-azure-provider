package gyro.azure.cosmosdb;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.cosmosdb.CosmosDBAccount;
import gyro.azure.AzureFinder;
import gyro.core.Type;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Query cosmos db.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    cosmos-db: $(external-query azure::cosmos-db {})
 */
@Type("cosmos-db")
public class CosmosDBAccountFinder extends AzureFinder<CosmosDBAccount, CosmosDBAccountResource> {
    private String id;

    /**
     * The ID of the Cosmos DB Account.
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    protected List<CosmosDBAccount> findAllAzure(Azure client) {
        return client.cosmosDBAccounts().list();
    }

    @Override
    protected List<CosmosDBAccount> findAzure(Azure client, Map<String, String> filters) {
        CosmosDBAccount cosmosDBAccount = client.cosmosDBAccounts().getById(filters.get("id"));
        if (cosmosDBAccount == null) {
            return Collections.emptyList();
        } else {
            return Collections.singletonList(cosmosDBAccount);
        }
    }
}
