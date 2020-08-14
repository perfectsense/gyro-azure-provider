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

import java.util.List;

import gyro.azure.keyvault.AbstractVaultCommand;
import gyro.azure.keyvault.AzureKeyVaultCommand;
import gyro.azure.network.AbstractApplicationGatewayCommand;
import gyro.azure.network.AzureApplicationGatewayCommand;
import gyro.core.command.GyroCommand;
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "azure", description = "CLI command for all things azure.", mixinStandardHelpOptions = true, subcommands = {
    AzureKeyVaultCommand.class,
    AzureApplicationGatewayCommand.class })
public class AzureCommand implements GyroCommand {

    public static Reflections reflections;

    @Parameters(description = "", arity = "1")
    private List<String> arguments;

    public static Reflections getReflections() {
        if (reflections == null) {
            reflections = new Reflections(new org.reflections.util.ConfigurationBuilder()
                .setUrls(ClasspathHelper.forPackage("gyro.azure")));
        }

        return reflections;
    }

    @Override
    public void execute() throws Exception {
        /*Cli.CliBuilder<Object> builder = Cli.builder("azure")
            .withDescription("CLI command for all things azure")
            .withDefaultCommand(Help.class)
            .withCommands(Help.class);

        // Vault command loader
        AbstractVaultCommand.setVaultCommand(builder);

        // Application gateway command loader
        AbstractApplicationGatewayCommand.setApplicationGatewayCommand(builder);

        Cli<Object> gitParser = builder.build();

        Object command = gitParser.parse(arguments);

        if (command instanceof Runnable) {
            ((Runnable) command).run();
        } else if (command instanceof GyroCommand) {
            ((GyroCommand) command).execute();
        } else {
            throw new IllegalStateException(String.format(
                "[%s] must be an instance of [%s] or [%s]!",
                command.getClass().getName(),
                Runnable.class.getName(),
                GyroCommand.class.getName()));
        }*/
    }
}
