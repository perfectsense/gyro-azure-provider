package gyro.azure.compute;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.compute.VirtualMachineCustomImage;
import gyro.azure.AzureFinder;
import gyro.core.Type;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Query virtual machine image.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    virtual-machine-image: $(external-query azure::virtual-machine-image {})
 */
@Type("virtual-machine-image")
public class VirtualMachineImageFinder extends AzureFinder<VirtualMachineCustomImage, VirtualMachineImageResource> {
    private String id;

    /**
     * The ID of the virtual machine image.
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    protected List<VirtualMachineCustomImage> findAllAzure(Azure client) {
        return client.virtualMachineCustomImages().list();
    }

    @Override
    protected List<VirtualMachineCustomImage> findAzure(Azure client, Map<String, String> filters) {
        VirtualMachineCustomImage image = client.virtualMachineCustomImages().getById(filters.get("id"));

        if (image == null) {
            return Collections.emptyList();
        } else {
            return Collections.singletonList(image);
        }
    }
}
