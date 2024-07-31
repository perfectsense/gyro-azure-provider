/*
 * Copyright 2019, Perfect Sense, Inc.
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

package gyro.azure.cdn;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.cdn.models.CdnProfile;
import gyro.azure.AzureFinder;
import gyro.core.Type;

/**
 * Query cdn profile.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    cdn-profile: $(external-query azure::cdn-profile {})
 */
@Type("cdn-profile")
public class CdnProfileFinder extends AzureFinder<AzureResourceManager, CdnProfile, CdnProfileResource> {

    private String id;

    /**
     * The ID of the CDN Profile.
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    protected List<CdnProfile> findAllAzure(AzureResourceManager client) {
        return client.cdnProfiles().list().stream().collect(Collectors.toList());
    }

    @Override
    protected List<CdnProfile> findAzure(AzureResourceManager client, Map<String, String> filters) {
        CdnProfile cdnProfile = client.cdnProfiles().getById(filters.get("id"));
        if (cdnProfile == null) {
            return Collections.emptyList();
        } else {
            return Collections.singletonList(cdnProfile);
        }
    }
}
