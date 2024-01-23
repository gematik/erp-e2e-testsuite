/*
 * Copyright 2023 gematik GmbH
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

import de.gematik.test.erezept.fhir.builder.AbstractResourceBuilder;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.KbvItaErpStructDef;
import de.gematik.test.erezept.fhir.parser.profiles.systems.KbvCodeSystem;
import de.gematik.test.erezept.fhir.parser.profiles.systems.KbvNamingSystem;
import de.gematik.test.erezept.fhir.parser.profiles.version.KbvItaErpVersion;
import de.gematik.test.erezept.fhir.references.kbv.RequesterReference;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import lombok.NonNull;
import lombok.val;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Reference;

public class KbvCompositionBuilder extends AbstractResourceBuilder<KbvCompositionBuilder> {

  private KbvItaErpVersion kbvItaErpVersion = KbvItaErpVersion.getDefaultVersion();
  private Composition.CompositionStatus status = Composition.CompositionStatus.FINAL;
  private String title = "elektronische Arzneimittelverordnung";

  private Reference subjectReference;
  private Reference custodianReference;

  private final List<Extension> extensions = new LinkedList<>();
  private final List<Reference> authors = new LinkedList<>();
  private final List<Composition.SectionComponent> sections = new LinkedList<>();
  private final List<Composition.CompositionAttesterComponent> attesters = new LinkedList<>();

  /**
   * CompositionBuilder shall be used within this package only e.g. by {@link KbvErpBundleBuilder}
   */
  protected KbvCompositionBuilder() {}

  protected static KbvCompositionBuilder builder() {
    return new KbvCompositionBuilder();
  }

  /**
   * <b>Attention:</b> use with care as this setter might break automatic choice of the version.
   * This builder will set the default version automatically, so there should be no need to provide
   * an explicit version
   *
   * @param version to use for generation of this resource
   * @return Builder
   */
  public KbvCompositionBuilder version(KbvItaErpVersion version) {
    this.kbvItaErpVersion = version;
    return this;
  }

  protected KbvCompositionBuilder addExtension(@NonNull Extension extension) {
    this.extensions.add(extension);
    return self();
  }

  protected KbvCompositionBuilder subjectReference(@NonNull Reference subjectReference) {
    this.subjectReference = subjectReference;
    return self();
  }

  protected KbvCompositionBuilder coverageReference(@NonNull Reference coverageReference) {
    val s = new Composition.SectionComponent();
    s.setCode(KbvCodeSystem.SECTION_TYPE.asCodeableConcept("Coverage"));
    s.addEntry(coverageReference);

    sections.add(s);
    return self();
  }

  protected KbvCompositionBuilder custodianReference(Reference custodianReference) {
    this.custodianReference = custodianReference;
    return self();
  }

  protected KbvCompositionBuilder status(@NonNull String statusCode) {
    return status(Composition.CompositionStatus.fromCode(statusCode));
  }

  protected KbvCompositionBuilder status(@NonNull Composition.CompositionStatus status) {
    this.status = status;
    return self();
  }

  protected KbvCompositionBuilder addDeviceAuthor(@NonNull String checkNumber) {
    val devRef = new Reference();
    devRef
        .getIdentifier()
        .setSystem(KbvNamingSystem.PRUEFNUMMER.getCanonicalUrl())
        .setValue(checkNumber);
    devRef.setType("Device");
    return this.addAuthor(devRef);
  }

  protected KbvCompositionBuilder requesterReference(Reference requesterReference) {
    val authorPractitioner = requesterReference;
    authorPractitioner.setType(RequesterReference.REQUESTER_PREFIX);
    return this.addAuthor(authorPractitioner);
  }

  protected KbvCompositionBuilder attesterReference(Reference attesterReference) {
    val attesterComponent = new Composition.CompositionAttesterComponent();
    attesterComponent.setParty(attesterReference);
    attesterComponent.setMode(Composition.CompositionAttestationMode.LEGAL);
    attesters.add(attesterComponent);
    return self();
  }

  protected KbvCompositionBuilder medicationRequestReference(Reference medicationReference) {
    val s = new Composition.SectionComponent();
    s.setCode(KbvCodeSystem.SECTION_TYPE.asCodeableConcept("Prescription"));
    s.addEntry(medicationReference);

    sections.add(s);
    return self();
  }

  protected KbvCompositionBuilder supplyRequestReference(Reference supplyRequestReference) {
    val s = new Composition.SectionComponent();
    s.setCode(KbvCodeSystem.SECTION_TYPE.asCodeableConcept("PracticeSupply"));
    s.addEntry(supplyRequestReference);
    sections.add(s);
    return self();
  }

  protected KbvCompositionBuilder title(String title) {
    this.title = title;
    return self();
  }

  private KbvCompositionBuilder addAuthor(@NonNull Reference authorReference) {
    this.authors.add(authorReference);
    return self();
  }

  protected Composition build() {
    val composition = new Composition();

    val profile = KbvItaErpStructDef.COMPOSITION.asCanonicalType(kbvItaErpVersion);
    val meta = new Meta().setProfile(List.of(profile));

    // set FHIR-specific values provided by HAPI
    composition.setId(this.getResourceId()).setMeta(meta);
    composition.setDate(new Date());

    composition
        .setStatus(status)
        .setTitle(title)
        .setSubject(subjectReference)
        .setAuthor(authors)
        .setCustodian(custodianReference)
        .setSection(sections)
        .setExtension(extensions);
    composition.setAttester(attesters);
    // Composition-Type is required: let's use a default one for now
    composition.setType(KbvCodeSystem.FORMULAR_ART.asCodeableConcept("e16A"));

    return composition;
  }
}
