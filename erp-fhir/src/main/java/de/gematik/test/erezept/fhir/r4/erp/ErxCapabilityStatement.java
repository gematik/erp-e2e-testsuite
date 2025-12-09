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

package de.gematik.test.erezept.fhir.r4.erp;

import ca.uhn.fhir.model.api.annotation.ResourceDef;
import de.gematik.bbriccs.fhir.coding.exceptions.MissingFieldException;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.CapabilityStatement;
import org.hl7.fhir.r4.model.PrimitiveType;

@Slf4j
@ResourceDef(name = "CapabilityStatement")
@SuppressWarnings({"java:S110"})
public class ErxCapabilityStatement extends CapabilityStatement {

  public String getSoftwareVersion() {
    return Optional.ofNullable(this.getSoftware().getVersion())
        .orElseThrow(() -> new MissingFieldException(this.getClass(), "Version"));
  }

  public String getSoftwareName() {
    return Optional.ofNullable(this.getSoftware().getName())
        .orElseThrow(() -> new MissingFieldException(this.getClass(), "Software Name"));
  }

  public String getSoftwareReleaseDate() {
    return Optional.ofNullable(this.getSoftware().getReleaseDateElement())
        .map(PrimitiveType::asStringValue)
        .orElseThrow(() -> new MissingFieldException(this.getClass(), "Software ReleaseDate"));
  }

  public String getImplementationDescription() {
    return Optional.ofNullable(this.getImplementation().getDescription())
        .orElseThrow(
            () -> new MissingFieldException(this.getClass(), "Implementation Description"));
  }
}
