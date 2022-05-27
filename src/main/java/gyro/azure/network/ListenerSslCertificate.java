package gyro.azure.network;

import com.azure.resourcemanager.network.models.ApplicationGatewaySslCertificate;
import gyro.azure.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.validation.Required;

public class ListenerSslCertificate extends Diffable implements Copyable<ApplicationGatewaySslCertificate> {

    private String certificateSecretId;
    private String certificateName;

    /**
     * The secret id of a certificate in key vault to attach to the listener. If this has been to attached to any other listener of this application gateway before, only use the `certificate-name` field.
     */
    public String getCertificateSecretId() {
        return certificateSecretId;
    }

    public void setCertificateSecretId(String certificateSecretId) {
        this.certificateSecretId = certificateSecretId;
    }

    /**
     * The name of the certificate to attach to the listener.
     */
    @Required
    public String getCertificateName() {
        return certificateName;
    }

    public void setCertificateName(String certificateName) {
        this.certificateName = certificateName;
    }

    @Override
    public void copyFrom(ApplicationGatewaySslCertificate sslCertificate) {
        setCertificateName(sslCertificate.name());
        setCertificateSecretId(sslCertificate.keyVaultSecretId());
    }

    @Override
    public String primaryKey() {
        return getCertificateName();
    }
}
