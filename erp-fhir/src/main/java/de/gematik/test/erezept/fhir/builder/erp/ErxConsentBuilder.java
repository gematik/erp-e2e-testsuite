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

package de.gematik.test.erezept.fhir.builder.erp;

import de.gematik.bbriccs.fhir.builder.ResourceBuilder;
import de.gematik.bbriccs.fhir.coding.WithCodeSystem;
import de.gematik.bbriccs.fhir.de.DeBasisProfilNamingSystem;
import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.bbriccs.fhir.de.valueset.ActCode;
import de.gematik.bbriccs.fhir.de.valueset.ConsentScope;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.ErpWorkflowStructDef;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.PatientenrechnungStructDef;
import de.gematik.test.erezept.fhir.parser.profiles.systems.ErpWorkflowCodeSystem;
import de.gematik.test.erezept.fhir.parser.profiles.systems.PatientenrechnungCodeSystem;
import de.gematik.test.erezept.fhir.parser.profiles.version.ErpWorkflowVersion;
import de.gematik.test.erezept.fhir.parser.profiles.version.PatientenrechnungVersion;
import de.gematik.test.erezept.fhir.r4.erp.ErxConsent;
import de.gematik.test.erezept.fhir.valuesets.ConsentType;
import java.util.Date;
import java.util.List;
import lombok.val;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.Consent;

public class ErxConsentBuilder extends ResourceBuilder<ErxConsent, ErxConsentBuilder> {

  private ErpWorkflowVersion erpWorkflowVersion = ErpWorkflowVersion.getDefaultVersion();
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

  public ErxConsentBuilder version(ErpWorkflowVersion version) {
    this.erpWorkflowVersion = version;
    return self();
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

  @Override
  public ErxConsent build() {
    CanonicalType profile;
    DeBasisProfilNamingSystem kvnrNamingSystem;
    WithCodeSystem consentTypeCodeSystem;
    if (erpWorkflowVersion.compareTo(ErpWorkflowVersion.V1_1_1) == 0) {
      profile = ErpWorkflowStructDef.CONSENT.asCanonicalType();
      kvnrNamingSystem = DeBasisProfilNamingSystem.KVID;
      consentTypeCodeSystem = ErpWorkflowCodeSystem.CONSENT_TYPE;
    } else {
      profile =
          PatientenrechnungStructDef.GEM_ERPCHRG_PR_CONSENT.asCanonicalType(
              PatientenrechnungVersion.V1_0_0);
      kvnrNamingSystem = DeBasisProfilNamingSystem.KVID_PKV_SID;
      consentTypeCodeSystem = PatientenrechnungCodeSystem.CONSENT_TYPE;
    }

    val consent = this.createResource(ErxConsent::new, profile);

    consent.setPatient(kvnr.asReference(kvnrNamingSystem, false));
    consent.setPolicyRule(policyRule.asCodeableConcept());
    consent.setStatus(status);
    consent.setScope(scope.asCodeableConcept());
    consent.setCategory(List.of(CONSENT_TYPE.asCodeableConcept(consentTypeCodeSystem)));
    consent.setDateTime(new Date());

    return consent;
  }
}
