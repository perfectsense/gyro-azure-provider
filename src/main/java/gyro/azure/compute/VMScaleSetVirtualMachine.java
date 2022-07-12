package gyro.azure.compute;

import java.util.Collection;

import com.azure.resourcemanager.compute.models.VirtualMachineScaleSetVM;
import com.azure.resourcemanager.network.fluent.models.NetworkInterfaceIpConfigurationInner;
import gyro.azure.Copyable;
import gyro.core.GyroInstance;
import gyro.core.resource.Diffable;

public class VMScaleSetVirtualMachine extends Diffable implements GyroInstance, Copyable<VirtualMachineScaleSetVM> {

    private String name;
    private String instanceId;
    private String state;
    private String privateIp;
    private String publicIp;
    private String location;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPrivateIp() {
        return privateIp;
    }

    public void setPrivateIp(String privateIp) {
        this.privateIp = privateIp;
    }

    public String getPublicIp() {
        return publicIp;
    }

    public void setPublicIp(String publicIp) {
        this.publicIp = publicIp;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @Override
    public void copyFrom(VirtualMachineScaleSetVM model) {
        setName(model.computerName());
        setInstanceId(model.instanceId());
        setState(model.powerState().toString());
        setLocation(model.innerModel().location());
        //model
        NetworkInterfaceIpConfigurationInner ipConfig = model.listNetworkInterfaces()
            .stream()
            .filter(nic -> nic.name().equals("primary-nic-cfg"))
            .map(nic -> nic.innerModel().ipConfigurations())
            .flatMap(Collection::stream)
            .filter(NetworkInterfaceIpConfigurationInner::primary)
            .findFirst()
            .orElse(null);

        if (ipConfig != null) {
            setPrivateIp(ipConfig.privateIpAddress());
            setPublicIp(ipConfig.publicIpAddress() != null ? ipConfig.publicIpAddress().ipAddress() : null);
        }

    }

    @Override
    public String getGyroInstanceId() {
        return getInstanceId();
    }

    @Override
    public String getGyroInstanceState() {
        return getState();
    }

    @Override
    public String getGyroInstancePrivateIpAddress() {
        return getPrivateIp();
    }

    @Override
    public String getGyroInstancePublicIpAddress() {
        return getPublicIp();
    }

    @Override
    public String getGyroInstanceHostname() {
        return getGyroInstancePublicIpAddress() != null
            ? getGyroInstancePublicIpAddress()
            : getGyroInstancePrivateIpAddress();
    }

    @Override
    public String getGyroInstanceName() {
        return getName();
    }

    @Override
    public String getGyroInstanceLaunchDate() {
        return null;
    }

    @Override
    public String getGyroInstanceLocation() {
        return getLocation();
    }

    @Override
    public String primaryKey() {
        return getGyroInstanceId();
    }
}
