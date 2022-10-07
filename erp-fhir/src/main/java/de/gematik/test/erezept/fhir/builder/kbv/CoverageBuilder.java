/*
 * Copyright (c) 2022 gematik GmbH
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

package de.gematik.test.erezept.fhir.builder.kbv;

import static de.gematik.test.erezept.fhir.builder.GemFaker.*;

import de.gematik.test.erezept.fhir.builder.AbstractResourceBuilder;
import de.gematik.test.erezept.fhir.parser.profiles.ErpStructureDefinition;
import de.gematik.test.erezept.fhir.references.kbv.SubjectReference;
import de.gematik.test.erezept.fhir.resources.kbv.KbvPatient;
import de.gematik.test.erezept.fhir.values.IKNR;
import de.gematik.test.erezept.fhir.valuesets.*;
import java.util.LinkedList;
import java.util.List;
import lombok.val;
import org.hl7.fhir.r4.model.Coverage;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Reference;

public class CoverageBuilder extends AbstractResourceBuilder<CoverageBuilder> {

  private PersonGroup personGroup = PersonGroup.NOT_SET;
  private DmpKennzeichen dmpKennzeichen = DmpKennzeichen.NOT_SET;
  private Wop wop = Wop.DUMMY;
  private VersichertenStatus versichertenStatus = VersichertenStatus.MEMBERS;
  private Coverage.CoverageStatus coverageStatus = Coverage.CoverageStatus.ACTIVE;
  private VersicherungsArtDeBasis versicherungsArt = VersicherungsArtDeBasis.GKV;

  private SubjectReference beneficiary;
  private IKNR iknr;
  private String insuranceName;

  public static CoverageBuilder insurance(String iknr, String insuranceName) {
    return insurance(new IKNR(iknr), insuranceName);
  }

  public static CoverageBuilder insurance(IKNR iknr, String insuranceName) {
    val insurance = new CoverageBuilder();
    insurance.iknr = iknr;
    insurance.insuranceName = insuranceName;
    return insurance;
  }

  public static CoverageBuilder faker() {
    return faker(randomElement(VersicherungsArtDeBasis.GKV, VersicherungsArtDeBasis.PKV));
  }

  public static CoverageBuilder faker(VersicherungsArtDeBasis insuranceType) {
    val builder = insurance(fakerIknr(), fakerName());

    IdentifierTypeDe identifierTypeDe =
        (insuranceType == VersicherungsArtDeBasis.PKV)
            ? IdentifierTypeDe.PKV
            : IdentifierTypeDe.GKV;

    builder
        .personGroup(fakerValueSet(PersonGroup.class))
        .beneficiary(PatientBuilder.faker(identifierTypeDe).build())
        .dmpKennzeichen(fakerValueSet(DmpKennzeichen.class))
        .wop(fakerValueSet(Wop.class))
        .versichertenStatus(fakerValueSet(VersichertenStatus.class));
    return builder;
  }

  public CoverageBuilder personGroup(PersonGroup personGroup) {
    this.personGroup = personGroup;
    return self();
  }

  public CoverageBuilder dmpKennzeichen(DmpKennzeichen kennzeichen) {
    this.dmpKennzeichen = kennzeichen;
    return self();
  }

  public CoverageBuilder wop(Wop wop) {
    this.wop = wop;
    return self();
  }

  public CoverageBuilder versichertenStatus(VersichertenStatus status) {
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
  public CoverageBuilder versicherungsArt(VersicherungsArtDeBasis versicherungsArt) {
    this.versicherungsArt = versicherungsArt;
    return self();
  }

  public CoverageBuilder beneficiary(KbvPatient patient) {
    this.versicherungsArt = patient.getInsuranceKind();
    return beneficiary(new SubjectReference(patient.getId()));
  }

  private CoverageBuilder beneficiary(SubjectReference subject) {
    this.beneficiary = subject;
    return self();
  }

  public Coverage build() {
    val coverage = new Coverage();

    val profile = ErpStructureDefinition.KBV_COVERAGE.asCanonicalType();
    val meta = new Meta().setProfile(List.of(profile));

    // set FHIR-specific values provided by HAPI
    coverage.setId(this.getResourceId()).setMeta(meta);

    val extensions = new LinkedList<Extension>();
    extensions.add(personGroup.asExtension());
    extensions.add(dmpKennzeichen.asExtension());
    extensions.add(wop.asExtension());
    extensions.add(versichertenStatus.asExtension());

    coverage
        .setStatus(coverageStatus)
        .setType(versicherungsArt.asCodeableConcept())
        .setBeneficiary(beneficiary)
        .setExtension(extensions);

    // set the payor
    val insuranceRef = new Reference().setDisplay(insuranceName);
    insuranceRef.setIdentifier(iknr.asIdentifier());
    coverage.setPayor(List.of(insuranceRef));

    return coverage;
  }
}
