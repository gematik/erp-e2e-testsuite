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

package de.gematik.test.erezept.fhir.resources.kbv;

import static java.text.MessageFormat.format;

import ca.uhn.fhir.model.api.annotation.*;
import de.gematik.test.erezept.fhir.exceptions.*;
import de.gematik.test.erezept.fhir.parser.profiles.systems.*;
import de.gematik.test.erezept.fhir.references.kbv.*;
import de.gematik.test.erezept.fhir.resources.*;
import de.gematik.test.erezept.fhir.values.KVNR;
import de.gematik.test.erezept.fhir.valuesets.*;
import java.util.*;
import java.util.concurrent.atomic.*;
import java.util.function.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.hl7.fhir.r4.model.*;

/** <a href="https://simplifier.net/base1x0/kbvprbasepatient">KBV Base Patient</a> */
@Slf4j
@Getter
@ResourceDef(name = "Patient")
@SuppressWarnings({"java:S110"})
public class KbvPatient extends Patient implements ErpFhirResource {

  public String getLogicalId() {
    return this.id.getIdPart();
  }

  public SubjectReference getReference() {
    return new SubjectReference(this.getId());
  }

  /**
   * Check if the patient has an ID for "gesetzliche Krankenversicherung"
   *
   * @return true if the Patient has a GKV ID
   */
  public boolean hasGkvKvnr() {
    return this.getGkvIdentifier().isPresent();
  }

  /**
   * Check if the patient has an ID for "private Krankenversicherung"
   *
   * @return true if the Patient has a PKV ID
   */
  public boolean hasPkvKvnr() {
    return getPkvIdentifier().isPresent();
  }

  public VersicherungsArtDeBasis getInsuranceKind() {
    return VersicherungsArtDeBasis.fromCode(getInsuranceKindCode());
  }

  private String getInsuranceKindCode() {
    return this.getIdentifier().stream()
        .map(id -> id.getType().getCodingFirstRep())
        .filter(
            coding ->
                coding
                    .getSystem()
                    .equals(DeBasisCodeSystem.IDENTIFIER_TYPE_DE_BASIS.getCanonicalUrl()))
        .map(Coding::getCode)
        .findFirst()
        .orElseThrow(
            () ->
                new MissingFieldException(
                    this.getClass(), DeBasisCodeSystem.IDENTIFIER_TYPE_DE_BASIS));
  }

  /**
   * Get the KVNR of the patient without bothering about the concrete insurance type
   *
   * @return the KVNR of the ptient
   */
  public KVNR getKvnr() {
    return getGkvId()
        .or(this::getPkvId)
        .orElseThrow(() -> new MissingFieldException(this.getClass(), "KVNR"));
  }

  /**
   * Get the GKV-KVNR of the patient
   *
   * @return Optional GKV-KVNR
   */
  public Optional<KVNR> getGkvId() {
    return getInsuranceId(this::getGkvIdentifier);
  }

  /**
   * Get the PKV-KVNR of the patient
   *
   * @return Optional PKV-KVNR
   */
  public Optional<KVNR> getPkvId() {
    return getInsuranceId(this::getPkvIdentifier);
  }

  private Optional<KVNR> getInsuranceId(Supplier<Optional<Identifier>> identifierSupplier) {
    val opt = identifierSupplier.get();
    return opt.map(identifier -> KVNR.from(identifier.getValue()));
  }

  private Optional<Identifier> getGkvIdentifier() {
    return this.getIdentifier().stream()
        .filter(
            identifier ->
                DeBasisNamingSystem.KVID.match(identifier)
                    || DeBasisNamingSystem.KVID_GKV.match(identifier))
        .findFirst();
  }

  private Optional<Identifier> getPkvIdentifier() {
    /*
    Why is the identifier not fetched identically as the GKV one?
    - because on old profiles (1.0.2) PKV-Identifier didn't contain a system-value,
    thus using the type-code is the only way to find PKV-Identifier for older profiles
    - for newer profiles (1.1.0+) the identifier can be selected either via type-code or via system-value
    */
    return this.getIdentifier().stream()
        .filter(
            identifier ->
                identifier.getType().getCoding().stream()
                    .anyMatch(
                        coding ->
                            coding.getCode().equalsIgnoreCase(IdentifierTypeDe.PKV.getCode())))
        .findFirst();
  }

  public Optional<Reference> getPkvAssigner() {
    return this.getPkvIdentifier().map(Identifier::getAssigner);
  }

  public Optional<String> getPkvAssignerName() {
    return this.getPkvIdentifier().map(identifier -> identifier.getAssigner().getDisplay());
  }

  public String getFullname() {
    val hn = this.getNameFirstRep();
    return format("{0}, {1}", hn.getFamily(), hn.getGivenAsSingleString());
  }

  @Override
  public String getDescription() {
    return format(
        "{0} Versicherte/r {1} (KVNR: {2})",
        this.getInsuranceKind(), this.getFullname(), this.getKvnr().getValue());
  }

  public static KbvPatient fromPatient(Patient adaptee) {
    if (adaptee instanceof KbvPatient kbvPatient) {
      return kbvPatient;
    } else {
      val kbvPatient = new KbvPatient();
      adaptee.copyValues(kbvPatient);
      return kbvPatient;
    }
  }

  public static KbvPatient fromPatient(Resource adaptee) {
    return fromPatient((Patient) adaptee);
  }
}
