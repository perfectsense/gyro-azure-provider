package gyro.azure.storage;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.storage.StorageAccount;
import gyro.azure.AzureFinder;
import gyro.core.Type;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Type("storage-account")
public class StorageAccountFinder extends AzureFinder<StorageAccount, StorageAccountResource> {
    private String id;

    /**
     * The ID of the Storage Account.
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    protected List<StorageAccount> findAllAzure(Azure client) {
        return client.storageAccounts().list();
    }

    @Override
    protected List<StorageAccount> findAzure(Azure client, Map<String, String> filters) {
        StorageAccount storageAccount = client.storageAccounts().getById(filters.get("id"));
        if (storageAccount == null) {
            return Collections.emptyList();
        } else {
            return Collections.singletonList(storageAccount);
        }
    }
}
