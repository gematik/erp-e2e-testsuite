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
import de.gematik.test.erezept.fhir.r4.eu.EuAccessPermission;
import de.gematik.test.erezept.fhir.values.EuAccessCode;
import de.gematik.test.erezept.fhir.valuesets.IsoCountryCode;
import lombok.val;

public class EuAccessPermissionRequestBuilder
    extends ResourceBuilder<EuAccessPermission, EuAccessPermissionRequestBuilder> {

  private EuVersion version = EuVersion.getDefaultVersion();
  private IsoCountryCode countryCode;
  private final EuAccessCode euAccessCode;

  private EuAccessPermissionRequestBuilder(EuAccessCode euAccessCode) {
    this.euAccessCode = euAccessCode;
  }

  public static EuAccessPermissionRequestBuilder euAccessCode(EuAccessCode euAccessCode) {
    if (!euAccessCode.isValid()) {
      throw new IllegalArgumentException("Invalid euAccessCode");
    }
    return new EuAccessPermissionRequestBuilder(euAccessCode);
  }

  public static EuAccessPermissionRequestBuilder withRandomAccessCode() {
    return new EuAccessPermissionRequestBuilder(EuAccessCode.random());
  }

  public static EuAccessPermissionRequestBuilder withUncheckedAccessCode(
      EuAccessCode euAccessCode) {
    return new EuAccessPermissionRequestBuilder(euAccessCode);
  }

  public EuAccessPermissionRequestBuilder countryCode(IsoCountryCode countryCode) {
    this.countryCode = countryCode;
    return this;
  }

  @Override
  public EuAccessPermission build() {
    val euAccessPermission =
        createResource(
            EuAccessPermission::new, GemErpEuStructDef.ACCESS_AUTHORIZATION_REQUEST, version);
    euAccessPermission.addParameter().setName("countryCode").setValue(this.countryCode.asCoding());
    euAccessPermission.addParameter().setName("accessCode").setValue(euAccessCode.asIdentifier());
    return euAccessPermission;
  }
}
