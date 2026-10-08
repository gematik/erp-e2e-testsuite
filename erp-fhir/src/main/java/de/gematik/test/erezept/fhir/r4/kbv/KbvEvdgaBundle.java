/*
 * Copyright (Change Date see Readme), gematik GmbH
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
import de.gematik.test.erezept.fhir.profiles.definitions.KbvItvEvdgaStructDef;
import de.gematik.test.erezept.fhir.util.FhirEntryReplacer;
import java.util.Date;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.r4.model.ResourceType;

@Slf4j
@Getter
@ResourceDef(name = "Bundle")
@SuppressWarnings({"java:S110"})
public class KbvEvdgaBundle extends KbvBaseBundle {

  public KbvHealthAppRequest getHealthAppRequest() {
    return this.entry.stream()
        .filter(entry -> entry.getResource().getResourceType().equals(ResourceType.DeviceRequest))
        .map(
            entry ->
                FhirEntryReplacer.cast(
                    KbvHealthAppRequest.class, entry, KbvHealthAppRequest::fromDeviceRequest))
        .findFirst()
        .orElseThrow(
            () ->
                new MissingFieldException(
                    this.getClass(), KbvItvEvdgaStructDef.HEALTH_APP_REQUEST));
  }

  @Override
  public String getDescription() {
    val workflow = this.getFlowType();
    return format(
        "{0} (Workflow {1}) {2} für {3}",
        workflow.getDisplay(), workflow.getCode(), type, this.getPatient().getDescription());
  }

  @Override
  public void setAuthoredOnDate(Date authoredOn) {
    this.getHealthAppRequest().setAuthoredOn(authoredOn);
  }

  @Override
  public Date getAuthoredOn() {
    return this.getHealthAppRequest().getAuthoredOn();
  }
}
