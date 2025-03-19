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

package de.gematik.test.erezept.fhir.builder.kbv;

import de.gematik.test.erezept.fhir.parser.profiles.definitions.KbvItvEvdgaStructDef;
import de.gematik.test.erezept.fhir.parser.profiles.version.KbvItvEvdgaVersion;
import de.gematik.test.erezept.fhir.r4.kbv.KbvEvdgaBundle;
import de.gematik.test.erezept.fhir.r4.kbv.KbvHealthAppRequest;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import java.util.Date;
import java.util.Optional;
import lombok.val;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Composition;

public class KbvEvdgaBundleBuilder
    extends KbvBaseDocumentBundleBuilder<
        KbvItvEvdgaVersion, KbvEvdgaBundle, KbvEvdgaBundleBuilder> {

  private KbvHealthAppRequest healthAppRequest;

  private KbvEvdgaBundleBuilder() {
    super(KbvEvdgaCompositionBuilder.builder(), KbvItvEvdgaVersion.getDefaultVersion());
  }

  public static KbvEvdgaBundleBuilder forPrescription(PrescriptionId prescriptionId) {
    return new KbvEvdgaBundleBuilder().prescriptionId(prescriptionId);
  }

  public KbvEvdgaBundleBuilder healthAppRequest(KbvHealthAppRequest healthAppRequest) {
    this.healthAppRequest = healthAppRequest;
    return this;
  }

  @Override
  public KbvEvdgaBundle build() {
    val bundle = this.createResource(KbvEvdgaBundle::new, KbvItvEvdgaStructDef.BUNDLE, version);

    bundle.setType(Bundle.BundleType.DOCUMENT);
    bundle.setTimestamp(new Date());
    bundle.setIdentifier(prescriptionId.asIdentifier());

    bundle.addEntry(compositionBuilder.createEntryFor("Prescription", healthAppRequest));
    bundle.addEntry(compositionBuilder.createEntryFor(Composition::getSubject, patient));
    bundle.addEntry(compositionBuilder.createEntryFor(Composition::addAuthor, practitioner, true));
    Optional.ofNullable(attester)
        .ifPresent(a -> bundle.addEntry(compositionBuilder.createAttesterEntry(a)));
    bundle.addEntry(
        compositionBuilder.createEntryFor(Composition::getCustodian, medicalOrganization));
    bundle.addEntry(compositionBuilder.createEntryFor("HealthInsurance", coverage));

    // now build the composition
    compositionBuilder.addExtension(statusKennzeichen.asExtension());
    val compositionEntry = compositionBuilder.buildBundleEntryComponent();
    bundle.getEntry().add(0, compositionEntry);
    return bundle;
  }
}
