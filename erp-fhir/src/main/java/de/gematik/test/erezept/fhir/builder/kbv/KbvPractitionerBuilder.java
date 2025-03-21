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
 */

package de.gematik.test.erezept.fhir.builder.kbv;

import de.gematik.bbriccs.fhir.builder.ResourceBuilder;
import de.gematik.bbriccs.fhir.de.builder.HumanNameBuilder;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.KbvItaForStructDef;
import de.gematik.test.erezept.fhir.parser.profiles.systems.KbvCodeSystem;
import de.gematik.test.erezept.fhir.parser.profiles.systems.KbvNamingSystem;
import de.gematik.test.erezept.fhir.parser.profiles.version.KbvItaForVersion;
import de.gematik.test.erezept.fhir.r4.kbv.KbvPractitioner;
import de.gematik.test.erezept.fhir.values.AsvFachgruppennummer;
import de.gematik.test.erezept.fhir.values.BaseANR;
import de.gematik.test.erezept.fhir.values.LANR;
import de.gematik.test.erezept.fhir.values.ZANR;
import de.gematik.test.erezept.fhir.valuesets.QualificationType;
import java.util.LinkedList;
import java.util.List;
import lombok.val;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Practitioner;

public class KbvPractitionerBuilder
    extends ResourceBuilder<KbvPractitioner, KbvPractitionerBuilder> {

  private final List<Identifier> identifiers = new LinkedList<>();
  private final List<Practitioner.PractitionerQualificationComponent> qualifications =
      new LinkedList<>();
  private KbvItaForVersion kbvItaForVersion = KbvItaForVersion.getDefaultVersion();
  private BaseANR baseAnr;
  private String givenName;
  private String familyName;
  private String namePrefix;
  private String jobTitle;

  public static KbvPractitionerBuilder builder() {
    return new KbvPractitionerBuilder();
  }

  /**
   * <b>Attention:</b> use with care as this setter might break automatic choice of the version.
   * This builder will set the default version automatically, so there should be no need to provide
   * an explicit version
   *
   * @param version to use for generation of this resource
   * @return Builder
   */
  public KbvPractitionerBuilder version(KbvItaForVersion version) {
    this.kbvItaForVersion = version;
    return this;
  }

  /**
   * Lebenslange Arztnummer
   *
   * @param value the LANR value
   * @return builder
   */
  public KbvPractitionerBuilder lanr(String value) {
    return anr(new LANR(value));
  }

  public KbvPractitionerBuilder zanr(String value) {
    return anr(new ZANR(value));
  }

  /**
   * set either a "Lebenslange Arztnummer" or "Zahnarztnummer"
   *
   * @param anr is the object which wraps the coding system and the value
   * @return builder
   */
  public KbvPractitionerBuilder anr(BaseANR anr) {
    this.baseAnr = anr;
    return self();
  }

  /**
   * Set the official name of the practitioner
   *
   * @param given given name
   * @param family family name
   * @return builder
   */
  public KbvPractitionerBuilder name(String given, String family) {
    return name(given, family, "");
  }

  /**
   * Set the official name of the practitioner with a prefix e.g. Dr.
   *
   * @param given given name
   * @param family family name
   * @return builder
   */
  public KbvPractitionerBuilder name(String given, String family, String prefix) {
    this.givenName = given;
    this.familyName = family;
    this.namePrefix = prefix;

    return self();
  }

  public KbvPractitionerBuilder addQualification(QualificationType qualificationType) {
    this.qualifications.add(
        new Practitioner.PractitionerQualificationComponent(qualificationType.asCodeableConcept()));
    return self();
  }

  public KbvPractitionerBuilder addQualification(AsvFachgruppennummer asvFachgruppennummer) {
    this.qualifications.add(
        new Practitioner.PractitionerQualificationComponent(
            asvFachgruppennummer.asCodeableConcept()));
    return self();
  }

  public KbvPractitionerBuilder addQualification(String jobTitle) {
    this.jobTitle = jobTitle;
    return self();
  }

  @Override
  public KbvPractitioner build() {
    val practitioner =
        this.createResource(
            KbvPractitioner::new, KbvItaForStructDef.PRACTITIONER, kbvItaForVersion);

    val humanNameBuilder =
        HumanNameBuilder.official()
            .prefix(this.namePrefix)
            .given(this.givenName)
            .family(this.familyName);

    HumanName humanName;

    if (kbvItaForVersion.compareTo(KbvItaForVersion.V1_1_0) < 0) {
      val codeable = new CodeableConcept().setText(jobTitle);
      this.qualifications.add(new Practitioner.PractitionerQualificationComponent(codeable));
      if (this.baseAnr != null) {
        this.identifiers.add(this.baseAnr.asIdentifier());
      }
      humanName = humanNameBuilder.buildSimple();
    } else {
      val codeable =
          KbvCodeSystem.BERUFSBEZEICHNUNG.asCodeableConcept("Berufsbezeichnung").setText(jobTitle);
      this.qualifications.add(new Practitioner.PractitionerQualificationComponent(codeable));
      if (this.baseAnr != null) {
        if (this.baseAnr.getType().equals(BaseANR.ANRType.ZANR)) {
          this.identifiers.add(this.baseAnr.asIdentifier(KbvNamingSystem.ZAHNARZTNUMMER));
        } else {
          this.identifiers.add(this.baseAnr.asIdentifier());
        }
      }
      humanName = humanNameBuilder.build();
    }

    practitioner
        .setIdentifier(identifiers)
        .setQualification(qualifications)
        .setName(List.of(humanName));

    return practitioner;
  }
}
