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

package de.gematik.test.erezept.fhir.builder.erp;

import de.gematik.bbriccs.fhir.builder.ResourceBuilder;
import de.gematik.bbriccs.fhir.de.DeBasisProfilNamingSystem;
import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.bbriccs.fhir.de.valueset.ActCode;
import de.gematik.bbriccs.fhir.de.valueset.ConsentScope;
import de.gematik.test.erezept.fhir.profiles.definitions.PatientenrechnungStructDef;
import de.gematik.test.erezept.fhir.profiles.version.PatientenrechnungVersion;
import de.gematik.test.erezept.fhir.r4.erp.ErxConsent;
import de.gematik.test.erezept.fhir.valuesets.ConsentType;
import java.util.Date;
import java.util.List;
import lombok.val;
import org.hl7.fhir.r4.model.Consent;

public class ErxConsentBuilder extends ResourceBuilder<ErxConsent, ErxConsentBuilder> {

  private PatientenrechnungVersion version = PatientenrechnungVersion.V1_0_0;
  private KVNR kvnr;
  private ActCode policyRule = ActCode.OPTIN;
  private Consent.ConsentState status = Consent.ConsentState.ACTIVE;
  private ConsentScope scope = ConsentScope.PATIENT_PRIVACY;
  private static final ConsentType CONSENT_TYPE = ConsentType.CHARGCONS;

  public static ErxConsentBuilder forKvnr(KVNR kvnr) {
    val builder = new ErxConsentBuilder();
    builder.kvnr = kvnr;
    return builder;
  }

  public ErxConsentBuilder version(PatientenrechnungVersion version) {
    this.version = version;
    return this;
  }

  public ErxConsentBuilder policyRule(ActCode policyRule) {
    this.policyRule = policyRule;
    return this;
  }

  public ErxConsentBuilder status(Consent.ConsentState status) {
    this.status = status;
    return this;
  }

  public ErxConsentBuilder scope(ConsentScope scope) {
    this.scope = scope;
    return this;
  }

  @Override
  public ErxConsent build() {
    val consent = this.createResource(ErxConsent::new, PatientenrechnungStructDef.CONSENT, version);

    // kvnr system hardcoded because we need to ensure first, a proper KVNR is always given!
    // same issue as in ErxChargeItemBuilder.checkPrerequisites
    consent.setPatient(kvnr.asReference(DeBasisProfilNamingSystem.KVID_PKV_SID, false));
    consent.setPolicyRule(policyRule.asCodeableConcept());
    consent.setStatus(status);
    consent.setScope(scope.asCodeableConcept());
    consent.setCategory(List.of(CONSENT_TYPE.asCodeableConcept()));
    consent.setDateTime(new Date());

    return consent;
  }
}
