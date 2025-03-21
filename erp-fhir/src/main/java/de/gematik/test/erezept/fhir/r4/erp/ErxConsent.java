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

package de.gematik.test.erezept.fhir.r4.erp;

import ca.uhn.fhir.model.api.annotation.ResourceDef;
import de.gematik.bbriccs.fhir.coding.WithSystem;
import de.gematik.bbriccs.fhir.coding.exceptions.MissingFieldException;
import de.gematik.bbriccs.fhir.de.valueset.ConsentScope;
import de.gematik.test.erezept.fhir.parser.profiles.systems.ErpWorkflowCodeSystem;
import de.gematik.test.erezept.fhir.parser.profiles.systems.PatientenrechnungCodeSystem;
import de.gematik.test.erezept.fhir.valuesets.ConsentType;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Consent;
import org.hl7.fhir.r4.model.Resource;

/**
 * @see <a href="https://simplifier.net/erezept-workflow/erxconsent">ErxConsent</a>
 */
@Slf4j
@ResourceDef(name = "Consent")
@SuppressWarnings({"java:S110"})
public class ErxConsent extends Consent {

  public ConsentType getConsentType() {
    return this.getCategory().stream()
        .filter(
            category ->
                WithSystem.anyOf(
                        ErpWorkflowCodeSystem.CONSENT_TYPE,
                        PatientenrechnungCodeSystem.CONSENT_TYPE)
                    .matches(category))
        .map(CodeableConcept::getCodingFirstRep)
        .map(coding -> ConsentType.fromCode(coding.getCode()))
        .findFirst()
        .orElseThrow(
            () -> new MissingFieldException(this.getClass(), ErpWorkflowCodeSystem.CONSENT_TYPE));
  }

  public String getConsentId() {
    val idTokens = this.getId().split("/");
    return idTokens[idTokens.length - 1];
  }

  public ConsentScope getConsentScope() {
    return ConsentScope.fromCode(this.getScope().getCodingFirstRep().getCode());
  }

  public String getPatientKvid() {
    return this.getPatient().getIdentifier().getValue();
  }

  public static ErxConsent fromConsent(Consent adaptee) {
    val erxConsent = new ErxConsent();
    adaptee.copyValues(erxConsent);
    return erxConsent;
  }

  public static ErxConsent fromConsent(Resource adaptee) {
    return fromConsent((Consent) adaptee);
  }
}
