package gyro.azure.cosmosdb;

import gyro.azure.AzureResource;
import gyro.core.GyroException;
import gyro.core.resource.Resource;
import gyro.core.resource.ResourceDiffProperty;
import gyro.core.resource.ResourceName;
import gyro.core.resource.ResourceOutput;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.cosmosdb.Capability;
import com.microsoft.azure.management.cosmosdb.CosmosDBAccount;
import com.microsoft.azure.management.cosmosdb.DatabaseAccountKind;
import com.microsoft.azure.management.cosmosdb.VirtualNetworkRule;
import com.microsoft.azure.management.cosmosdb.CosmosDBAccount.DefinitionStages.WithCreate;
import com.microsoft.azure.management.cosmosdb.CosmosDBAccount.DefinitionStages.WithConsistencyPolicy;
import com.microsoft.azure.management.cosmosdb.CosmosDBAccount.DefinitionStages.WithKind;
import com.microsoft.azure.management.cosmosdb.CosmosDBAccount.UpdateStages.WithOptionals;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Creates a cosmos database.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *         azure::cosmos-db cosmos-db-example
 *             database-account-kind: "AzureTable"
 *             consistency-level: "BoundedStaleness"
 *             max-interval: 6
 *             max-staleness-prefix: 5
 *             end
 *             resource-group-name: $(azure::resource-group resource-group-cosmos-db-example | resource-group-name)
 *             subnet-name: "subnet1"
 *             tags: {
 *                 Name: "network-interface-example"
 *             }
 *             write-replication-region: "West US"
 *         end
 */
@ResourceName("cosmos-db")
public class CosmosDBAccountResource extends AzureResource {

    private String databaseAccountKind;
    private String consistencyLevel;
    private String id;
    private String ipRangeFilter;
    private Integer maxInterval;
    private String maxStalenessPrefix;
    private String name;
    private List<String> readReplicationRegions;
    private String resourceGroupName;
    private Map<String, String> tags;
    private List<String> virtualNetworkRules;
    private String writeReplicationRegion;

    /**
     */
    public String getDatabaseAccountKind() {
        return databaseAccountKind;
    }

    public void setDatabaseAccountKind(String databaseAccountKind) {
        this.databaseAccountKind = databaseAccountKind;
    }

    /**
     * The consistency policy of the account. Options include BoundedStaleness, Eventual,
     * Session, and Strong (Required)
     */
    @ResourceDiffProperty(updatable = true)
    public String getConsistencyLevel() {
        return consistencyLevel;
    }

    public void setConsistencyLevel(String consistencyLevel) {
        this.consistencyLevel = consistencyLevel;
    }

    /**
     * The output id of the database.
     */
    @ResourceOutput
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * The ip range filter in CIDR notation (Optional)
     */
    @ResourceDiffProperty(updatable = true)
    public String getIpRangeFilter() {
        return ipRangeFilter;
    }

    public void setIpRangeFilter(String ipRangeFilter) {
        this.ipRangeFilter = ipRangeFilter;
    }

    /**
     * The max interval, in seconds. Used with bounded staleness consistency policy. (Conditional)
     */
    @ResourceDiffProperty(updatable = true)
    public Integer getMaxInterval() {
        return maxInterval;
    }

    public void setMaxInterval(Integer maxInterval) {
        this.maxInterval = maxInterval;
    }

    /**
     * The max staleness prefix. Used with bounded staleness consistency policy. (Conditional)
     */
    @ResourceDiffProperty(updatable = true)
    public String getMaxStalenessPrefix() {
        return maxStalenessPrefix;
    }

    public void setMaxStalenessPrefix(String maxStalenessPrefix) {
        this.maxStalenessPrefix = maxStalenessPrefix;
    }

    /**
     * Name of the account. (Required)
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the read location regions. (Optional)
     */
    @ResourceDiffProperty(updatable = true)
    public List<String> getReadReplicationRegions() {
        if (readReplicationRegions == null) {
            readReplicationRegions = new ArrayList<>();
        }

        return readReplicationRegions;
    }

    public void setReadReplicationRegions(List<String> readReplicationRegions) {
        this.readReplicationRegions = readReplicationRegions;
    }

    /**
     * The resource group where the database is found. (Required)
     */
    public String getResourceGroupName() {
        return resourceGroupName;
    }

    public void setResourceGroupName(String resourceGroupName) {
        this.resourceGroupName = resourceGroupName;
    }

    /**
     * The tags associated with the account. (Optional)
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
     * The virtual network rules associated with the account. (Optional)
     */
    @ResourceDiffProperty(updatable = true)
    public List<String> getVirtualNetworkRules() {
        if (virtualNetworkRules == null) {
            virtualNetworkRules = new ArrayList<>();
        }

        return virtualNetworkRules;
    }

    public void setVirtualNetworkRules(List<String> virtualNetworkRules) {
        this.virtualNetworkRules = virtualNetworkRules;
    }

    /**
     * Sets the write location. Used with BoundedStaleness, Eventual, and Session
     * consistency level (Conditional)
     */
    @ResourceDiffProperty(updatable = true)
    public String getWriteReplicationRegion() {
        return writeReplicationRegion;
    }

    public void setWriteReplicationRegion(String writeReplicationRegion) {
        this.writeReplicationRegion = writeReplicationRegion;
    }

    @Override
    public boolean refresh() {
        Azure client = createClient();

        CosmosDBAccount cosmosAccount = client.cosmosDBAccounts().getById(getId());

        if (cosmosAccount == null) {
            return false;
        }

        setConsistencyLevel(cosmosAccount.consistencyPolicy().defaultConsistencyLevel().toString());
        setId(cosmosAccount.id());
        setIpRangeFilter(cosmosAccount.ipRangeFilter());

        if (getConsistencyLevel().equals("BoundedStateless")) {
            setMaxInterval(cosmosAccount.consistencyPolicy().maxIntervalInSeconds());
            setMaxStalenessPrefix(cosmosAccount.consistencyPolicy().maxStalenessPrefix().toString());
        }
        setName(cosmosAccount.name());

        // if the write regions is placed first in the list, then load everything
        if (getReadReplicationRegions().get(0).equals(getWriteReplicationRegion())) {
            getReadReplicationRegions().clear();
            cosmosAccount.readableReplications().forEach(loc -> getReadReplicationRegions().add(loc.locationName()));
        } else {
            // if not, omit the first element of the read replications list
            getReadReplicationRegions().clear();
            for (int i = 1; i < cosmosAccount.readableReplications().size(); i++) {
                getReadReplicationRegions().add(cosmosAccount.readableReplications().get(i).locationName());
            }
        }

        setTags(cosmosAccount.tags());

        getVirtualNetworkRules().clear();
        cosmosAccount.virtualNetworkRules().forEach(rule -> getVirtualNetworkRules().add(rule.id()));

        cosmosAccount.writableReplications().forEach(loc -> setWriteReplicationRegion(loc.locationName()));

        return true;
    }

    @Override
    public void create() {
        Azure client = createClient();

        WithKind withKind = client.cosmosDBAccounts()
                .define(getName())
                .withRegion(Region.fromName(getRegion()))
                .withExistingResourceGroup(getResourceGroupName());

        WithConsistencyPolicy withConsistencyPolicy = null;
        if (getDatabaseAccountKind() != null) {
                withConsistencyPolicy = withKind.withDataModelAzureTable();
            } else if (getDatabaseAccountKind().equals("Cassandra")) {
                withConsistencyPolicy = withKind.withDataModelCassandra();
            } else if (getDatabaseAccountKind().equals("Gremlin")) {
                withConsistencyPolicy = withKind.withDataModelGremlin();
            } else if (getDatabaseAccountKind().equals("MongoDB")) {
                withConsistencyPolicy = withKind.withDataModelMongoDB();
            } else if (getDatabaseAccountKind().equals("Sql")) {
                withConsistencyPolicy = withKind.withDataModelSql();
            } else {
                withConsistencyPolicy = withKind.withKind(DatabaseAccountKind.fromString(getDatabaseAccountKind()));
                throw new GyroException("Invalid database account kind. " +
                        "Values are AzureTable, Cassandra, Gremlin, MongoDB, and Sql");
            }
        }

        WithCreate withCreate = null;
        if (getConsistencyLevel().equals("Bounded Staleness")) {
            withCreate = withConsistencyPolicy
                    .withBoundedStalenessConsistency(Long.parseLong(getMaxStalenessPrefix()), getMaxInterval())
                    .withWriteReplication(Region.fromName(getWriteReplicationRegion()));
        } else if (consistencyLevel.equals("Eventual")) {
            withCreate = withConsistencyPolicy.withEventualConsistency()
                        .withWriteReplication(Region.fromName(getWriteReplicationRegion()));
        } else if (consistencyLevel.equals("Session")) {
            withCreate = withConsistencyPolicy.withSessionConsistency()
                        .withWriteReplication(Region.fromName(getWriteReplicationRegion()));
        } else if (consistencyLevel.equals("Strong")) {
            withCreate = withConsistencyPolicy.withStrongConsistency();
        } else {
            throw new GyroException("Invalid consistency level. " +
                    "Values are BoundedStaleness, Eventual, Session, and Strong");
        }

        if (getIpRangeFilter() != null) {
            withCreate.withIpRangeFilter(getIpRangeFilter());
        }

        for (String readRegions : getReadReplicationRegions()) {
            withCreate.withReadReplication(Region.fromName(readRegions));
        }

        withCreate.withVirtualNetworkRules(toVirtualNetworkRules());

        CosmosDBAccount cosmosDBAccount = withCreate.withTags(getTags()).create();

        setId(cosmosDBAccount.id());
    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {
        Azure client = createClient();

        CosmosDBAccount.Update update = client.cosmosDBAccounts()
                .getById(getId())
                .update();

        CosmosDBAccountResource oldAccount = (CosmosDBAccountResource) current;

        WithOptionals withOptionals = null;

        if (getConsistencyLevel().equals("BoundedStaleness")) {
            withOptionals = update
                    .withBoundedStalenessConsistency(Long.parseLong(getMaxStalenessPrefix()), getMaxInterval());
        } else if (consistencyLevel.equals("Eventual")) {
            withOptionals = update
                    .withEventualConsistency();
        } else if (consistencyLevel.equals("Session")) {
            withOptionals = update
                    .withSessionConsistency();
        } else if (consistencyLevel.equals("Strong")) {
            withOptionals = update
                    .withStrongConsistency();
        } else {
            throw new GyroException("Invalid consistency level. " +
                    "Values are BoundedStaleness, Eventual, Session, and Strong");
        }

        withOptionals.withIpRangeFilter(getIpRangeFilter());

        List<String> addReadReplicationRegions = getReadReplicationRegions().stream()
                .filter(((Predicate<String>) new HashSet<>(oldAccount.getReadReplicationRegions())::contains).negate())
                .collect(Collectors.toList());

        for (String readRegions : addReadReplicationRegions) {
            update.withReadReplication(Region.fromName(readRegions));
        }

        List<String> removeReadReplicationRegions = oldAccount.getReadReplicationRegions().stream()
                .filter(((Predicate<String>) new HashSet<>(getReadReplicationRegions())::contains).negate())
                .collect(Collectors.toList());

        for (String readRegions : removeReadReplicationRegions) {
            update.withoutReadReplication(Region.fromName(readRegions));
        }

        withOptionals.withVirtualNetworkRules(toVirtualNetworkRules());
        withOptionals.withTags(getTags())
                .apply();
    }

    @Override
    public void delete() {
        Azure client = createClient();

        client.cosmosDBAccounts().deleteById(getId());
    }

    @Override
    public String toDisplayString() {
        return "cosmos database " + getName();
    }

    private List<VirtualNetworkRule> toVirtualNetworkRules() {
        List<VirtualNetworkRule> virtualNetworkRules = new ArrayList<>();
        for (String vnRule : getVirtualNetworkRules()) {
            VirtualNetworkRule rule = new VirtualNetworkRule();
            rule.withId(vnRule);
            virtualNetworkRules.add(rule);
        }
        return virtualNetworkRules;
    }
}
