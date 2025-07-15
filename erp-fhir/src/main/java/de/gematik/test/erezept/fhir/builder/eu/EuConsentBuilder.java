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

package de.gematik.test.erezept.fhir.builder.eu;

import de.gematik.bbriccs.fhir.builder.ResourceBuilder;
import de.gematik.bbriccs.fhir.de.DeBasisProfilNamingSystem;
import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.bbriccs.fhir.de.valueset.ActCode;
import de.gematik.bbriccs.fhir.de.valueset.ConsentScope;
import de.gematik.test.erezept.fhir.profiles.definitions.ErpEuStructDef;
import de.gematik.test.erezept.fhir.profiles.version.EuVersion;
import de.gematik.test.erezept.fhir.r4.eu.EuConsent;
import de.gematik.test.erezept.fhir.valuesets.EuConsentType;
import java.util.Date;
import java.util.List;
import lombok.val;
import org.hl7.fhir.r4.model.Consent;

public class EuConsentBuilder extends ResourceBuilder<EuConsent, EuConsentBuilder> {

  private EuVersion version = EuVersion.getDefaultVersion();
  private KVNR kvnr;
  private ActCode policyRule = ActCode.OPTIN;
  private Consent.ConsentState status = Consent.ConsentState.ACTIVE;
  private ConsentScope scope = ConsentScope.PATIENT_PRIVACY;
  private static final EuConsentType CONSENT_TYPE = EuConsentType.EUDISPCONS;

  public static EuConsentBuilder forKvnr(KVNR kvnr) {
    val builder = new EuConsentBuilder();
    builder.kvnr = kvnr;
    return builder;
  }

  public EuConsentBuilder version(EuVersion version) {
    this.version = version;
    return this;
  }

  public EuConsentBuilder policyRule(ActCode policyRule) {
    this.policyRule = policyRule;
    return this;
  }

  public EuConsentBuilder status(Consent.ConsentState status) {
    this.status = status;
    return this;
  }

  public EuConsentBuilder scope(ConsentScope scope) {
    this.scope = scope;
    return this;
  }

  @Override
  public EuConsent build() {
    val consent = this.createResource(EuConsent::new, ErpEuStructDef.CONSENT_TYPE, version);

    consent.setPatient(kvnr.asReference(DeBasisProfilNamingSystem.KVID_GKV_SID, false));
    consent.setPolicyRule(policyRule.asCodeableConcept());
    consent.setStatus(status);
    consent.setScope(scope.asCodeableConcept());
    consent.setCategory(List.of(CONSENT_TYPE.asCodeableConcept(true)));
    consent.setDateTime(new Date());

    return consent;
  }
}
