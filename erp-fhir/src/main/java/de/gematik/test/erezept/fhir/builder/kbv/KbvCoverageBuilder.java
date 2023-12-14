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

package de.gematik.test.erezept.fhir.builder.kbv;

import static de.gematik.test.erezept.fhir.builder.GemFaker.*;

import de.gematik.test.erezept.fhir.builder.*;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.*;
import de.gematik.test.erezept.fhir.parser.profiles.systems.*;
import de.gematik.test.erezept.fhir.parser.profiles.version.*;
import de.gematik.test.erezept.fhir.references.kbv.*;
import de.gematik.test.erezept.fhir.resources.kbv.*;
import de.gematik.test.erezept.fhir.values.*;
import de.gematik.test.erezept.fhir.valuesets.*;
import java.util.*;
import lombok.*;
import org.hl7.fhir.r4.model.*;

public class KbvCoverageBuilder extends AbstractResourceBuilder<KbvCoverageBuilder> {

  private static final List<IValueSet> REQUIRE_ALTERNATIVE_IKNR =
      List.of(VersicherungsArtDeBasis.BG, PayorType.UK);
  private KbvItaForVersion kbvItaForVersion = KbvItaForVersion.getDefaultVersion();

  private PersonGroup personGroup = PersonGroup.NOT_SET;
  private DmpKennzeichen dmpKennzeichen = DmpKennzeichen.NOT_SET;
  private Wop wop = Wop.DUMMY;
  private VersichertenStatus versichertenStatus = VersichertenStatus.MEMBERS;
  private static final Coverage.CoverageStatus coverageStatus = Coverage.CoverageStatus.ACTIVE;
  private IValueSet versicherungsArt = VersicherungsArtDeBasis.GKV;

  private Reference beneficiary;
  private IKNR iknr;
  private String insuranceName;

  public static KbvCoverageBuilder insurance(InsuranceCoverageInfo coverage) {
    val builder = insurance(coverage.getIknr(), coverage.getName());
    builder.versicherungsArt = coverage.getInsuranceType();
    return builder;
  }
  
  public static KbvCoverageBuilder insurance(String iknr, String insuranceName) {
    return insurance(new IKNR(iknr), insuranceName);
  }

  public static KbvCoverageBuilder insurance(IKNR iknr, String insuranceName) {
    val insurance = new KbvCoverageBuilder();
    insurance.iknr = iknr;
    insurance.insuranceName = insuranceName;
    return insurance;
  }

  public static KbvCoverageBuilder faker() {
    return faker(randomElement(VersicherungsArtDeBasis.GKV, VersicherungsArtDeBasis.PKV));
  }

  public static KbvCoverageBuilder faker(VersicherungsArtDeBasis insuranceType) {
    val builder = switch (insuranceType) {
      case GKV -> insurance(randomElement(GkvInsuranceCoverageInfo.values()));
      case PKV -> insurance(randomElement(PkvInsuranceCoverageInfo.values()));
      case BG -> insurance(randomElement(BGInsuranceCoverageInfo.values()));
      default -> insurance(DynamicInsuranceCoverageInfo.random());
    };
    
    builder
        .personGroup(fakerValueSet(PersonGroup.class))
        .beneficiary(PatientBuilder.faker(insuranceType).build())
        .dmpKennzeichen(fakerValueSet(DmpKennzeichen.class))
        .wop(fakerValueSet(Wop.class))
        .versichertenStatus(fakerValueSet(VersichertenStatus.class));
    return builder;
  }

  /**
   * <b>Attention:</b> use with care as this setter might break automatic choice of the version.
   * This builder will set the default version automatically, so there should be no need to provide
   * an explicit version
   *
   * @param version to use for generation of this resource
   * @return Builder
   */
  public KbvCoverageBuilder version(KbvItaForVersion version) {
    this.kbvItaForVersion = version;
    return this;
  }

  public KbvCoverageBuilder personGroup(PersonGroup personGroup) {
    this.personGroup = personGroup;
    return self();
  }

  public KbvCoverageBuilder dmpKennzeichen(DmpKennzeichen kennzeichen) {
    this.dmpKennzeichen = kennzeichen;
    return self();
  }

  public KbvCoverageBuilder wop(Wop wop) {
    this.wop = wop;
    return self();
  }

  public KbvCoverageBuilder versichertenStatus(VersichertenStatus status) {
    this.versichertenStatus = status;
    return self();
  }

  /**
   * Using this setter shouldn't be necessary because the VersicherungsArtDeBasis will be taken from
   * patient directly to ensure consistency. However, you can still use this setter to mess up with
   * the consistency by applying a different VersicherungsArtDeBasis after using
   * beneficiary(KbvPatient).
   *
   * @param versicherungsArt is the kind of Insurance of this coverage
   * @return self
   */
  public KbvCoverageBuilder versicherungsArt(VersicherungsArtDeBasis versicherungsArt) {
    this.versicherungsArt = versicherungsArt;
    return self();
  }

  public KbvCoverageBuilder versicherungsArt(PayorType payorType) {
    this.versicherungsArt = payorType;
    return self();
  }

  public KbvCoverageBuilder beneficiary(KbvPatient patient) {
    this.versicherungsArt = patient.getInsuranceKind();
    return beneficiary(new SubjectReference(patient.getId()));
  }

  public KbvCoverageBuilder beneficiary(SubjectReference subject) {
    this.beneficiary = subject.asReference();
    return self();
  }

  public KbvCoverage build() {
    val coverage = new KbvCoverage();

    val profile = KbvItaForStructDef.COVERAGE.asCanonicalType(kbvItaForVersion);
    val meta = new Meta().setProfile(List.of(profile));

    // set FHIR-specific values provided by HAPI
    coverage.setId(this.getResourceId()).setMeta(meta);

    val extensions = new LinkedList<Extension>();
    extensions.add(personGroup.asExtension());
    extensions.add(dmpKennzeichen.asExtension());
    extensions.add(wop.asExtension());
    extensions.add(versichertenStatus.asExtension());

    // set the payor
    val insuranceRef = new Reference().setDisplay(insuranceName);
    Identifier mainIknrIdentifier;

    if (kbvItaForVersion.compareTo(KbvItaForVersion.V1_1_0) < 0) {
      mainIknrIdentifier = iknr.asIdentifier();
    } else {
      mainIknrIdentifier = iknr.asIdentifier(DeBasisNamingSystem.IKNR_SID);
    }
    insuranceRef.setIdentifier(mainIknrIdentifier);

    if (requiresAlternativeIknr()) {
      val altIknr = IKNR.from("121191241"); // just a dummy for now!
      Identifier alternativeIkIdentifier;
      if (kbvItaForVersion.compareTo(KbvItaForVersion.V1_1_0) < 0) {
        alternativeIkIdentifier = altIknr.asIdentifier();
      } else {
        alternativeIkIdentifier = altIknr.asIdentifier(DeBasisNamingSystem.IKNR_SID);
      }
      val altIkExtension = new Extension(KbvItaForStructDef.ALTERNATIVE_IK.getCanonicalUrl());
      altIkExtension.setValue(alternativeIkIdentifier);
      mainIknrIdentifier.setExtension(List.of(altIkExtension));
    }

    coverage
        .setStatus(coverageStatus)
        .setType(versicherungsArt.asCodeableConcept())
        .setBeneficiary(beneficiary)
        .setExtension(extensions);
    coverage.setPayor(List.of(insuranceRef));

    return coverage;
  }

  private boolean requiresAlternativeIknr() {
    return REQUIRE_ALTERNATIVE_IKNR.contains(versicherungsArt);
  }
}
