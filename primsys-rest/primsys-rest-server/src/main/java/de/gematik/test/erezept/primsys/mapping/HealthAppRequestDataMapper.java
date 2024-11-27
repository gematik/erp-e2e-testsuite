/*
 * Copyright 2024 gematik GmbH
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

package de.gematik.test.erezept.primsys.mapping;

import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.builder.kbv.KbvHealthAppRequestBuilder;
import de.gematik.test.erezept.fhir.resources.kbv.KbvCoverage;
import de.gematik.test.erezept.fhir.resources.kbv.KbvHealthAppRequest;
import de.gematik.test.erezept.fhir.resources.kbv.KbvPatient;
import de.gematik.test.erezept.fhir.resources.kbv.KbvPractitioner;
import de.gematik.test.erezept.fhir.values.PZN;
import de.gematik.test.erezept.primsys.data.HealthAppRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.hl7.fhir.r4.model.DeviceRequest.DeviceRequestStatus;
import org.hl7.fhir.r4.model.DeviceRequest.RequestIntent;

public class HealthAppRequestDataMapper
    extends DataMapper<HealthAppRequestDto, KbvHealthAppRequest> {

  private final KbvPatient patient;
  private final KbvCoverage insurance;
  private final KbvPractitioner practitioner;

  public HealthAppRequestDataMapper(
      HealthAppRequestDto dto,
      KbvPatient patient,
      KbvCoverage insurance,
      KbvPractitioner practitioner) {
    super(dto);
    this.patient = patient;
    this.insurance = insurance;
    this.practitioner = practitioner;
  }

  public static Builder from(KbvHealthAppRequest healthAppRequest) {
    val builder =
        HealthAppRequestDto.builder()
            .pzn(healthAppRequest.getPzn().getValue())
            .name(healthAppRequest.getName())
            .ser(healthAppRequest.relatesToSocialCompensationLaw());
    return from(builder.build());
  }

  public static Builder from(HealthAppRequestDto dto) {
    return new Builder(dto);
  }

  @Override
  protected void complete() {
    ensure(dto::getPzn, dto::setPzn, () -> PZN.random().getValue());
    ensure(dto::getName, dto::setName, () -> GemFaker.getFaker().app().name());
  }

  @Override
  protected KbvHealthAppRequest convertInternal() {
    val builder =
        KbvHealthAppRequestBuilder.forPatient(patient)
            .insurance(insurance)
            .requester(practitioner)
            .healthApp(dto.getPzn(), dto.getName())
            .relatesToSocialCompensationLaw(dto.isSer())
            .status(DeviceRequestStatus.ACTIVE)
            .intent(RequestIntent.ORDER);
    return builder.build();
  }

  @RequiredArgsConstructor
  public static class Builder {
    private final HealthAppRequestDto dto;
    private KbvPatient patient;
    private KbvCoverage insurance;
    private KbvPractitioner practitioner;

    public Builder requestedFor(KbvPatient patient) {
      this.patient = patient;
      return this;
    }

    public Builder requestedBy(KbvPractitioner practitioner) {
      this.practitioner = practitioner;
      return this;
    }

    public Builder coveredBy(KbvCoverage insurance) {
      this.insurance = insurance;
      return this;
    }

    public HealthAppRequestDataMapper build() {
      return new HealthAppRequestDataMapper(dto, patient, insurance, practitioner);
    }
  }
}
