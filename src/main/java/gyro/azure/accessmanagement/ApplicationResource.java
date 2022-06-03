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

package gyro.azure.accessmanagement;

import java.util.HashSet;
import java.util.Set;

import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.authorization.models.ActiveDirectoryApplication;
import gyro.azure.AzureResource;
import gyro.azure.Copyable;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import org.apache.commons.lang3.StringUtils;

/**
 * Creates an application.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     azure::application application-example
 *         name: "application-example"
 *         account-type: "AzureADMyOrg"
 *     end
 *
 * end
 */
@Type("application")
public class ApplicationResource extends AzureResource implements Copyable<ActiveDirectoryApplication> {

    private String name;
    private String accountType;
    private Set<String> identifierUris;
    private Set<String> replyUrls;
    private String signOnUrl;

    private String applicationId;
    private String id;

    /**
     * The name of the application.
     */
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Required
    @Updatable
    @ValidStrings({
        "AzureADMyOrg",
        "AzureADMultipleOrgs",
        "AzureADandPersonalMicrosoftAccount",
        "PersonalMicrosoftAccount" })
    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    /**
     * A set of identifier uri's for the application.
     */
    @Updatable
    public Set<String> getIdentifierUris() {
        if (identifierUris == null) {
            identifierUris = new HashSet<>();
        }

        return identifierUris;
    }

    public void setIdentifierUris(Set<String> identifierUris) {
        this.identifierUris = identifierUris;
    }

    /**
     * A set of reply uri's for the application.
     */
    @Updatable
    public Set<String> getReplyUrls() {
        if (replyUrls == null) {
            replyUrls = new HashSet<>();
        }

        return replyUrls;
    }

    public void setReplyUrls(Set<String> replyUrls) {
        this.replyUrls = replyUrls;
    }

    /**
     * The sign on url for the application.
     */
    @Updatable
    public String getSignOnUrl() {
        return signOnUrl;
    }

    public void setSignOnUrl(String signOnUrl) {
        this.signOnUrl = signOnUrl;
    }

    /**
     * The application id of the application.
     */
    @Id
    @Output
    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    /**
     * The id of the application.
     */
    @Output
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public void copyFrom(ActiveDirectoryApplication model) {
        setName(model.name());
        setApplicationId(model.applicationId());
        setId(model.id());

        setAccountType(model.accountType().toString());
        setIdentifierUris(model.identifierUris());
        setReplyUrls(model.replyUrls());
        setSignOnUrl(model.signOnUrl() != null ? model.signOnUrl().toString() : null);
    }

    @Override
    public boolean refresh() {
        AzureResourceManager client = createClient();

        ActiveDirectoryApplication application = client.accessManagement()
            .activeDirectoryApplications()
            .getById(getId());

        if (application == null) {
            return false;
        }

        copyFrom(application);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        AzureResourceManager client = createClient();

        ActiveDirectoryApplication.DefinitionStages.WithCreate withCreate = client.accessManagement()
            .activeDirectoryApplications()
            .define(getName())
            .withAccountType(getAccountType());

        for (String uri : getIdentifierUris()) {
            withCreate = withCreate.withIdentifierUrl(uri);
        }

        for (String uri : getReplyUrls()) {
            withCreate = withCreate.withReplyUrl(uri);
        }

        if (!StringUtils.isBlank(getSignOnUrl())) {
            withCreate = withCreate.withSignOnUrl(getSignOnUrl());
        }

        ActiveDirectoryApplication application = withCreate.create();

        copyFrom(application);
    }

    @Override
    public void update(
        GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {
        AzureResourceManager client = createClient();

        ActiveDirectoryApplication application = client.accessManagement()
            .activeDirectoryApplications()
            .getById(getId());

        ActiveDirectoryApplication.Update update = application.update();

        ApplicationResource currentApp = (ApplicationResource) current;

        if (changedFieldNames.contains("account-type")) {
            update = update.withAccountType(getAccountType());
        }

        if (changedFieldNames.contains("identifier-uris")) {
            for (String uri : currentApp.getIdentifierUris()) {
                update = update.withoutIdentifierUrl(uri);
            }

            for (String uri : getIdentifierUris()) {
                update = update.withIdentifierUrl(uri);
            }
        }

        if (changedFieldNames.contains("reply-urls")) {
            for (String uri : currentApp.getReplyUrls()) {
                update = update.withoutReplyUrl(uri);
            }

            for (String uri : getReplyUrls()) {
                update = update.withReplyUrl(uri);
            }
        }

        if (changedFieldNames.contains("sign-on-url")) {
            update = update.withSignOnUrl(getSignOnUrl());
        }

        update.apply();
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        AzureResourceManager client = createClient();

        client.accessManagement().activeDirectoryApplications().deleteById(getId());
    }
}
