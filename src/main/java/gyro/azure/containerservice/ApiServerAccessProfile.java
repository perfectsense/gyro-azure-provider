/*
 * Copyright 2022, Brightspot, Inc.
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

package gyro.azure.containerservice;

import java.util.ArrayList;
import java.util.List;

import com.azure.resourcemanager.containerservice.models.ManagedClusterApiServerAccessProfile;
import gyro.azure.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.validation.ValidStrings;

public class ApiServerAccessProfile extends Diffable implements Copyable<ManagedClusterApiServerAccessProfile> {

    private Boolean enablePrivateCluster;
    private Boolean enablePrivateClusterPublicFqdn;
    private Boolean disableRunCommand;
    private String privateDnsZone;
    private List<String> authorizedIpRanges;

    /**
     * Enable private cluster.
     */
    public Boolean getEnablePrivateCluster() {
        return enablePrivateCluster;
    }

    public void setEnablePrivateCluster(Boolean enablePrivateCluster) {
        this.enablePrivateCluster = enablePrivateCluster;
    }

    /**
     * When set to ``true`` enables public fqdn on the private cluster.
     */
    public Boolean getEnablePrivateClusterPublicFqdn() {
        return enablePrivateClusterPublicFqdn;
    }

    public void setEnablePrivateClusterPublicFqdn(Boolean enablePrivateClusterPublicFqdn) {
        this.enablePrivateClusterPublicFqdn = enablePrivateClusterPublicFqdn;
    }

    /**
     * If set ot ``true`` disables run command.
     */
    public Boolean getDisableRunCommand() {
        return disableRunCommand;
    }

    public void setDisableRunCommand(Boolean disableRunCommand) {
        this.disableRunCommand = disableRunCommand;
    }

    /**
     * The private dns mode.
     */
    @ValidStrings({"system", "none"})
    public String getPrivateDnsZone() {
        return privateDnsZone;
    }

    public void setPrivateDnsZone(String privateDnsZone) {
        this.privateDnsZone = privateDnsZone;
    }

    /**
     * A list of authorized Ips.
     */
    public List<String> getAuthorizedIpRanges() {
        if (authorizedIpRanges == null) {
            authorizedIpRanges = new ArrayList<>();
        }

        return authorizedIpRanges;
    }

    public void setAuthorizedIpRanges(List<String> authorizedIpRanges) {
        this.authorizedIpRanges = authorizedIpRanges;
    }

    @Override
    public void copyFrom(ManagedClusterApiServerAccessProfile model) {
        setEnablePrivateCluster(model.enablePrivateCluster());
        setDisableRunCommand(model.disableRunCommand());
        setEnablePrivateClusterPublicFqdn(model.enablePrivateClusterPublicFqdn());
        setAuthorizedIpRanges(model.authorizedIpRanges());
        setPrivateDnsZone(model.privateDnsZone());
    }

    @Override
    public String primaryKey() {
        return "";
    }

    protected ManagedClusterApiServerAccessProfile toManagedClusterApiServerAccessProfile() {
        return new ManagedClusterApiServerAccessProfile()
            .withEnablePrivateCluster(getEnablePrivateCluster())
            .withAuthorizedIpRanges(getAuthorizedIpRanges())
            .withDisableRunCommand(getDisableRunCommand())
            .withEnablePrivateClusterPublicFqdn(getEnablePrivateClusterPublicFqdn())
            .withPrivateDnsZone(getPrivateDnsZone());
    }

    static protected ManagedClusterApiServerAccessProfile defaultPublic() {
        return new ManagedClusterApiServerAccessProfile()
            .withEnablePrivateCluster(false);
    }

    static protected ManagedClusterApiServerAccessProfile defaultPrivate() {
        return new ManagedClusterApiServerAccessProfile()
            .withEnablePrivateCluster(true)
            .withEnablePrivateClusterPublicFqdn(true)
            .withPrivateDnsZone("system");
    }
}
