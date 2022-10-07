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
import de.gematik.test.erezept.fhir.resources.kbv.KbvPractitioner;
import de.gematik.test.erezept.fhir.values.BaseANR;
import de.gematik.test.erezept.fhir.values.LANR;
import de.gematik.test.erezept.fhir.values.ZANR;
import de.gematik.test.erezept.fhir.valuesets.QualificationType;
import java.util.LinkedList;
import java.util.List;
import lombok.NonNull;
import lombok.val;
import org.hl7.fhir.r4.model.*;

public class PractitionerBuilder extends AbstractResourceBuilder<PractitionerBuilder> {

  private final List<Identifier> identifiers = new LinkedList<>();

  private HumanName name;
  private final List<Practitioner.PractitionerQualificationComponent> qualifications =
      new LinkedList<>();

  public static PractitionerBuilder builder() {
    return new PractitionerBuilder();
  }

  public static PractitionerBuilder faker() {
    return faker(fakerFirstName(), fakerLastName());
  }

  public static PractitionerBuilder faker(String fullName) {
    val tokens = fullName.split(" ");
    val firstName = tokens[0] != null ? tokens[0] : fakerFirstName();
    val lastName = tokens[1] != null ? tokens[1] : fakerLastName();
    return faker(firstName, lastName);
  }

  public static PractitionerBuilder faker(String firstName, String lastName) {
    val builder = builder();
    builder
        .lanr(fakerLanr())
        .name(firstName, lastName, "Dr.") // Doctors always have Dr. in Germany ;)
        .addQualification(fakerQualificationType())
        .addQualification(fakerProfession());
    return builder;
  }

  /**
   * Lebenslange Arztnummer
   *
   * @param value the LANR value
   * @return builder
   */
  public PractitionerBuilder lanr(@NonNull String value) {
    return anr(new LANR(value));
  }

  public PractitionerBuilder zanr(@NonNull String value) {
    return anr(new ZANR(value));
  }

  /**
   * set either a "Lebenslange Arztnummer" or "Zahnarztnummer"
   *
   * @param anr is the object which wraps the coding system and the value
   * @return builder
   */
  public PractitionerBuilder anr(@NonNull BaseANR anr) {
    this.identifiers.add(anr.asIdentifier());
    return self();
  }

  /**
   * Set the official name of the practitioner
   *
   * @param given given name
   * @param family family name
   * @return builder
   */
  public PractitionerBuilder name(@NonNull String given, @NonNull String family) {
    return name(given, family, "");
  }

  /**
   * Set the official name of the practitioner with a prefix e.g. Dr.
   *
   * @param given given name
   * @param family family name
   * @return builder
   */
  public PractitionerBuilder name(@NonNull String given, @NonNull String family, String prefix) {
    this.name = new HumanName();
    this.name.setUse(HumanName.NameUse.OFFICIAL);
    this.name.addGiven(given).setFamily(family);

    if (prefix != null && prefix.length() > 0) {
      this.name.addPrefix(prefix);
      // ISO21090 http://hl7.org/fhir/R4/extension-iso21090-en-qualifier.html
      // required in ErpStructureDefinition?
      this.name
          .getPrefix()
          .get(0)
          .addExtension(
              "http://hl7.org/fhir/StructureDefinition/iso21090-EN-qualifier", new CodeType("AC"));
    }

    return self();
  }

  public PractitionerBuilder addQualification(QualificationType qualificationType) {
    this.qualifications.add(
        new Practitioner.PractitionerQualificationComponent(qualificationType.asCodeableConcept()));
    return self();
  }

  public PractitionerBuilder addQualification(@NonNull String qualification) {
    val codeable = new CodeableConcept().setText(qualification);
    this.qualifications.add(new Practitioner.PractitionerQualificationComponent(codeable));
    return self();
  }

  public KbvPractitioner build() {
    val practitioner = new KbvPractitioner();

    val profile = ErpStructureDefinition.KBV_PRACTITIONER.asCanonicalType();
    val meta = new Meta().setProfile(List.of(profile));

    // set FHIR-specific values provided by HAPI
    practitioner.setId(this.getResourceId()).setMeta(meta);

    practitioner.setIdentifier(identifiers).setQualification(qualifications).setName(List.of(name));

    return practitioner;
  }
}
