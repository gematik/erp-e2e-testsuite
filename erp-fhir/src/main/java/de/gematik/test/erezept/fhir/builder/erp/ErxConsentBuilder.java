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

package de.gematik.test.erezept.fhir.builder.erp;

import de.gematik.test.erezept.fhir.builder.AbstractResourceBuilder;
import de.gematik.test.erezept.fhir.parser.profiles.ErpNamingSystem;
import de.gematik.test.erezept.fhir.parser.profiles.ErpStructureDefinition;
import de.gematik.test.erezept.fhir.resources.erp.ErxConsent;
import de.gematik.test.erezept.fhir.valuesets.ActCode;
import de.gematik.test.erezept.fhir.valuesets.ConsentScope;
import de.gematik.test.erezept.fhir.valuesets.ConsentType;
import java.util.Date;
import java.util.List;
import lombok.val;
import org.hl7.fhir.r4.model.Consent;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Reference;

public class ErxConsentBuilder extends AbstractResourceBuilder<ErxConsentBuilder> {

  private String kvid;
  private ActCode policyRule = ActCode.OPTIN;
  private Consent.ConsentState status = Consent.ConsentState.ACTIVE;
  private ConsentScope scope = ConsentScope.PATIENT_PRIVACY;
  private ConsentType consentType = ConsentType.CHARGCONS;

  public static ErxConsentBuilder forKvid(String kvid) {
    val builder = new ErxConsentBuilder();
    builder.kvid = kvid;
    return builder;
  }

  public ErxConsentBuilder policyRule(ActCode policyRule) {
    this.policyRule = policyRule;
    return self();
  }

  public ErxConsentBuilder status(Consent.ConsentState status) {
    this.status = status;
    return self();
  }

  public ErxConsentBuilder scope(ConsentScope scope) {
    this.scope = scope;
    return self();
  }

  public ErxConsent build() {
    val consent = new ErxConsent();

    val profile = ErpStructureDefinition.GEM_CONSENT.asCanonicalType();
    val meta = new Meta().setProfile(List.of(profile));

    // set FHIR-specific values provided by HAPI
    consent.setMeta(meta);

    consent.setPatient(
        new Reference()
            .setIdentifier(
                new Identifier().setSystem(ErpNamingSystem.KVID.getCanonicalUrl()).setValue(kvid)));
    consent.setPolicyRule(policyRule.asCodeableConcept());
    consent.setStatus(status);
    consent.setScope(scope.asCodeableConcept());
    consent.setCategory(List.of(consentType.asCodeableConcept()));
    consent.setDateTime(new Date());

    return consent;
  }
}
