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

package de.gematik.test.erezept.eml;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class RuleIdentifierTest {

  @Test
  void testEnumValues() {
    RuleIdentifier providePrescription = RuleIdentifier.PROVIDE_PRESCRIPTION;
    assertEquals("providePrescription", providePrescription.getUrlname());

    RuleIdentifier cancelPrescription = RuleIdentifier.CANCEL_PRESCRIPTION;
    assertEquals("cancelPrescription", cancelPrescription.getUrlname());

    RuleIdentifier provideDispensation = RuleIdentifier.PROVIDE_DISPENSATION;
    assertEquals("provideDispensation", provideDispensation.getUrlname());

    RuleIdentifier cancelDispensation = RuleIdentifier.CANCEL_DISPENSATION;
    assertEquals("cancelDispensation", cancelDispensation.getUrlname());

    RuleIdentifier getConsentDecisions = RuleIdentifier.CONSENT_DECISION;
    assertEquals("getConsentDecisions", getConsentDecisions.getUrlname());
  }
}
