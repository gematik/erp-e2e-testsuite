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

package de.gematik.test.erezept.fhir.builder.kbv;

import de.gematik.test.erezept.fhir.builder.AbstractResourceBuilder;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.KbvItaForStructDef;
import de.gematik.test.erezept.fhir.parser.profiles.systems.DeBasisNamingSystem;
import de.gematik.test.erezept.fhir.parser.profiles.version.KbvItaForVersion;
import de.gematik.test.erezept.fhir.references.kbv.SubjectReference;
import de.gematik.test.erezept.fhir.resources.kbv.KbvCoverage;
import de.gematik.test.erezept.fhir.resources.kbv.KbvPatient;
import de.gematik.test.erezept.fhir.values.IKNR;
import de.gematik.test.erezept.fhir.values.InsuranceCoverageInfo;
import de.gematik.test.erezept.fhir.valuesets.DmpKennzeichen;
import de.gematik.test.erezept.fhir.valuesets.IValueSet;
import de.gematik.test.erezept.fhir.valuesets.PayorType;
import de.gematik.test.erezept.fhir.valuesets.PersonGroup;
import de.gematik.test.erezept.fhir.valuesets.VersichertenStatus;
import de.gematik.test.erezept.fhir.valuesets.VersicherungsArtDeBasis;
import de.gematik.test.erezept.fhir.valuesets.Wop;
import java.util.LinkedList;
import java.util.List;
import lombok.val;
import org.hl7.fhir.r4.model.Coverage;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Reference;

public class KbvCoverageBuilder extends AbstractResourceBuilder<KbvCoverageBuilder> {

  private static final List<IValueSet> REQUIRE_ALTERNATIVE_IKNR =
      List.of(VersicherungsArtDeBasis.BG, PayorType.UK);
  private KbvItaForVersion kbvItaForVersion = KbvItaForVersion.getDefaultVersion();

  private PersonGroup personGroup = PersonGroup.NOT_SET;
  private DmpKennzeichen dmpKennzeichen = DmpKennzeichen.NOT_SET;
  private Wop wop = Wop.DUMMY;
  private VersichertenStatus versichertenStatus = VersichertenStatus.MEMBERS;
  private static final Coverage.CoverageStatus coverageStatus = Coverage.CoverageStatus.ACTIVE;
  private IValueSet versicherungsArt;

  private Reference beneficiary;
  private IKNR iknr;
  private String insuranceName;

  public static KbvCoverageBuilder insurance(InsuranceCoverageInfo coverage) {
    val builder = insurance(coverage.getIknr(), coverage.getName());
    return builder.versicherungsArt(coverage.getInsuranceType());
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
    if (this.versicherungsArt == null) versicherungsArt(patient.getInsuranceKind());
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
