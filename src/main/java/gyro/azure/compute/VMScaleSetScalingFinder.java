package gyro.azure.compute;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.monitor.AutoscaleSetting;
import gyro.azure.AzureFinder;
import gyro.core.Type;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Query scale set scaling.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    scale-set-scaling: $(external-query azure::scale-set-scaling {})
 */
@Type("scale-set-scaling")
public class VMScaleSetScalingFinder extends AzureFinder<AutoscaleSetting, VMScaleSetScalingResource> {
    private String id;

    /**
     * The ID of the Scale Set Scaling.
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    protected List<AutoscaleSetting> findAllAzure(Azure client) {
        return client.autoscaleSettings().list();
    }

    @Override
    protected List<AutoscaleSetting> findAzure(Azure client, Map<String, String> filters) {
        AutoscaleSetting setting = client.autoscaleSettings().getById(filters.get("id"));
        if (setting == null) {
            return Collections.emptyList();
        } else {
            return Collections.singletonList(setting);
        }
    }
}
