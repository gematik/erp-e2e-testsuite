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

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.fhir.builder.ResourceBuilder;
import de.gematik.test.erezept.fhir.parser.profiles.ICodeSystem;
import de.gematik.test.erezept.fhir.parser.profiles.systems.KbvCodeSystem;
import de.gematik.test.erezept.fhir.parser.profiles.systems.KbvNamingSystem;
import de.gematik.test.erezept.fhir.parser.profiles.version.ProfileVersion;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import lombok.val;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.Composition.CompositionAttestationMode;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;

public abstract class KbvBaseCompositionBuilder<
        V extends ProfileVersion<V>, B extends ResourceBuilder<Composition, B>>
    extends ResourceBuilder<Composition, B> {

  /**
   * KBV Prüfnummer e.g. <a
   * href="https://update.kbv.de/ita-update/Service-Informationen/Zulassungsverzeichnisse/KBV_ITA_SIEX_Verzeichnis_AVWG_eRezept.pdf">KBV_ITA_SIEX_Verzeichnis_AVWG_eRezept.pdf</a>
   */
  @SuppressWarnings({"java:S6418"}) // this is not an AUTH token
  private static final String DEVICE_AUTHOR_ID = "GEMATIK/410/2109/36/123";

  private static final String BASE_URL = "https://pvs.gematik.de/fhir";
  private static final Composition.CompositionStatus COMPOSITION_STATUS =
      Composition.CompositionStatus.FINAL;

  protected V version;
  private final List<Extension> extensions = new LinkedList<>();
  private final List<Composition.SectionComponent> sections = new LinkedList<>();
  private final List<Composition.CompositionAttesterComponent> attesters = new LinkedList<>();

  private final List<Consumer<Composition>> referenceProviders = new LinkedList<>();

  protected KbvBaseCompositionBuilder() {
    addDeviceAuthor();
  }

  private void addDeviceAuthor() {
    val devRef = new Reference();
    devRef
        .getIdentifier()
        .setSystem(KbvNamingSystem.PRUEFNUMMER.getCanonicalUrl())
        .setValue(DEVICE_AUTHOR_ID);
    devRef.setType("Device");
    this.referenceProviders.add(c -> c.addAuthor(devRef));
  }

  private String calculateReferenceValue(Resource resource) {
    return format("{0}/{1}", resource.getResourceType(), resource.getId());
  }

  private String calculateFullUrl(Resource resource) {
    return calculateFullUrl(calculateReferenceValue(resource));
  }

  private String calculateFullUrl(String urlPath) {
    return format("{0}/{1}", BASE_URL, urlPath);
  }

  public B version(V version) {
    this.version = version;
    return self();
  }

  protected B addExtension(Extension extension) {
    this.extensions.add(extension);
    return self();
  }

  /**
   * This method wraps the given resource in a BundleEntryComponent <b>without creating a section
   * entry</b> within the composition
   *
   * @param resource to be added to the corresponding document bundle
   * @return a BundleEntryComponent which can be added as an entry to the corresponding document
   *     bundle
   */
  public BundleEntryComponent createEntryFor(Resource resource) {
    val fullUrl = calculateFullUrl(resource);
    return new Bundle.BundleEntryComponent().setResource(resource).setFullUrl(fullUrl);
  }

  /**
   * This method wraps the given resource in a BundleEntryComponent and automatically creates a
   * section entry within the composition for a reference in the composition
   *
   * @param sectionCode of the section entry for this resources
   * @param resource to be added to the corresponding document bundle
   * @return a BundleEntryComponent which can be added as an entry to the corresponding document
   *     bundle
   */
  public BundleEntryComponent createEntryFor(String sectionCode, Resource resource) {
    val referenceValue = calculateReferenceValue(resource);

    val section = new Composition.SectionComponent();
    val sectionType = getSectionCodeSystem();
    section.setCode(sectionType.asCodeableConcept(sectionCode));
    section.addEntry(new Reference(referenceValue));
    this.sections.add(section);

    val fullUrl = calculateFullUrl(referenceValue);
    return new Bundle.BundleEntryComponent().setResource(resource).setFullUrl(fullUrl);
  }

  public BundleEntryComponent createAttesterEntry(Resource resource) {
    return createAttesterEntry(resource, CompositionAttestationMode.LEGAL);
  }

  public BundleEntryComponent createAttesterEntry(
      Resource resource, CompositionAttestationMode attesterMode) {
    this.referenceProviders.add(
        c -> {
          val attesterComponent = c.addAttester();
          val referenceValue = calculateReferenceValue(resource);
          attesterComponent.setParty(new Reference(referenceValue));
          attesterComponent.setMode(attesterMode);
        });

    val fullUrl = calculateFullUrl(resource);
    return new Bundle.BundleEntryComponent().setResource(resource).setFullUrl(fullUrl);
  }

  public BundleEntryComponent createEntryFor(
      Function<Composition, Reference> refSectionProvider, Resource resource) {
    return createEntryFor(refSectionProvider, resource, false);
  }

  public BundleEntryComponent createEntryFor(
      Function<Composition, Reference> refSectionProvider, Resource resource, boolean withType) {
    val referenceValue = calculateReferenceValue(resource);
    this.referenceProviders.add(
        c -> {
          val reference = refSectionProvider.apply(c).setReference(referenceValue);
          if (withType) reference.setType(resource.getResourceType().name());
        });

    val fullUrl = calculateFullUrl(resource);
    return new Bundle.BundleEntryComponent().setResource(resource).setFullUrl(fullUrl);
  }

  public BundleEntryComponent buildBundleEntryComponent() {
    val composition = build();
    val fullUrl = calculateFullUrl(composition);
    return new Bundle.BundleEntryComponent().setResource(composition).setFullUrl(fullUrl);
  }

  public Composition build() {
    val composition = new Composition();

    val meta = new Meta().setProfile(List.of(this.getProfile()));

    // set FHIR-specific values provided by HAPI
    composition.setId(this.getResourceId()).setMeta(meta);
    composition.setDate(new Date());

    composition
        .setStatus(COMPOSITION_STATUS)
        .setTitle(this.getTitle())
        .setSection(sections)
        .setExtension(extensions);
    composition.setAttester(attesters);
    // Composition-Type is required: let's use a default one for now
    composition.setType(KbvCodeSystem.FORMULAR_ART.asCodeableConcept(this.getFormularArtCode()));

    this.referenceProviders.forEach(rp -> rp.accept(composition));

    return composition;
  }

  protected abstract CanonicalType getProfile();

  protected abstract String getFormularArtCode();

  protected abstract String getTitle();

  protected abstract ICodeSystem getSectionCodeSystem();
}
