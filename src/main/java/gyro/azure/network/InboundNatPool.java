package gyro.azure.network;

import gyro.core.resource.Diffable;
import gyro.core.resource.ResourceUpdatable;

import com.microsoft.azure.management.network.LoadBalancerInboundNatPool;
import com.microsoft.azure.management.network.TransportProtocol;

/**
 * Creates a nat pool.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *         inbound-nat-pool
 *             name: "testnatpool"
 *             frontend-name: "test-frontend"
 *             backend-port: 80
 *             protocol: "TCP"
 *             frontend-port-range-start: 80
 *             frontend-port-range-end: 89
 *         end
 */
public class InboundNatPool extends Diffable {

    private Integer backendPort;
    private String frontendName;
    private Integer frontendPortRangeStart;
    private Integer frontendPortRangeEnd;
    private String name;
    private String protocol;

    public InboundNatPool() {}

    public InboundNatPool(LoadBalancerInboundNatPool natPool) {
        setBackendPort(natPool.backendPort());
        setFrontendName(natPool.frontend() != null ? natPool.frontend().name() : null);
        setFrontendPortRangeStart(natPool.frontendPortRangeStart());
        setFrontendPortRangeEnd(natPool.frontendPortRangeEnd());
        setName(natPool.name());
        setProtocol(natPool.protocol() == TransportProtocol.TCP ? "TCP" : "UDP");
    }

    /**
     * The port number that network traffic is sent to. (Required)
     */
    @ResourceUpdatable
    public Integer getBackendPort() {
        return backendPort;
    }

    public void setBackendPort(Integer backendPort) {
        this.backendPort = backendPort;
    }

    /**
     * The name of the frontend that this nat pool is associated with. (Required)
     */
    @ResourceUpdatable
    public String getFrontendName() {
        return frontendName;
    }

    public void setFrontendName(String frontendName) {
        this.frontendName = frontendName;
    }

    /**
     * The starting number of the frontend port. (Required)
     */
    @ResourceUpdatable
    public Integer getFrontendPortRangeStart() {
        return frontendPortRangeStart;
    }

    public void setFrontendPortRangeStart(Integer frontendPortRangeStart) {
        this.frontendPortRangeStart = frontendPortRangeStart;
    }

    /**
     * The ending number of the frontend port. (Required)
     */
    @ResourceUpdatable
    public Integer getFrontendPortRangeEnd() {
        return frontendPortRangeEnd;
    }

    public void setFrontendPortRangeEnd(Integer frontendPortRangeEnd) {
        this.frontendPortRangeEnd = frontendPortRangeEnd;
    }

    /**
     * The name of the inbound nat pool. (Required)
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The protocol used by the nat pool. (Required)
     */
    @ResourceUpdatable
    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String primaryKey() {
        return String.format("%s", getName());
    }

    @Override
    public String toDisplayString() {
        return "inbound nat pool " + getName();
    }

}
