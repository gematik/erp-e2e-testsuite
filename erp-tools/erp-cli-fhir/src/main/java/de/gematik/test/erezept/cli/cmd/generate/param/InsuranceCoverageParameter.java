/*
 * Copyright (c) 2023 gematik GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.gematik.test.erezept.cli.cmd.generate.param;

import de.gematik.test.erezept.fhir.builder.*;
import de.gematik.test.erezept.fhir.builder.kbv.*;
import de.gematik.test.erezept.fhir.references.kbv.*;
import de.gematik.test.erezept.fhir.resources.kbv.*;
import de.gematik.test.erezept.fhir.valuesets.*;
import java.util.*;
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
      names = {"--insurance"},
      paramLabel = "<NAME>",
      type = String.class,
      description = "The name of the health insurance organization")
  private String insuranceName;

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

  public String getIknr() {
    return getOrDefault(iknr, GemFaker::fakerIknr);
  }

  public String getInsuranceName() {
    return getOrDefault(insuranceName, GemFaker::insuranceName);
  }

  public PersonGroup getPersonGroup() {
    return getOrDefault(personGroup, () -> GemFaker.fakerValueSet(PersonGroup.class));
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

  public KbvCoverage createCoverage() {
    val builder =
        KbvCoverageBuilder.insurance(getIknr(), getInsuranceName())
            .personGroup(getPersonGroup())
            .dmpKennzeichen(getDmp())
            .wop(getWop())
            .versichertenStatus(getStatus());

    if (patient != null) {
      builder.beneficiary(patient);
    } else {
      builder.beneficiary(new SubjectReference(UUID.randomUUID().toString()));
      builder.versicherungsArt(
          GemFaker.randomElement(VersicherungsArtDeBasis.GKV, VersicherungsArtDeBasis.PKV));
    }

    return builder.build();
  }
}
