/*
 * Copyright 2024 gematik GmbH
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

package de.gematik.test.erezept.fhir.builder.kbv;

import de.gematik.bbriccs.fhir.builder.ResourceBuilder;
import de.gematik.test.erezept.fhir.parser.profiles.version.ProfileVersion;
import de.gematik.test.erezept.fhir.resources.kbv.KbvCoverage;
import de.gematik.test.erezept.fhir.resources.kbv.KbvPatient;
import de.gematik.test.erezept.fhir.resources.kbv.KbvPractitioner;
import de.gematik.test.erezept.fhir.resources.kbv.MedicalOrganization;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.valuesets.StatusKennzeichen;
import org.hl7.fhir.r4.model.Resource;

public abstract class KbvBaseDocumentBundleBuilder<
        V extends ProfileVersion<V>, R extends Resource, B extends ResourceBuilder<R, B>>
    extends ResourceBuilder<R, B> {

  protected V version;
  protected final KbvBaseCompositionBuilder<V, ?> compositionBuilder;

  protected PrescriptionId prescriptionId;
  protected StatusKennzeichen statusKennzeichen = StatusKennzeichen.NONE;

  protected KbvPatient patient;
  protected KbvPractitioner practitioner;
  protected KbvPractitioner attester;
  protected KbvCoverage coverage;
  protected MedicalOrganization medicalOrganization; // the organization issuing the prescription

  protected KbvBaseDocumentBundleBuilder(
      KbvBaseCompositionBuilder<V, ?> compositionBuilder, V defaultVersion) {
    this.compositionBuilder = compositionBuilder;
    this.version(defaultVersion);
  }

  public B version(V version) {
    this.version = version;
    this.compositionBuilder.version(version);
    return self();
  }

  public B prescriptionId(PrescriptionId prescriptionId) {
    this.prescriptionId = prescriptionId;
    return self();
  }

  public B patient(KbvPatient patient) {
    this.patient = patient;
    return self();
  }

  public B insurance(KbvCoverage coverage) {
    this.coverage = coverage;
    return self();
  }

  public B practitioner(KbvPractitioner practitioner) {
    this.practitioner = practitioner;
    return self();
  }

  public B attester(KbvPractitioner attester) {
    this.attester = attester;
    return self();
  }

  public B medicalOrganization(MedicalOrganization organization) {
    this.medicalOrganization = organization;
    return self();
  }

  public B statusKennzeichen(String code) {
    return statusKennzeichen(StatusKennzeichen.fromCode(code));
  }

  public B statusKennzeichen(StatusKennzeichen statusKennzeichen) {
    this.statusKennzeichen = statusKennzeichen;
    return self();
  }
}
