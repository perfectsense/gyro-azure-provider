package gyro.azure.compute;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.compute.VirtualMachineScaleSet;
import gyro.azure.AzureFinder;
import gyro.core.Type;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Type("scale-set")
public class VMScaleSetFinder extends AzureFinder<VirtualMachineScaleSet, VMScaleSetResource> {
    private String id;

    /**
     * The ID of the Scale Set.
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    protected List<VirtualMachineScaleSet> findAllAzure(Azure client) {
        return client.virtualMachineScaleSets().list();
    }

    @Override
    protected List<VirtualMachineScaleSet> findAzure(Azure client, Map<String, String> filters) {
        VirtualMachineScaleSet scaleSet = client.virtualMachineScaleSets().getById(filters.get("id"));
        if (scaleSet == null) {
            return Collections.emptyList();
        } else {
            return Collections.singletonList(scaleSet);
        }
    }
}
