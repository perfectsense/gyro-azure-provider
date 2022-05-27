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

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.azure.resourcemanager.AzureResourceManager;
import com.microsoft.azure.management.Azure;
import gyro.core.GyroCore;
import gyro.core.GyroException;
import gyro.core.GyroInputStream;
import gyro.core.LocalFileBackend;
import gyro.core.auth.Credentials;
import gyro.core.auth.CredentialsSettings;
import gyro.core.scope.RootScope;
import gyro.lang.ast.Node;
import gyro.lang.ast.block.FileNode;
import gyro.util.Bug;
import org.apache.commons.lang3.StringUtils;
import picocli.CommandLine.Option;

public abstract class AbstractAzureCommand {

    @Option(names = "--credential", description = "The azure credentials to be used as defined in the project init file. When not specified the 'default' credential is used.")
    private String credential;

    private RootScope scope;

    public String getCredential() {
        if (credential == null) {
            credential = "default";
        }

        return credential;
    }

    public RootScope getScope() {
        if (scope == null) {
            setScope();
        }

        return scope;
    }

    private void setScope() {
        Path rootDir = GyroCore.getRootDirectory();

        if (rootDir == null) {
            throw new GyroException(
                "Not a gyro project directory, use 'gyro init <plugins>...' to create one. See 'gyro help init' for detailed usage.");
        }

        RootScope current = new RootScope(
            "../../" + GyroCore.INIT_FILE,
            new LocalFileBackend(rootDir.resolve(".gyro/state")),
            null,
            null);
        List<Node> nodes = current.load();
        Set<String> existingFiles;
        try (Stream<String> s = current.list()) {
            existingFiles = s.collect(Collectors.toCollection(LinkedHashSet::new));
        }
        existingFiles.forEach(f -> evaluateFile(f, nodes::add, current));
        current.getEvaluator().evaluate(current, nodes);
        this.scope = current;
    }

    public Azure getClient() {
        Credentials credentials = getScope().getSettings(CredentialsSettings.class)
            .getCredentialsByName()
            .get("azure::" + getCredential());

        if (credentials == null) {
            throw new GyroException(String.format(
                "No credentials with name - '%s' found. Check the your project init file.",
                getCredential()));
        }

        return AzureResource.createClient((AzureCredentials) credentials);
    }

    public AzureResourceManager getResourceManagerClient() {
        Credentials credentials = getScope().getSettings(CredentialsSettings.class)
            .getCredentialsByName()
            .get("azure::" + getCredential());

        if (credentials == null) {
            throw new GyroException(String.format(
                "No credentials with name - '%s' found. Check the your project init file.",
                getCredential()));
        }

        return AzureResource.createResourceManagerClient((AzureCredentials) credentials);
    }

    private void evaluateFile(String file, Consumer<FileNode> consumer, RootScope current) {
        if (StringUtils.isBlank(file)) {
            return;
        }

        try (GyroInputStream input = current.openInput(file)) {
            consumer.accept((FileNode) Node.parse(input, file, gyro.parser.antlr4.GyroParser::file));

        } catch (IOException error) {
            throw new Bug(error);

        } catch (Exception error) {
            throw new GyroException(
                String.format("Can't parse @|bold %s|@ in @|bold %s|@!", file, current.getBackend()),
                error);
        }
    }
}
