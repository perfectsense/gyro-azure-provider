/*
 * Copyright 2024, Brightspot, Inc.
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

package gyro.azure.communication;

import com.azure.resourcemanager.communication.models.VerificationStatusRecord;
import gyro.azure.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Output;
import gyro.core.validation.ValidStrings;

public class DomainVerificationStatusRecord extends Diffable implements Copyable<VerificationStatusRecord> {

    private String status;
    private String errorCode;

    @ValidStrings({
        "NotStarted",
        "VerificationRequested",
        "VerificationInProgress",
        "VerificationFailed",
        "Verified",
        "CancellationRequested"
    })
    @Output
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Output
    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    @Override
    public void copyFrom(VerificationStatusRecord model) {
        setStatus(model.status().toString());
        setErrorCode(model.errorCode());
    }

    @Override
    public String primaryKey() {
        return "";
    }
}
