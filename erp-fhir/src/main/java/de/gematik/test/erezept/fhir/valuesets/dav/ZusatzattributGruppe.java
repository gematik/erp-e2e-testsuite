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

package de.gematik.test.erezept.fhir.valuesets.dav;

import de.gematik.bbriccs.fhir.coding.FromValueSet;
import de.gematik.bbriccs.fhir.coding.exceptions.InvalidValueSetException;
import de.gematik.test.erezept.fhir.profiles.systems.AbdaCodeSystem;
import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@SuppressWarnings({"java:S1192"})
public enum ZusatzattributGruppe implements FromValueSet {
  MARKT("1", "Markt", "Pflichtangabe bei Fertigarzneimitteln"),
  RABATT("2", "Rabattvertragserfüllung", "Pflichtangabe bei Fertigarzneimitteln"),
  FAM("3", "Preisgünstiges FAM", "Pflichtangabe bei Fertigarzneimitteln"),
  IMPORT_FAM("4", "Import-FAM", "Pflichtangabe bei Fertigarzneimitteln"),
  MEHRKOSTEN("5", "Mehrkostenübernahme"),
  WUNSCHARZNEIMITTEL("6", "Wunscharzneimittel"),
  WIRKSTOFFVERORDNUNG("7", "Wirkstoffverordnung"),
  ERSATZVERORDNUNG("8", "Ersatzverordnung"),
  KUENSTLICHE_BEFRUCHTUNG("9", "künstliche Befruchtung"),
  IMPORT_FERTIGARZNEI(
      "10",
      "einzeln importierte Fertigarzneimittel (§ 73 Abs. 3 AMG)",
      "bedingt verpflichtendes Freitextfeld"),
  ABGABE_NOTDIENST("11", "Abgabe im Notdienst", "bedingt Datum und Uhrzeit"),
  ZUSAETZLICHE_ABGABEDATEN(
      "12", "zusätzliche Abgabeangaben", "bedingt verpflichtendes Freitextfeld"),
  GENEHMIGUNG(
      "13", "Genehmigungen", "bedingt verpflichtendes Freitextfeld (Genehmigungsnummer) und Datum"),
  TARIFF_KENNZEICHEN(
      "14",
      "Tarifkennzeichen",
      "bedingt Codes, für das Tarifkennzeichen und Kennzeichen für den Sondertarif"),
  ZUZAHLUNGSSTATUS("15", "Zuzahlungsstatus", "von Zuzahlungspflicht befreit"),
  ;

  public static final AbdaCodeSystem CODE_SYSTEM = AbdaCodeSystem.ZUSATZATTRIBUTE_GRUPPE;
  public static final String DESCRIPTION = "Group of additional attributes";

  private final String code;
  private final String display;
  private final String definition;

  ZusatzattributGruppe(String code, String display) {
    this(code, display, "n/a");
  }

  @Override
  public AbdaCodeSystem getCodeSystem() {
    return CODE_SYSTEM;
  }

  public static ZusatzattributGruppe fromCode(String code) {
    return Arrays.stream(ZusatzattributGruppe.values())
        .filter(zag -> zag.code.equals(code))
        .findFirst()
        .orElseThrow(() -> new InvalidValueSetException(ZusatzattributGruppe.class, code));
  }
}
