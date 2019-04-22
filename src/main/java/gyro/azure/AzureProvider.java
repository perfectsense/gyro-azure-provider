package gyro.azure;

import gyro.core.plugin.Provider;

public class AzureProvider extends Provider {
    @Override
    public String name() {
        return "azure";
    }
}
