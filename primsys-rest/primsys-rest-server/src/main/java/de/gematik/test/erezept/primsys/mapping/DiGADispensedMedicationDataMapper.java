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

package de.gematik.test.erezept.primsys.mapping;

import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.builder.erp.ErxMedicationDispenseDiGAFaker;
import de.gematik.test.erezept.fhir.r4.erp.ErxMedicationDispenseDiGA;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.primsys.data.DiGADispensedMedicationDto;
import lombok.val;

public class DiGADispensedMedicationDataMapper
    extends DataMapper<DiGADispensedMedicationDto, ErxMedicationDispenseDiGA> {

  private final KVNR forKvnr;
  private final PrescriptionId forPrescription;
  private final String performerTelematikId;

  public DiGADispensedMedicationDataMapper(
      DiGADispensedMedicationDto dto,
      KVNR forKvnr,
      PrescriptionId forPrescription,
      String performerTelematikId) {
    super(dto);
    this.forKvnr = forKvnr;
    this.forPrescription = forPrescription;
    this.performerTelematikId = performerTelematikId;
  }

  public static DiGADispensedMedicationDataMapper from(
      DiGADispensedMedicationDto dto,
      KVNR forKvnr,
      PrescriptionId forPrescription,
      String performerTelematikId) {
    return new DiGADispensedMedicationDataMapper(
        dto, forKvnr, forPrescription, performerTelematikId);
  }

  public static DiGADispensedMedicationDataMapper random() {
    val dto = randomDto();
    return from(dto, KVNR.random(), PrescriptionId.random(), GemFaker.fakerTelematikId());
  }

  public static DiGADispensedMedicationDto randomDto() {
    return new DiGADispensedMedicationDto();
  }

  @Override
  protected void complete() {
    if (!isNullOrEmpty(dto.getRedeemCode())) {
      // ensure the DiGA has a name if the redeem code is set
      ensure(dto::getName, dto::setName, () -> GemFaker.getFaker().app().name());
    }
  }

  @Override
  protected ErxMedicationDispenseDiGA convertInternal() {
    val faker = ErxMedicationDispenseDiGAFaker.builder();

    faker.withKvnr(forKvnr).withPerformer(performerTelematikId).withPrescriptionId(forPrescription);

    setIfPresent(dto::getPzn, dtoPzn -> faker.withPzn(dtoPzn, dto.getName()));
    setIfPresent(dto::getDeepLink, faker::withDeepLink);
    setIfPresent(dto::getRedeemCode, faker::withRedeemCode);
    setIfPresent(dto::getWhenHandedOver, faker::withHandedOverDate);

    return faker.fake();
  }
}
