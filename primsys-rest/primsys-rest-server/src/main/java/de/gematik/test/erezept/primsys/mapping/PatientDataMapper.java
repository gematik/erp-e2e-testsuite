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

import static de.gematik.test.erezept.fhir.builder.GemFaker.*;
import static de.gematik.test.erezept.fhir.builder.GemFaker.fakerStreetName;

import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.builder.kbv.AssignerOrganizationFaker;
import de.gematik.test.erezept.fhir.builder.kbv.PatientBuilder;
import de.gematik.test.erezept.fhir.resources.kbv.KbvPatient;
import de.gematik.test.erezept.fhir.values.KVNR;
import de.gematik.test.erezept.fhir.valuesets.Country;
import de.gematik.test.erezept.fhir.valuesets.VersicherungsArtDeBasis;
import de.gematik.test.erezept.primsys.data.PatientDto;
import de.gematik.test.erezept.primsys.data.valuesets.InsuranceTypeDto;
import java.util.stream.Collectors;
import lombok.val;
import org.hl7.fhir.r4.model.PrimitiveType;

public class PatientDataMapper extends DataMapper<PatientDto, KbvPatient> {

  public PatientDataMapper(PatientDto dto) {
    super(dto);
  }

  @Override
  protected void complete() {
    // Note: don't fake KVNR because this value is required to be given by the user!
    ensure(dto::getInsuranceType, dto::setInsuranceType, () -> InsuranceTypeDto.GKV);
    ensure(dto::getFirstName, dto::setFirstName, GemFaker::fakerFirstName);
    ensure(dto::getLastName, dto::setLastName, GemFaker::fakerLastName);
    ensure(dto::getBirthDate, dto::setBirthDate, GemFaker::fakerBirthday);
    ensure(dto::getCity, dto::setCity, GemFaker::fakerCity);
    ensure(dto::getPostal, dto::setPostal, GemFaker::fakerZipCode);
    ensure(dto::getStreet, dto::setStreet, GemFaker::fakerStreetName);
  }

  @Override
  protected KbvPatient convertInternal() {
    return PatientBuilder.builder()
        .kvnr(getKvnr(), getInsuranceKind())
        .name(dto.getFirstName(), dto.getLastName())
        .birthDate(dto.getBirthDate())
        .address(Country.D, dto.getCity(), dto.getPostal(), dto.getStreet())
        .assigner(
            AssignerOrganizationFaker.builder()
                .fake()) // will only be used for GKV with old profiles
        .build();
  }

  public boolean hasKvnr() {
    return !this.isNullOrEmpty(dto.getKvnr());
  }

  private KVNR getKvnr() {
    return KVNR.from(dto.getKvnr());
  }

  private VersicherungsArtDeBasis getInsuranceKind() {
    return VersicherungsArtDeBasis.fromCode(dto.getInsuranceType().name());
  }

  public static PatientDataMapper from(KbvPatient patient) {
    val dto = new PatientDto();
    dto.setKvnr(patient.getKvnr().getValue());

    val humanName = patient.getNameFirstRep();
    dto.setFirstName(humanName.getGivenAsSingleString());
    dto.setLastName(humanName.getFamily());
    dto.setBirthDate(patient.getBirthDate());

    val address = patient.getAddressFirstRep();
    dto.setCity(address.getCity());
    dto.setPostal(address.getPostalCode());
    dto.setStreet(
        address.getLine().stream().map(PrimitiveType::getValue).collect(Collectors.joining(" ")));

    return from(dto);
  }

  public static PatientDataMapper from(PatientDto dto) {
    return new PatientDataMapper(dto);
  }

  public static PatientDto randomDto() {
    return PatientDto.withKvnr(KVNR.random().getValue())
        .named(fakerFirstName(), fakerLastName())
        .bornOn(fakerBirthday())
        .city(fakerCity())
        .postal(fakerZipCode())
        .street(fakerStreetName())
        .build();
  }
}
