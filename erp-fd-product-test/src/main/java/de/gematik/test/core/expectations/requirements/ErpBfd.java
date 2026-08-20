/*
 * Copyright (Change Date see Readme), gematik GmbH
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

package de.gematik.test.core.expectations.requirements;

import lombok.Getter;

public enum ErpBfd implements RequirementsSet {
  B_FD_1357(
      "B_FD-1357", "C_12295 E-Rezept: Verordnungen zu Lasten sonstiger Kostenträger ermöglichen"),
  B_FD_1506("B_FD-1506", "Patch Task schlägt mit HTTP 500 fehl - PatchTaskHandler.cxx:50"),

  B_FD_1661(
      "B_FD-1661",
      "Dosierungstext: Unterschied 'nachts' und 'zur Nacht',"
          + " https://service.gematik.de/browse/B_FD-1661"),
  B_FD_1659(
      "B-FD_1659",
      "Sortierung von Dosierungen im Uhrzeiten-Schema,"
          + " https://service.gematik.de/browse/B_FD-1659"),
  B_FD_1676("B_FD-1676", "Strukturierte Dosierungen: Angabe mehrfacher Schemata eingrenzen"),
  B_FD_1685("B_FD-1685", "Update kbv.ita.erp 1.4.4"),
  B_FD_1687("B_FD-1687", "Strukturiere Dosierung: boundsDuration"),
  B_FD_1699(
      "B-FD_1699", "Abweichende Generierung / Validierung der Strukturierten Dosierinformationen"),
  ;

  @Getter private final Requirement requirement;

  ErpBfd(String id, String description) {
    this.requirement = new Requirement(id, description);
  }

  @Override
  public String toString() {
    return requirement.toString();
  }
}
