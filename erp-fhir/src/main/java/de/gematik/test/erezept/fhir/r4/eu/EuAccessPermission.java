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

package de.gematik.test.erezept.fhir.r4.eu;

import ca.uhn.fhir.model.api.annotation.ResourceDef;
import de.gematik.bbriccs.fhir.coding.exceptions.MissingFieldException;
import de.gematik.test.erezept.fhir.values.EuAccessCode;
import de.gematik.test.erezept.fhir.valuesets.IsoCountryCode;
import java.time.Instant;
import java.util.Optional;
import org.hl7.fhir.r4.model.Parameters;

@ResourceDef(name = "Parameters")
@SuppressWarnings({"java:S110"})
public class EuAccessPermission extends Parameters {

  private static final String ACCESS_CODE = "accessCode";

  public IsoCountryCode getIsoCountryCode() {
    return this.getParameter().stream()
        .filter(p -> p.getName().equals("countryCode"))
        .map(
            para ->
                IsoCountryCode.fromCode(para.getValue().castToCoding(para.getValue()).getCode()))
        .findFirst()
        .orElseThrow(() -> new MissingFieldException(this.getClass(), "countryCode"));
  }

  public EuAccessCode getAccessCode() {
    return this.getParameter().stream()
        .filter(p -> p.getName().equals(ACCESS_CODE))
        .map(parameter -> parameter.getValue().castToIdentifier(parameter.getValue()).getValue())
        .map(EuAccessCode::from)
        .findFirst()
        .orElseThrow(() -> new MissingFieldException(this.getClass(), ACCESS_CODE));
  }

  public Optional<Instant> getValidUntil() {
    return this.getParameter().stream()
        .filter(p -> p.getName().equals("validUntil"))
        .map(ext -> ext.getValue().castToInstant(ext.getValue()).getValue().toInstant())
        .findFirst();
  }

  public Optional<Instant> getCreateAt() {
    return this.getParameter().stream()
        .filter(p -> p.getName().equals("createdAt"))
        .map(ext -> ext.getValue().castToInstant(ext.getValue()).getValue().toInstant())
        .findFirst();
  }
}
