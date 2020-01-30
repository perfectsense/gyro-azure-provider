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

package gyro.azure;

import gyro.core.resource.Resource;
import com.microsoft.azure.management.Azure;

public abstract class AzureResource extends Resource {

    public static Azure createClient(AzureCredentials credentials) {
        return credentials.createClient();
    }

    protected Azure createClient() {
        return AzureResource.createClient(credentials(AzureCredentials.class));
    }

    protected String getRegion() {
        return credentials(AzureCredentials.class).getRegion();
    }

}
