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

package de.gematik.test.erezept.fhir.r4.kbv;

import static java.text.MessageFormat.format;

import ca.uhn.fhir.model.api.annotation.ResourceDef;
import de.gematik.bbriccs.fhir.coding.exceptions.MissingFieldException;
import de.gematik.bbriccs.fhir.de.DeBasisProfilCodeSystem;
import de.gematik.bbriccs.fhir.de.value.PZN;
import de.gematik.test.erezept.fhir.extensions.kbv.AccidentExtension;
import de.gematik.test.erezept.fhir.profiles.definitions.KbvItaForStructDef;
import de.gematik.test.erezept.fhir.profiles.definitions.KbvItvEvdgaStructDef;
import de.gematik.test.erezept.fhir.r4.ErpFhirResource;
import de.gematik.test.erezept.fhir.valuesets.AccidentCauseType;
import java.util.Date;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.r4.model.DeviceRequest;
import org.hl7.fhir.r4.model.Resource;

@Slf4j
@ResourceDef(name = "DeviceRequest")
@SuppressWarnings({"java:S110"})
public class KbvHealthAppRequest extends DeviceRequest implements ErpFhirResource {

  public PZN getPzn() {
    return this.getCodeCodeableConcept().getCoding().stream()
        .filter(DeBasisProfilCodeSystem.PZN::matches)
        .map(coding -> PZN.from(coding.getCode()))
        .findFirst()
        .orElseThrow(() -> new MissingFieldException(this.getClass(), DeBasisProfilCodeSystem.PZN));
  }

  public String getName() {
    return this.getCodeCodeableConcept().getText();
  }

  /**
   * Kennzeichnung, ob diese Verordnung mit Bezug zum Sozialen Entschädigungsrecht nach SGB XIV
   * (SER) erfolgt
   *
   * @return true if SER extension is set to true and false otherwise
   */
  public boolean relatesToSocialCompensationLaw() {
    return this.extension.stream()
        .filter(KbvItvEvdgaStructDef.SER_EXTENSION::matches)
        .map(ext -> ext.getValue().castToBoolean(ext.getValue()).booleanValue())
        .findFirst()
        .orElse(false);
  }

  public boolean hasAccidentExtension() {
    return this.getAccident().isPresent();
  }

  public Optional<AccidentCauseType> getAccidentCause() {
    return this.getAccident().map(AccidentExtension::accidentCauseType);
  }

  public Optional<String> getAccidentWorkplace() {
    return this.getAccident().map(AccidentExtension::workplace);
  }

  public Optional<Date> getAccidentDate() {
    return this.getAccident().map(AccidentExtension::accidentDay);
  }

  public Optional<AccidentExtension> getAccident() {
    return this.getExtension().stream()
        .filter(KbvItaForStructDef.ACCIDENT::matches)
        .findFirst()
        .map(AccidentExtension::fromExtension);
  }

  @Override
  public String getDescription() {
    return format("Health App {0} with PZN {1}", this.getName(), this.getPzn().getValue());
  }

  public static KbvHealthAppRequest fromDeviceRequest(DeviceRequest adaptee) {
    if (adaptee instanceof KbvHealthAppRequest healthAppRequest) {
      return healthAppRequest;
    } else {
      val kbvHealthAppRequest = new KbvHealthAppRequest();
      adaptee.copyValues(kbvHealthAppRequest);
      return kbvHealthAppRequest;
    }
  }

  public static KbvHealthAppRequest fromDeviceRequest(Resource adaptee) {
    return fromDeviceRequest((DeviceRequest) adaptee);
  }
}
