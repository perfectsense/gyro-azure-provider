package gyro.azure.network;

import gyro.core.resource.Diffable;
import gyro.core.resource.ResourceUpdatable;

/**
 * Creates a route in a route table.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     route
 *         destination-address-prefix: "10.0.1.0/24"
 *         name: "test-route"
 *         next-hop-type: "VirtualAppliance"
 *         next-hop-ip-address: "10.0.2.4"
 *     end
 */
public class Route extends Diffable {

    private String destinationAddressPrefix;
    private String name;
    private String nextHopIpAddress;
    private String nextHopType;

    public Route() {

    }

    public Route(com.microsoft.azure.management.network.Route route) {
        setDestinationAddressPrefix(route.destinationAddressPrefix());
        setName(route.name());
        setNextHopIpAddress(route.nextHopIPAddress());
        setNextHopType(route.nextHopType().toString());
    }

    /**
     * The destination address prefix to which the route applies. Expressed in CIDR notation. (Required)
     */
    @ResourceUpdatable
    public String getDestinationAddressPrefix() {
        return destinationAddressPrefix;
    }

    public void setDestinationAddressPrefix(String destinationAddressPrefix) {
        this.destinationAddressPrefix = destinationAddressPrefix;
    }

    /**
     * The name of the route. (Required)
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The IP address of the next hop. (Required)
     */
    @ResourceUpdatable
    public String getNextHopIpAddress() {
        return nextHopIpAddress;
    }

    public void setNextHopIpAddress(String nextHopIpAddress) {
        this.nextHopIpAddress = nextHopIpAddress;
    }

    /**
     * The type of the next hop. Options are: Internet, VirtualAppliance, VnetLocal, VirtualNetworkGateway, None (Required)
     */
    @ResourceUpdatable
    public String getNextHopType() {
        return nextHopType;
    }

    public void setNextHopType(String nextHopType) {
        this.nextHopType = nextHopType;
    }

    public String primaryKey() {
        return String.format("%s", getName());
    }

    @Override
    public String toDisplayString() {
        return "route " + getName();
    }

}
