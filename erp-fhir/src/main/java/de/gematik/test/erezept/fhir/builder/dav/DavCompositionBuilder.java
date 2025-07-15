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

package de.gematik.test.erezept.fhir.builder.dav;

import de.gematik.bbriccs.fhir.builder.ResourceBuilder;
import de.gematik.test.erezept.fhir.profiles.definitions.AbdaErpPkvStructDef;
import de.gematik.test.erezept.fhir.profiles.systems.AbdaCodeSystem;
import de.gematik.test.erezept.fhir.profiles.version.AbdaErpPkvVersion;
import java.util.Date;
import java.util.List;
import lombok.val;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.Reference;

public class DavCompositionBuilder extends ResourceBuilder<Composition, DavCompositionBuilder> {

  private AbdaErpPkvVersion abdaErpPkvVersion = AbdaErpPkvVersion.getDefaultVersion();
  private static final String TITLE = "ERezeptAbgabedaten";

  private Composition.CompositionStatus status = Composition.CompositionStatus.FINAL;
  private Reference pharmacyReference;
  private Reference medicationReference;

  private DavCompositionBuilder() {}

  protected static DavCompositionBuilder builder() {
    return new DavCompositionBuilder();
  }

  /**
   * <b>Attention:</b> use with care as this setter might break automatic choice of the version.
   * This builder will set the default version automatically, so there should be no need to provide
   * an explicit version
   *
   * @param version to use for generation of this resource
   * @return Builder
   */
  public DavCompositionBuilder version(AbdaErpPkvVersion version) {
    this.abdaErpPkvVersion = version;
    return this;
  }

  protected DavCompositionBuilder status(String statusCode) {
    return status(Composition.CompositionStatus.fromCode(statusCode));
  }

  protected DavCompositionBuilder status(Composition.CompositionStatus status) {
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

  @Override
  public Composition build() {
    val composition =
        this.createResource(
            Composition::new, AbdaErpPkvStructDef.PKV_ABGABEDATEN_COMPOSITION, abdaErpPkvVersion);

    composition.setDate(new Date());
    composition.setType(AbdaCodeSystem.COMPOSITION_TYPES.asCodeableConcept(TITLE));
    composition.setTitle(TITLE);
    composition.setStatus(status);

    composition.setAuthor(List.of(this.pharmacyReference));
    composition.addSection().addEntry(this.pharmacyReference).setTitle("Apotheke");
    composition.addSection().addEntry(this.medicationReference).setTitle("Abgabeinformationen");

    return composition;
  }
}
