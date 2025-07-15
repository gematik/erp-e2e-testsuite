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
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 */

package de.gematik.test.erezept.fhir.profiles.systems;

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.fhir.coding.WithCodeSystem;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AbdaCodeSystem implements WithCodeSystem {
  INVOICE_TYPE("http://fhir.abda.de/eRezeptAbgabedaten/CodeSystem/DAV-CS-ERP-InvoiceTyp"),
  MEDICATIONDISPENSE_TYPE(
      "http://fhir.abda.de/eRezeptAbgabedaten/CodeSystem/DAV-CS-ERP-MedicationDispenseTyp"),
  KOSTEN_VERSICHERTER_KATEGORIE(
      "http://fhir.abda.de/eRezeptAbgabedaten/CodeSystem/DAV-PKV-CS-ERP-KostenVersicherterKategorie"),
  ZUSATZATTRIBUTE_GRUPPE(
      "http://fhir.abda.de/eRezeptAbgabedaten/CodeSystem/DAV-CS-ERP-ZusatzattributGruppe"),
  ZUSATZATTRIBUTE_FAM_SCHLUESSEL_MARKT(
      "http://fhir.abda.de/eRezeptAbgabedaten/CodeSystem/DAV-CS-ERP-ZusatzattributFAMSchluesselMarkt"),
  ABRECHNUNGSTYP("http://fhir.abda.de/eRezeptAbgabedaten/CodeSystem/DAV-PKV-CS-ERP-AbrechnungsTyp"),
  COMPOSITION_TYPES(
      "http://fhir.abda.de/eRezeptAbgabedaten/CodeSystem/DAV-CS-ERP-CompositionTypes");

  private final String canonicalUrl;

  @Override
  public String toString() {
    return format("{0}({1})", this.name(), this.canonicalUrl);
  }
}
