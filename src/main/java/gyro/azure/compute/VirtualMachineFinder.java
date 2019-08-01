package gyro.azure.compute;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.compute.VirtualMachine;
import gyro.azure.AzureFinder;
import gyro.core.Type;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Type("virtual-machine")
public class VirtualMachineFinder extends AzureFinder<VirtualMachine, VirtualMachineResource> {
    private String id;

    /**
     * The ID of the Virtual Machine.
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    @Override
    protected List<VirtualMachine> findAllAzure(Azure client) {
        PagedList<VirtualMachine> list =  client.virtualMachines().list();
        list.loadAll();
        return list;
    }

    @Override
    protected List<VirtualMachine> findAzure(Azure client, Map<String, String> filters) {
        VirtualMachine virtualMachine = client.virtualMachines().getById(filters.get("id"));

        if (virtualMachine == null) {
            return Collections.emptyList();
        } else {
            return Collections.singletonList(virtualMachine);
        }
    }
}
