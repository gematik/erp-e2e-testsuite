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

package de.gematik.test.core.expectations.requirements;

import lombok.Getter;

public enum FhirRequirements implements RequirementsSet {
  LOGICAL_ID(
      "FHIR_2.26.3.3",
      "Each resource has an id element which contains the 'logical id' of the resource"),
  FHIR_PROFILES("FHIR_PROFILES", "FHIR Ressourcen müssen gegen die Profilierung validiert werden"),
  NON_WHITESPACE_CONTENT(
      "FHIR_2.6.1",
      "Attributes cannot be empty. Either they are absent, or they are present with at least one"
          + " character of non-whitespace content"),
  FHIR_XML_PARSING("FHIR_XML_PARSING", "Invalide FHIR Ressourcen dürfen nicht zum Absturz führen"),

  FHIR_VALIDATION_ERROR("FHIR-Validation", "die Semantik entspricht nicht der Profilierung");

  @Getter private final Requirement requirement;

  FhirRequirements(String id, String description) {
    this.requirement = new Requirement(id, description);
  }
}
