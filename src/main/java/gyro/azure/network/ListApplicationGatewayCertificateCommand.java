/*
 * Copyright 2020, Perfect Sense, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gyro.azure.network;

import java.util.ArrayList;
import java.util.List;

import com.microsoft.azure.management.network.ApplicationGateway;
import com.microsoft.azure.management.network.ApplicationGatewaySslCertificate;
import gyro.core.GyroCore;
import gyro.core.GyroException;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "list-certificate",
    header = "List all certificates present in an Azure application gateway.",
    synopsisHeading = "%n",
    parameterListHeading = "%nParameters:%n",
    optionListHeading = "%nOptions:%n",
    usageHelpWidth = 100)
public class ListApplicationGatewayCertificateCommand extends AbstractApplicationGatewayCommand {

    @Parameters(description = "The command requires one argument. <application-gateway-name>: the application gateway resource name used in the config whose certificates would be listed.", arity = "1")
    private List<String> arguments;

    @Option(names = "--show-data", description = "Show data of the certificate.")
    private boolean showData;

    @Option(names = "--show-secret-id", description = "Show secret id of the certificate.")
    private boolean showSecretId;

    @Override
    public void execute() throws Exception {
        if (arguments.size() == 1) {
            String applicationGatewayResourceName = arguments.get(0);

            ApplicationGateway applicationGateway = getApplicationGateway(applicationGatewayResourceName);

            List<ApplicationGatewaySslCertificate> sslCertificates = new ArrayList<>(applicationGateway.sslCertificates()
                .values());

            if (!sslCertificates.isEmpty()) {
                for (ApplicationGatewaySslCertificate certificate : sslCertificates) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("\n***********************");
                    sb.append(String.format("\nName: %s", certificate.name()));
                    sb.append(String.format("\nKey: %s", certificate.key()));

                    if (showSecretId) {
                        sb.append(String.format("\nSecret-Id: %s", certificate.keyVaultSecretId()));
                    }

                    if (showData) {
                        sb.append(String.format("\nData: %s", certificate.publicData()));
                    }

                    GyroCore.ui().write(sb.toString());
                }

            } else {
                GyroCore.ui().write("No certificates found!");
            }

        } else {
            throw new GyroException("'List-certificate' needs exactly one argument, <application-gateway-name>");
        }
    }
}
