package gyro.azure.network;

import com.microsoft.azure.management.Azure;
import gyro.azure.AzureResource;


import gyro.core.diff.Diffable;
import com.microsoft.azure.management.network.model.HasNetworkInterfaces;

import java.util.ArrayList;
import java.util.List;

public class BackendPool extends Diffable {

    private String backendPoolName;
    private List<String> virtualMachineIds;

    public String getBackendPoolName() {
        return backendPoolName;
    }

    public void setBackendPoolName(String backendPoolName) {
        this.backendPoolName = backendPoolName;
    }

    public List<String> getVirtualMachineIds() {
        return virtualMachineIds;
    }

    public void setVirtualMachineIds(List<String> virtualMachineIds) {
        this.virtualMachineIds = virtualMachineIds;
    }

    public String primaryKey() {
        return String.format("%s", getBackendPoolName());
    }

    @Override
    public String toDisplayString() {
        return "backend pool " + getBackendPoolName();
    }

    /*
    public List<HasNetworkInterfaces> toBackend() {
        Azure client = createClient();

        List<HasNetworkInterfaces> virtualMachines = new ArrayList<>();
        getVirtualMachineIds().stream().forEach(vm -> virtualMachines.add(client.virtualMachines().getById(vm)));

        return virtualMachines;
    }*/
}
