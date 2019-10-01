package gyro.azure.compute;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.compute.VirtualMachine;
import gyro.azure.AzureFinder;
import gyro.core.Type;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Query virtual machine.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    virtual-machine: $(external-query azure::virtual-machine {})
 */
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
        return client.virtualMachines().list();
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
