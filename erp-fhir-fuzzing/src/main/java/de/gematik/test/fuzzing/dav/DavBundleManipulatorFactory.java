/*
 * Copyright (c) 2023 gematik GmbH
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

package de.gematik.test.fuzzing.dav;

import de.gematik.test.erezept.fhir.parser.profiles.definitions.AbdaErpBasisStructDef;
import de.gematik.test.erezept.fhir.parser.profiles.systems.ErpWorkflowNamingSystem;
import de.gematik.test.erezept.fhir.resources.dav.DavAbgabedatenBundle;
import de.gematik.test.erezept.fhir.valuesets.dav.KostenVersicherterKategorie;
import de.gematik.test.fuzzing.core.FuzzingMutator;
import de.gematik.test.fuzzing.core.NamedEnvelope;
import java.util.LinkedList;
import java.util.List;
import lombok.val;
import org.hl7.fhir.r4.model.Invoice.InvoiceStatus;
import org.hl7.fhir.r4.model.StringType;

public class DavBundleManipulatorFactory {

  private DavBundleManipulatorFactory() {
    throw new AssertionError("Do not instantiate");
  }

  public static List<NamedEnvelope<FuzzingMutator<DavAbgabedatenBundle>>>
      getAllDavBundleManipulators() {
    val manipulators = new LinkedList<>(getDavBundleManipulators());
    manipulators.addAll(getInvoiceManipulators());
    return manipulators;
  }

  public static List<NamedEnvelope<FuzzingMutator<DavAbgabedatenBundle>>>
      getDavBundleManipulators() {
    val manipulators = new LinkedList<NamedEnvelope<FuzzingMutator<DavAbgabedatenBundle>>>();

    manipulators.add(
        NamedEnvelope.of(
            "Abgabedaten mit altem Prescription Identifier",
            b ->
                b.getIdentifier()
                    .setSystem(ErpWorkflowNamingSystem.PRESCRIPTION_ID.getCanonicalUrl())));

    return manipulators;
  }

  public static List<NamedEnvelope<FuzzingMutator<DavAbgabedatenBundle>>> getInvoiceManipulators() {
    val manipulators = new LinkedList<NamedEnvelope<FuzzingMutator<DavAbgabedatenBundle>>>();

    manipulators.add(
        NamedEnvelope.of(
            "Invoice Status CANCELLED", b -> b.getInvoice().setStatus(InvoiceStatus.CANCELLED)));

    manipulators.add(
        NamedEnvelope.of(
            "Invoice Status DRAFT", b -> b.getInvoice().setStatus(InvoiceStatus.DRAFT)));

    manipulators.add(
        NamedEnvelope.of("Invoice Status NULL", b -> b.getInvoice().setStatus(InvoiceStatus.NULL)));

    manipulators.add(
        NamedEnvelope.of(
            "Invoice Status BALANCED", b -> b.getInvoice().setStatus(InvoiceStatus.BALANCED)));

    manipulators.add(
        NamedEnvelope.of(
            "Invoice Status ENTEREDINERROR",
            b -> b.getInvoice().setStatus(InvoiceStatus.ENTEREDINERROR)));

    manipulators.add(
        NamedEnvelope.of(
            "Invoice mit altem Prescription Identifier",
            b ->
                b.getInvoice()
                    .getIdentifierFirstRep()
                    .setSystem(ErpWorkflowNamingSystem.PRESCRIPTION_ID.getCanonicalUrl())));

    manipulators.add(
        NamedEnvelope.of(
            "Invoice mit KostenVersicherterKategorie Mehrkosten",
            b ->
                b.getInvoice()
                    .getPriceComponents()
                    .forEach(
                        pc ->
                            pc.getExtension().stream()
                                .filter(
                                    ext ->
                                        ext.getUrl()
                                            .equals(
                                                AbdaErpBasisStructDef.KOSTEN_VERSICHERTER
                                                    .getCanonicalUrl()))
                                .map(ext -> ext.getExtensionByUrl("Kategorie"))
                                .forEach(
                                    ext ->
                                        ext.setValue(
                                            KostenVersicherterKategorie.MEHRKOSTEN
                                                .asCodeableConcept())))));

    manipulators.add(
        NamedEnvelope.of(
            "Invoice mit KostenVersicherterKategorie Eigenbeteiligung",
            b ->
                b.getInvoice()
                    .getPriceComponents()
                    .forEach(
                        pc ->
                            pc.getExtension().stream()
                                .filter(
                                    ext ->
                                        ext.getUrl()
                                            .equals(
                                                AbdaErpBasisStructDef.KOSTEN_VERSICHERTER
                                                    .getCanonicalUrl()))
                                .map(ext -> ext.getExtensionByUrl("Kategorie"))
                                .forEach(
                                    ext ->
                                        ext.setValue(
                                            KostenVersicherterKategorie.EIGENBETEILIGUNG
                                                .asCodeableConcept())))));

    manipulators.add(
        NamedEnvelope.of(
            "MwSt leer",
            b ->
                b
                    .getInvoice()
                    .getLineItemFirstRep()
                    .getPriceComponentFirstRep()
                    .getExtension()
                    .stream()
                    .filter(
                        ext ->
                            ext.getUrl().equals(AbdaErpBasisStructDef.MWST_SATZ.getCanonicalUrl()))
                    .forEach(ext -> ext.setValue(new StringType("")))));

    manipulators.add(
        NamedEnvelope.of(
            "MwSt = 100.0%",
            b ->
                b
                    .getInvoice()
                    .getLineItemFirstRep()
                    .getPriceComponentFirstRep()
                    .getExtension()
                    .stream()
                    .filter(
                        ext ->
                            ext.getUrl().equals(AbdaErpBasisStructDef.MWST_SATZ.getCanonicalUrl()))
                    .forEach(ext -> ext.setValue(new StringType("100.0")))));

    manipulators.add(
        NamedEnvelope.of(
            "MwSt = 19.123%",
            b ->
                b
                    .getInvoice()
                    .getLineItemFirstRep()
                    .getPriceComponentFirstRep()
                    .getExtension()
                    .stream()
                    .filter(
                        ext ->
                            ext.getUrl().equals(AbdaErpBasisStructDef.MWST_SATZ.getCanonicalUrl()))
                    .forEach(ext -> ext.setValue(new StringType("19.123")))));

    manipulators.add(
        NamedEnvelope.of(
            "MwSt = 19,00%",
            b ->
                b
                    .getInvoice()
                    .getLineItemFirstRep()
                    .getPriceComponentFirstRep()
                    .getExtension()
                    .stream()
                    .filter(
                        ext ->
                            ext.getUrl().equals(AbdaErpBasisStructDef.MWST_SATZ.getCanonicalUrl()))
                    .forEach(ext -> ext.setValue(new StringType("19,00")))));

    manipulators.add(
        NamedEnvelope.of(
            "MwSt = 19.0%",
            b ->
                b
                    .getInvoice()
                    .getLineItemFirstRep()
                    .getPriceComponentFirstRep()
                    .getExtension()
                    .stream()
                    .filter(
                        ext ->
                            ext.getUrl().equals(AbdaErpBasisStructDef.MWST_SATZ.getCanonicalUrl()))
                    .forEach(ext -> ext.setValue(new StringType("19.0")))));

    return manipulators;
  }
}
