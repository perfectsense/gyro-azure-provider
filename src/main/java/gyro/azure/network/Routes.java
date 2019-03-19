package gyro.azure.network;

import gyro.core.diff.Diffable;

import com.microsoft.azure.management.network.Route;

public class Routes extends Diffable {

    private String destinationAddressPrefix;
    private String name;
    private String nextHopIpAddress;
    private String nextHopType;

    public Routes() {

    }

    public Routes(Route route) {
        setDestinationAddressPrefix(route.destinationAddressPrefix());
        setName(route.name());
        setNextHopIpAddress(route.nextHopIPAddress());
        setNextHopType(route.nextHopType().toString());
    }

    public String getDestinationAddressPrefix() {
        return destinationAddressPrefix;
    }

    public void setDestinationAddressPrefix(String destinationAddressPrefix) {
        this.destinationAddressPrefix = destinationAddressPrefix;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNextHopIpAddress() {
        return nextHopIpAddress;
    }

    public void setNextHopIpAddress(String nextHopIpAddress) {
        this.nextHopIpAddress = nextHopIpAddress;
    }

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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
             return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Routes route = (Routes) obj;

        return route.getName().equals(this.getName());
    }
}
