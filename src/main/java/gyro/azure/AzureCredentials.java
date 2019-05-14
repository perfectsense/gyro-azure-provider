package gyro.azure;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.credentials.AzureTokenCredentials;
import com.microsoft.azure.management.Azure;
import com.microsoft.rest.LogLevel;
import gyro.core.Credentials;
import gyro.core.GyroException;
import gyro.core.resource.ResourceType;

import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Properties;

@ResourceType("credentials")
public class AzureCredentials extends Credentials<Azure.Authenticated> {

    private String region;

    private String credentialFilePath;

    private String logLevel;

    public String getRegion() {
        return region != null ? region.toUpperCase() : null;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getCredentialFilePath() {
        return credentialFilePath;
    }

    public void setCredentialFilePath(String credentialFilePath) {
        this.credentialFilePath = credentialFilePath;
    }

    public String getLogLevel() {
        return logLevel != null ? logLevel.toUpperCase() : null;
    }

    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }

    @Override
    public String getCloudName() {
        return "azure";
    }

    @Override
    public Azure.Authenticated findCredentials(boolean refresh) {
        AzureEnvironment environment = AzureEnvironment.AZURE;
        Properties credentialsProperties = loadProperties();
        AzureTokenCredentials credentials = new ApplicationTokenCredentials(
            (String) credentialsProperties.get("client"),
            (String) credentialsProperties.get("tenant"),
            (String) credentialsProperties.get("key"),
            environment
        );

        return Azure.configure()
            .withLogLevel(LogLevel.valueOf(getLogLevel()))
            .authenticate(credentials);
    }

    @Override
    public Azure.Authenticated findCredentials(boolean refresh, boolean extended) {
        return findCredentials(refresh);
    }

    private Properties loadProperties() {
        try (InputStream input = getRelativeCredentialsPath()) {
            Properties props = new Properties();
            props.load(input);

            return props;
        } catch (Exception ex) {
            throw new GyroException(ex.getMessage());
        }
    }

    private InputStream getRelativeCredentialsPath() throws Exception {
        return openInput(getCredentialFilePath());
    }
}
