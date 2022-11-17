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

package de.gematik.test.erezept.fhir.resources.kbv;

import ca.uhn.fhir.model.api.annotation.ResourceDef;
import de.gematik.test.erezept.fhir.exceptions.MissingFieldException;
import de.gematik.test.erezept.fhir.parser.profiles.systems.DeBasisCodeSystem;
import de.gematik.test.erezept.fhir.parser.profiles.systems.DeBasisNamingSystem;
import de.gematik.test.erezept.fhir.references.kbv.SubjectReference;
import de.gematik.test.erezept.fhir.valuesets.IdentifierTypeDe;
import de.gematik.test.erezept.fhir.valuesets.VersicherungsArtDeBasis;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.r4.model.*;

/** <a href="https://simplifier.net/base1x0/kbvprbasepatient">KBV Base Patient</a> */
@Slf4j
@Getter
@ResourceDef(name = "Patient")
@SuppressWarnings({"java:S110"})
public class KbvPatient extends Patient {

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
  public boolean hasGkvId() {
    return getKvid().isPresent();
  }

  /**
   * Check if the patient has an ID for "private Krankenversicherung"
   *
   * @return true if the Patient has a PKV ID
   */
  public boolean hasPkvId() {
    return getPkvId().isPresent();
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

  public Optional<String> getKvid() {
    return getInsuranceId(this::getKvidIdentifier);
  }

  public Optional<String> getPkvId() {
    return getInsuranceId(this::getPkvIdentifier);
  }

  private Optional<String> getInsuranceId(Supplier<Optional<Identifier>> func) {
    val opt = func.get();
    Optional<String> ret = Optional.empty();
    if (opt.isPresent()) {
      ret = Optional.of(opt.get().getValue());
    }
    return ret;
  }

  private Optional<Identifier> getKvidIdentifier() {
    return this.getIdentifier().stream()
        .filter(
            identifier -> identifier.getSystem().equals(DeBasisNamingSystem.KVID.getCanonicalUrl()))
        //                .filter(identifier ->
        // identifier.getValue().equals(IdentifierTypeDe.GKV.getCode()))
        .findFirst();
  }

  private Optional<Identifier> getPkvIdentifier() {
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
    val pkvIdentifer = this.getPkvIdentifier();

    AtomicReference<Optional<Reference>> ret = new AtomicReference<>(Optional.empty());
    pkvIdentifer.ifPresent(identifier -> ret.set(Optional.ofNullable(identifier.getAssigner())));
    return ret.get();
  }

  public Optional<String> getPkvAssignerName() {
    val pkvIdentifer = this.getPkvIdentifier();

    AtomicReference<Optional<String>> ret = new AtomicReference<>(Optional.empty());
    pkvIdentifer.ifPresent(
        identifier -> ret.set(Optional.ofNullable(identifier.getAssigner().getDisplay())));
    return ret.get();
  }

  public static KbvPatient fromPatient(Patient adaptee) {
    val kbvBasePatient = new KbvPatient();
    adaptee.copyValues(kbvBasePatient);
    return kbvBasePatient;
  }

  public static KbvPatient fromPatient(Resource adaptee) {
    return fromPatient((Patient) adaptee);
  }
}
