/*
 * Copyright 2025 gematik GmbH
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
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 */

package de.gematik.test.erezept.fhir.builder.eu;

import de.gematik.bbriccs.fhir.builder.ResourceBuilder;
import de.gematik.test.erezept.fhir.profiles.definitions.GemErpEuStructDef;
import de.gematik.test.erezept.fhir.profiles.version.EuVersion;
import lombok.val;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.Parameters;

public class EuPatchTaskInputBuilder extends ResourceBuilder<Parameters, EuPatchTaskInputBuilder> {

  private EuVersion version = EuVersion.getDefaultVersion();
  private boolean isRedeemable = true;

  public static EuPatchTaskInputBuilder builder() {
    return new EuPatchTaskInputBuilder();
  }

  public EuPatchTaskInputBuilder withVersion(EuVersion version) {
    this.version = version;
    return this;
  }

  public EuPatchTaskInputBuilder withIsRedeemable(boolean isRedeemable) {
    this.isRedeemable = isRedeemable;
    return this;
  }

  @Override
  public Parameters build() {
    val parametersResource =
        this.createResource(Parameters::new, GemErpEuStructDef.PATCH_TASK_INPUT, version);

    parametersResource
        .addParameter()
        .setName("eu-isRedeemableByPatientAuthorization")
        .setValue(new BooleanType(isRedeemable));

    return parametersResource;
  }
}
