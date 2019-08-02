package gyro.azure.network;

import com.microsoft.azure.management.network.Route;
import gyro.azure.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;

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
public class RouteResource extends Diffable implements Copyable<Route> {
    private String destinationAddressPrefix;
    private String name;
    private String nextHopIpAddress;
    private String nextHopType;

    /**
     * The destination address prefix to which the Route applies. Expressed in CIDR notation. (Required)
     */
    @Updatable
    public String getDestinationAddressPrefix() {
        return destinationAddressPrefix;
    }

    public void setDestinationAddressPrefix(String destinationAddressPrefix) {
        this.destinationAddressPrefix = destinationAddressPrefix;
    }

    /**
     * The name of the Route. (Required)
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
    @Updatable
    public String getNextHopIpAddress() {
        return nextHopIpAddress;
    }

    public void setNextHopIpAddress(String nextHopIpAddress) {
        this.nextHopIpAddress = nextHopIpAddress;
    }

    /**
     * The type of the next hop. Valid values are `` Internet`` or ``VirtualAppliance`` or ``VnetLocal`` or ``VirtualNetworkGateway`` or ``None``. (Required)
     */
    @Updatable
    public String getNextHopType() {
        return nextHopType;
    }

    public void setNextHopType(String nextHopType) {
        this.nextHopType = nextHopType;
    }

    @Override
    public String primaryKey() {
        return String.format("%s", getName());
    }

    @Override
    public void copyFrom(Route route) {
        setDestinationAddressPrefix(route.destinationAddressPrefix());
        setName(route.name());
        setNextHopIpAddress(route.nextHopIPAddress());
        setNextHopType(route.nextHopType().toString());
    }
}
