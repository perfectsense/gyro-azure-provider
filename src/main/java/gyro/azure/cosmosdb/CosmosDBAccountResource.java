package gyro.azure.cosmosdb;

import gyro.azure.AzureResource;
import gyro.azure.Copyable;
import gyro.azure.resources.ResourceGroupResource;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.resource.Id;
import gyro.core.resource.Resource;
import gyro.core.resource.Output;
import gyro.core.Type;
import gyro.core.resource.Updatable;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.cosmosdb.CosmosDBAccount;
import com.microsoft.azure.management.cosmosdb.Location;
import com.microsoft.azure.management.cosmosdb.VirtualNetworkRule;
import com.microsoft.azure.management.cosmosdb.CosmosDBAccount.DefinitionStages.WithCreate;
import com.microsoft.azure.management.cosmosdb.CosmosDBAccount.DefinitionStages.WithConsistencyPolicy;
import com.microsoft.azure.management.cosmosdb.CosmosDBAccount.DefinitionStages.WithKind;
import com.microsoft.azure.management.cosmosdb.CosmosDBAccount.UpdateStages.WithOptionals;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import gyro.core.scope.State;
import gyro.core.validation.Range;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
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
 *             consistency-level: "Session"
 *             ip-range-filter: "10.1.0.0"
 *             name: "cosmos-db-example"
 *             read-replication-regions: ["Central US"]
 *             resource-group: $(azure::resource-group resource-group-cosmos-db-example)
 *             tags: {
 *                 Name: "network-interface-example"
 *             }
 *             virtual-network-rules: [$(azure::network network-example).subnet.0.id]
 *             write-replication-region: "Canada East"
 *         end
 */
@Type("cosmos-db")
public class CosmosDBAccountResource extends AzureResource implements Copyable<CosmosDBAccount> {

    private static final String KIND_AZURETABLE = "AzureTable";
    private static final String KIND_CASSANDRA = "Cassandra";
    private static final String KIND_GREMLIN = "Gremlin";
    private static final String KIND_MONGODB = "MongoDB";
    private static final String KIND_SQL = "Sql";
    private static final String LEVEL_BOUNDED = "BoundedStaleness";
    private static final String LEVEL_EVENTUAL = "Eventual";
    private static final String LEVEL_SESSION = "Session";
    private static final String LEVEL_STRONG = "Strong";

    private String databaseAccountKind;
    private String consistencyLevel;
    private String id;
    private String ipRangeFilter;
    private Integer maxInterval;
    private Long maxStalenessPrefix;
    private String name;
    private List<String> readReplicationRegions;
    private ResourceGroupResource resourceGroup;
    private Map<String, String> tags;
    private List<String> virtualNetworkRules;
    private String writeReplicationRegion;

    /**
     * The database account kind. Valid values are ``AzureTable`` or ``Cassandra`` or ``Gremlin`` or ``MongoDB`` or ``Sql``. (Required)
     */
    @Required
    @ValidStrings({"AzureTable", "Cassandra", "Gremlin", "MongoDB", "Sql"})
    public String getDatabaseAccountKind() {
        return databaseAccountKind;
    }

    public void setDatabaseAccountKind(String databaseAccountKind) {
        this.databaseAccountKind = databaseAccountKind;
    }

    /**
     * The consistency policy of the account. Valid values are ``BoundedStaleness``or ``Eventual`` or ``Session`` or ``Strong``. (Required)
     */
    @Required
    @ValidStrings({"BoundedStaleness", "Eventual", "Session", "Strong"})
    @Updatable
    public String getConsistencyLevel() {
        return consistencyLevel;
    }

    public void setConsistencyLevel(String consistencyLevel) {
        this.consistencyLevel = consistencyLevel;
    }

    /**
     * The ID of the database.
     */
    @Id
    @Output
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * This value specifies the set of IP addresses or IP address ranges in CIDR form to be included as the allowed list of client IP's for a given database account. IP addresses/ranges must be comma separated and must not contain any spaces. (Optional)
     */
    @Updatable
    public String getIpRangeFilter() {
        return ipRangeFilter;
    }

    public void setIpRangeFilter(String ipRangeFilter) {
        this.ipRangeFilter = ipRangeFilter;
    }

    /**
     * The time amount of staleness (in seconds) tolerated. Valid values are ``5`` to ``86400``. Required when used with ``BoundedStaleness`` consistency policy. (Optional)
     */
    @Range(min = 5, max = 86400)
    @Updatable
    public Integer getMaxInterval() {
        return maxInterval;
    }

    public void setMaxInterval(Integer maxInterval) {
        this.maxInterval = maxInterval;
    }

    /**
     * This value represents the number of stale requests tolerated. Valid values are ``10`` to ``2147483647``. Required when used with ``BoundedStaleness`` consistency policy. (Optional)
     */
    @Range(min = 10, max = 2147483647)
    @Updatable
    public Long getMaxStalenessPrefix() {
        return maxStalenessPrefix;
    }

    public void setMaxStalenessPrefix(Long maxStalenessPrefix) {
        this.maxStalenessPrefix = maxStalenessPrefix;
    }

    /**
     * Name of the account. (Required)
     */
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the read location regions. (Optional)
     */
    @Updatable
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
    @Required
    public ResourceGroupResource getResourceGroup() {
        return resourceGroup;
    }

    public void setResourceGroup(ResourceGroupResource resourceGroup) {
        this.resourceGroup = resourceGroup;
    }

    /**
     * The tags associated with the account. (Optional)
     */
    @Updatable
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
     * The virtual network rules associated with the account. A list of Subnet ID is required. (Optional)
     */
    @Updatable
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
     * Sets the write location. Required when used with ``BoundedStaleness``, ``Eventual``, and ``Session`` consistency levels. (Optional)
     */
    @Updatable
    public String getWriteReplicationRegion() {
        return writeReplicationRegion;
    }

    public void setWriteReplicationRegion(String writeReplicationRegion) {
        this.writeReplicationRegion = writeReplicationRegion;
    }

    @Override
    public void copyFrom(CosmosDBAccount cosmosAccount) {
        setResourceGroup(findById(ResourceGroupResource.class, cosmosAccount.resourceGroupName()));
        setConsistencyLevel(cosmosAccount.consistencyPolicy().defaultConsistencyLevel().toString());
        setId(cosmosAccount.id());
        setIpRangeFilter(cosmosAccount.ipRangeFilter());

        if (LEVEL_BOUNDED.equalsIgnoreCase(getConsistencyLevel())) {
            setMaxInterval(cosmosAccount.consistencyPolicy().maxIntervalInSeconds());
            setMaxStalenessPrefix(cosmosAccount.consistencyPolicy().maxStalenessPrefix());
        }
        setName(cosmosAccount.name());

        Map<Integer, String> priorities = new TreeMap<>();
        getReadReplicationRegions().clear();
        for (Location location : cosmosAccount.readableReplications()) {
            priorities.put(location.failoverPriority(), location.locationName());
        }

        setWriteReplicationRegion(priorities.remove(0));
        setReadReplicationRegions(new ArrayList<>(priorities.values()));

        setTags(cosmosAccount.tags());

        getVirtualNetworkRules().clear();
        cosmosAccount.virtualNetworkRules().forEach(rule -> getVirtualNetworkRules().add(rule.id()));
    }

    @Override
    public boolean refresh() {
        Azure client = createClient();

        CosmosDBAccount cosmosAccount = client.cosmosDBAccounts().getById(getId());

        if (cosmosAccount == null) {
            return false;
        }

        copyFrom(cosmosAccount);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) {
        Azure client = createClient();

        WithKind withKind = client.cosmosDBAccounts()
                .define(getName())
                .withRegion(Region.fromName(getRegion()))
                .withExistingResourceGroup(getResourceGroup().getName());

        WithConsistencyPolicy withConsistencyPolicy = null;
        if (KIND_AZURETABLE.equalsIgnoreCase(getDatabaseAccountKind())) {
            withConsistencyPolicy = withKind.withDataModelAzureTable();
        } else if (KIND_CASSANDRA.equalsIgnoreCase(getDatabaseAccountKind())) {
            withConsistencyPolicy = withKind.withDataModelCassandra();
        } else if (KIND_GREMLIN.equalsIgnoreCase(getDatabaseAccountKind())) {
            withConsistencyPolicy = withKind.withDataModelGremlin();
        } else if (KIND_MONGODB.equalsIgnoreCase(getDatabaseAccountKind())) {
            withConsistencyPolicy = withKind.withDataModelMongoDB();
        } else if (KIND_SQL.equalsIgnoreCase(getDatabaseAccountKind())) {
            withConsistencyPolicy = withKind.withDataModelSql();
        }

        WithCreate withCreate = null;
        if (LEVEL_BOUNDED.equalsIgnoreCase(getConsistencyLevel())
                && getMaxStalenessPrefix() != null
                && getMaxInterval() != null) {
            withCreate = withConsistencyPolicy
                    .withBoundedStalenessConsistency(getMaxStalenessPrefix(), getMaxInterval())
                    .withWriteReplication(Region.fromName(getWriteReplicationRegion()));
        } else if (LEVEL_EVENTUAL.equalsIgnoreCase(getConsistencyLevel())) {
            withCreate = withConsistencyPolicy.withEventualConsistency()
                        .withWriteReplication(Region.fromName(getWriteReplicationRegion()));
        } else if (LEVEL_SESSION.equalsIgnoreCase(getConsistencyLevel())) {
            withCreate = withConsistencyPolicy.withSessionConsistency()
                        .withWriteReplication(Region.fromName(getWriteReplicationRegion()));
        } else if (LEVEL_STRONG.equalsIgnoreCase(getConsistencyLevel())) {
            withCreate = withConsistencyPolicy.withStrongConsistency();
        }

        if (getIpRangeFilter() != null) {
            withCreate = withCreate.withIpRangeFilter(getIpRangeFilter());
        }

        for (String region : getReadReplicationRegions()) {
            withCreate.withReadReplication(Region.fromName(region));
        }

        withCreate.withVirtualNetworkRules(toVirtualNetworkRules());
        withCreate.withTags(getTags());

        CosmosDBAccount cosmosDBAccount = withCreate.create();

        setId(cosmosDBAccount.id());
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedProperties) {
        Azure client = createClient();

        CosmosDBAccount.Update update = client.cosmosDBAccounts()
                .getById(getId())
                .update();

        CosmosDBAccountResource oldAccount = (CosmosDBAccountResource) current;

        WithOptionals withOptionals = null;

        List<String> addReadReplicationRegions = getReadReplicationRegions().stream()
                .filter(((Predicate<String>) new HashSet<>(oldAccount.getReadReplicationRegions())::contains).negate())
                .collect(Collectors.toList());

        for (String readRegions : addReadReplicationRegions) {
            update.withReadReplication(Region.fromName(readRegions));
        }

        if (!getReadReplicationRegions().contains(getWriteReplicationRegion())) {
            update.withReadReplication(Region.fromName(getWriteReplicationRegion()));
        }

        List<String> removeReadReplicationRegions = oldAccount.getReadReplicationRegions().stream()
                .filter(((Predicate<String>) new HashSet<>(getReadReplicationRegions())::contains).negate())
                .collect(Collectors.toList());

        for (String readRegions : removeReadReplicationRegions) {
            update.withoutReadReplication(Region.fromName(readRegions));
        }

        if (LEVEL_BOUNDED.equalsIgnoreCase(getConsistencyLevel())
                && getMaxStalenessPrefix() != null
                && getMaxInterval() != null) {
            withOptionals = update
                    .withBoundedStalenessConsistency(getMaxStalenessPrefix(), getMaxInterval());
        } else if (LEVEL_EVENTUAL.equalsIgnoreCase(getConsistencyLevel())) {
            withOptionals = update.withEventualConsistency();
        } else if (LEVEL_SESSION.equalsIgnoreCase(getConsistencyLevel())) {
            withOptionals = update.withSessionConsistency();
        } else if (LEVEL_STRONG.equalsIgnoreCase(getConsistencyLevel())) {
            withOptionals = update.withStrongConsistency();
        }

        withOptionals.withIpRangeFilter(getIpRangeFilter());
        withOptionals.withVirtualNetworkRules(toVirtualNetworkRules());
        withOptionals.withTags(getTags());
        update.apply();

        List<Location> locations = new ArrayList<>();

        Location writeLocation = new Location();
        writeLocation.withLocationName(getWriteReplicationRegion());
        writeLocation.withFailoverPriority(0);
        locations.add(writeLocation);

        int priority = 1;
        for (String readReplicationRegion : getReadReplicationRegions()) {
            Location readLocation = new Location();
            readLocation.withLocationName(readReplicationRegion);
            readLocation.withFailoverPriority(priority++);
            locations.add(readLocation);
        }

        client.cosmosDBAccounts().failoverPriorityChange(getResourceGroup().getName(), getName(), locations);
    }

    @Override
    public void delete(GyroUI ui, State state) {
        Azure client = createClient();

        client.cosmosDBAccounts().deleteById(getId());
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
