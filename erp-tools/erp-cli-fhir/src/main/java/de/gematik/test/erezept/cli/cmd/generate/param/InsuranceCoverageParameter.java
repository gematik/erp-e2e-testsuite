/*
 * Copyright 2023 gematik GmbH
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

package de.gematik.test.erezept.cli.cmd.generate.param;

import de.gematik.test.erezept.fhir.builder.*;
import de.gematik.test.erezept.fhir.builder.kbv.*;
import de.gematik.test.erezept.fhir.resources.kbv.*;
import de.gematik.test.erezept.fhir.values.DynamicInsuranceCoverageInfo;
import de.gematik.test.erezept.fhir.values.InsuranceCoverageInfo;
import de.gematik.test.erezept.fhir.valuesets.*;
import java.util.List;
import java.util.Optional;
import lombok.*;
import picocli.*;

public class InsuranceCoverageParameter implements BaseResourceParameter {

  @CommandLine.Option(
      names = {"--iknr"},
      paramLabel = "<IKNR>",
      type = String.class,
      description =
          "IKNR (Institutionskennzeichen) is a unique 9-digit number which identifies the health insurance organization")
  private String iknr;

  @CommandLine.Option(
      names = {"--insurance", "--insurance-name"},
      paramLabel = "<NAME>",
      type = String.class,
      description = "The name of the health insurance organization")
  private String insuranceName;

  @CommandLine.Option(
      names = {"--coverage-type"},
      paramLabel = "<TYPE>",
      type = VersicherungsArtDeBasis.class,
      description =
          "The Type of the Insurance from ${COMPLETION-CANDIDATES} for the Coverage-Section")
  private VersicherungsArtDeBasis versicherungsArt;

  @CommandLine.Option(
      names = {"--group", "--pg"},
      paramLabel = "<GROUP>",
      type = PersonGroup.class,
      description =
          "Defines to which person group the insured person belongs to. Can be one of ${COMPLETION-CANDIDATES}")
  private PersonGroup personGroup;

  @CommandLine.Option(
      names = {"--dmp"},
      paramLabel = "<DMP>",
      type = DmpKennzeichen.class,
      description =
          "Defines to which DMP the insured person is subscribed to. Can be one of ${COMPLETION-CANDIDATES}")
  private DmpKennzeichen dmp;

  @CommandLine.Option(
      names = {"--wop"},
      paramLabel = "<WOP>",
      type = Wop.class,
      description = "WOP can be one of ${COMPLETION-CANDIDATES}")
  private Wop wop;

  @CommandLine.Option(
      names = {"--status"},
      paramLabel = "<STATUS>",
      type = VersichertenStatus.class,
      description = "The status of the insured person. Can be one of ${COMPLETION-CANDIDATES}")
  private VersichertenStatus status;

  @Setter private KbvPatient patient;

  private Optional<InsuranceCoverageInfo> getInsuranceInfoFor() {
    Optional<InsuranceCoverageInfo> ret = Optional.empty();

    val typeOption = Optional.ofNullable(versicherungsArt).flatMap(VersicherungsArtDeBasis::getCoverageOptions);

    if (iknr != null && insuranceName == null && typeOption.isEmpty()) {
      // when only IKNR given, try to guess the name from all known
      ret = InsuranceCoverageInfo.getByIknr(iknr);
    } else if (iknr != null && typeOption.isPresent()) {
      // when IKNR and insurance type is known
      ret = InsuranceCoverageInfo.getByIknr(typeOption.get(), iknr);
    } else if (iknr != null) {
      // when IKNR and insuranceName are given simply use this information
      ret =
          Optional.of(
              DynamicInsuranceCoverageInfo.named(getInsuranceName())
                  .ofType(getInsuranceType())
                  .withIknr(iknr));
    } else if (typeOption.isPresent()) {
      // when only the type is given choose a random one
      var rndCov = GemFaker.randomElement(typeOption.get().getEnumConstants());
      if (insuranceName != null) {
        // overwrite the name if one was given
        rndCov = DynamicInsuranceCoverageInfo.named(insuranceName).ofType(versicherungsArt).withIknr(rndCov.getIknr());
      }
      ret = Optional.of(rndCov);
    }
      
      return ret;
  }

  public String getInsuranceName() {
    return getOrDefault(insuranceName, GemFaker::insuranceName);
  }

  public PersonGroup getPersonGroup() {
    return getOrDefault(personGroup, () -> GemFaker.fakerValueSet(PersonGroup.class));
  }

  public VersicherungsArtDeBasis getInsuranceType() {
    return getOrDefault(
        versicherungsArt,
        () ->
            GemFaker.fakerValueSet(
                VersicherungsArtDeBasis.class,
                List.of(
                    VersicherungsArtDeBasis.PPV,
                    VersicherungsArtDeBasis.SEL,
                    VersicherungsArtDeBasis.SOZ)));
  }

  public DmpKennzeichen getDmp() {
    return getOrDefault(dmp, () -> GemFaker.fakerValueSet(DmpKennzeichen.class));
  }

  public Wop getWop() {
    return getOrDefault(wop, () -> GemFaker.fakerValueSet(Wop.class));
  }

  public VersichertenStatus getStatus() {
    return getOrDefault(status, () -> GemFaker.fakerValueSet(VersichertenStatus.class));
  }

  private KbvPatient getPatient() {
    return getOrDefault(patient, () -> PatientBuilder.faker(this.getInsuranceType()).build());
  }

  public KbvCoverage createCoverage() {
    val thePatient = getPatient();
    val givenInsuranceType =
        versicherungsArt != null ? versicherungsArt : thePatient.getInsuranceKind();
    val determinedInsuranceType =
        getInsuranceInfoFor().orElse(InsuranceCoverageInfo.randomFor(givenInsuranceType));
    return KbvCoverageBuilder.insurance(determinedInsuranceType)
        .personGroup(getPersonGroup())
        .dmpKennzeichen(getDmp())
        .wop(getWop())
        .versichertenStatus(getStatus())
        .beneficiary(thePatient.getReference())
        .build();
  }
}
