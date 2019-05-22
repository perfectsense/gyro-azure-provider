package gyro.azure;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.credentials.AzureTokenCredentials;
import com.microsoft.azure.management.Azure;
import com.microsoft.rest.LogLevel;
import gyro.core.GyroException;
import gyro.core.auth.Credentials;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AzureCredentials extends Credentials {

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

    public Azure createClient() {
        AzureEnvironment environment = AzureEnvironment.AZURE;
        Properties properties;

        try (InputStream input = openInput(getCredentialFilePath())) {
            properties = new Properties();

            properties.load(input);

        } catch (IOException error) {
            throw new GyroException(error.getMessage());
        }

        AzureTokenCredentials credentials = new ApplicationTokenCredentials(
            (String) properties.get("client"),
            (String) properties.get("tenant"),
            (String) properties.get("key"),
            environment);

        try {
            return Azure.configure()
                .withLogLevel(LogLevel.valueOf(getLogLevel()))
                .authenticate(credentials)
                .withDefaultSubscription();

        } catch (IOException error) {
            throw new GyroException(error.getMessage(), error);
        }
    }

}
