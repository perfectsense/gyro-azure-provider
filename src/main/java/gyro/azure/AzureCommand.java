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

package gyro.azure;

import gyro.azure.keyvault.AzureKeyVaultCommand;
import gyro.azure.network.AzureApplicationGatewayCommand;
import gyro.core.command.GyroCommand;
import picocli.CommandLine.Command;

@Command(name = "azure",
    description = "Manage azure assets.",
    synopsisHeading = "%n",
    header = "Add, remove, or list assets part of key-vault and application-gateway.",
    descriptionHeading = "%nDescription:%n%n",
    parameterListHeading = "%nParameters:%n",
    optionListHeading = "%nOptions:%n",
    commandListHeading = "%nCommands:%n",
    usageHelpWidth = 100,
    subcommands = {
        AzureKeyVaultCommand.class,
        AzureApplicationGatewayCommand.class
    }
)
public class AzureCommand implements GyroCommand {

    @Override
    public void execute() throws Exception {

    }
}
