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

package de.gematik.test.erezept.fhir.builder.dav;

import de.gematik.test.erezept.fhir.builder.AbstractResourceBuilder;
import de.gematik.test.erezept.fhir.parser.profiles.ErpCodeSystem;
import de.gematik.test.erezept.fhir.parser.profiles.ErpStructureDefinition;
import java.util.Date;
import java.util.List;
import lombok.NonNull;
import lombok.val;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Reference;

public class DavCompositionBuilder extends AbstractResourceBuilder<DavCompositionBuilder> {

  private static final String TITLE = "ERezeptAbgabedaten";

  private Composition.CompositionStatus status = Composition.CompositionStatus.FINAL;
  private Reference pharmacyReference;
  private Reference medicationReference;

  private DavCompositionBuilder() {}

  protected static DavCompositionBuilder builder() {
    return new DavCompositionBuilder();
  }

  protected DavCompositionBuilder status(@NonNull String statusCode) {
    return status(Composition.CompositionStatus.fromCode(statusCode));
  }

  protected DavCompositionBuilder status(@NonNull Composition.CompositionStatus status) {
    this.status = status;
    return self();
  }

  protected DavCompositionBuilder pharmacy(String id) {
    return pharmacy(new Reference(id));
  }

  protected DavCompositionBuilder pharmacy(Reference pharmacy) {
    this.pharmacyReference = pharmacy;
    return self();
  }

  protected DavCompositionBuilder medication(String id) {
    return medication(new Reference(id));
  }

  protected DavCompositionBuilder medication(Reference medication) {
    this.medicationReference = medication;
    return self();
  }

  protected Composition build() {
    val composition = new Composition();

    val profile = ErpStructureDefinition.DAV_PKV_PR_ERP_ABGABEDATEN_COMPOSITION.asCanonicalType();
    val meta = new Meta().setProfile(List.of(profile));

    // set FHIR-specific values provided by HAPI
    composition.setId(this.getResourceId()).setMeta(meta);
    composition.setDate(new Date());
    composition.setType(ErpCodeSystem.DAV_CS_ERP_COMPOSITION_TYPES.asCodeableConcept(TITLE));
    composition.setTitle(TITLE);
    composition.setStatus(status);

    composition.setAuthor(List.of(this.pharmacyReference));
    composition.addSection().addEntry(this.pharmacyReference).setTitle("Apotheke");
    composition.addSection().addEntry(this.medicationReference).setTitle("Abgabeinformationen");

    return composition;
  }
}
