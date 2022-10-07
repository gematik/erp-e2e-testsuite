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

package de.gematik.test.erezept.fhir.builder.kbv;

import de.gematik.test.erezept.fhir.builder.AbstractResourceBuilder;
import de.gematik.test.erezept.fhir.builder.BuilderUtil;
import de.gematik.test.erezept.fhir.parser.profiles.ErpCodeSystem;
import de.gematik.test.erezept.fhir.parser.profiles.ErpNamingSystem;
import de.gematik.test.erezept.fhir.parser.profiles.ErpStructureDefinition;
import de.gematik.test.erezept.fhir.references.kbv.*;
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

  private Composition.CompositionStatus status = Composition.CompositionStatus.FINAL;
  private String title = "elektronische Arzneimittelverordnung";

  private Reference subjectReference;
  private Reference custodianReference;

  private final List<Extension> extensions = new LinkedList<>();
  private final List<Reference> authors = new LinkedList<>();
  private final List<Composition.SectionComponent> sections = new LinkedList<>();

  /**
   * CompositionBuilder shall be used within this package only e.g. by {@link KbvErpBundleBuilder}
   */
  protected KbvCompositionBuilder() {}

  protected static KbvCompositionBuilder builder() {
    return new KbvCompositionBuilder();
  }

  protected KbvCompositionBuilder addExtension(@NonNull Extension extension) {
    this.extensions.add(extension);
    return self();
  }

  protected KbvCompositionBuilder subjectReference(@NonNull SubjectReference subjectReference) {
    this.subjectReference = subjectReference;
    return self();
  }

  protected KbvCompositionBuilder coverageReference(@NonNull CoverageReference coverageReference) {
    val s = new Composition.SectionComponent();
    s.setCode(BuilderUtil.kbvSectionType("Coverage"));
    s.addEntry(coverageReference);

    sections.add(s);
    return self();
  }

  protected KbvCompositionBuilder custodianReference(
      @NonNull OrganizationReference custodianReference) {
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
        .setSystem(ErpNamingSystem.KBV_PRUEFNUMMER.getCanonicalUrl())
        .setValue(checkNumber);
    devRef.setType("Device");
    return this.addAuthor(devRef);
  }

  protected KbvCompositionBuilder requesterReference(
      @NonNull RequesterReference requesterReference) {
    // need to copy to set the type of the reference on the copy but not on the original object
    val authorPractitioner = requesterReference.copy();
    authorPractitioner.setType(RequesterReference.REQUESTER_PREFIX);
    return this.addAuthor(authorPractitioner);
  }

  protected KbvCompositionBuilder medicationRequestReference(
      @NonNull MedicationRequestReference medicationReference) {
    val s = new Composition.SectionComponent();
    s.setCode(BuilderUtil.kbvSectionType("Prescription"));
    s.addEntry(medicationReference);

    sections.add(s);
    return self();
  }

  protected KbvCompositionBuilder title(@NonNull String title) {
    this.title = title;
    return self();
  }

  private KbvCompositionBuilder addAuthor(@NonNull Reference authorReference) {
    this.authors.add(authorReference);
    return self();
  }

  protected Composition build() {
    val composition = new Composition();

    val profile = ErpStructureDefinition.KBV_COMPOSITION.asCanonicalType();
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

    // Composition-Type is required: let's use a default one for now
    composition.setType(ErpCodeSystem.FORMULAR_ART.asCodeableConcept("e16A"));

    return composition;
  }
}
