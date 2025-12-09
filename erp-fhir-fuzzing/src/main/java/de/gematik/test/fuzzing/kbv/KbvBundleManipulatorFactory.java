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

package de.gematik.test.fuzzing.kbv;

import static de.gematik.bbriccs.fhir.de.DeBasisProfilNamingSystem.*;
import static de.gematik.test.erezept.fhir.profiles.definitions.KbvItaErpStructDef.*;
import static de.gematik.test.erezept.fhir.profiles.definitions.KbvItaForStructDef.*;
import static java.text.MessageFormat.format;

import de.gematik.bbriccs.fhir.coding.WithSystem;
import de.gematik.bbriccs.fhir.coding.exceptions.MissingFieldException;
import de.gematik.bbriccs.fhir.de.DeBasisProfilNamingSystem;
import de.gematik.bbriccs.fhir.de.DeBasisProfilStructDef;
import de.gematik.bbriccs.fhir.de.HL7StructDef;
import de.gematik.bbriccs.fhir.de.valueset.IdentifierTypeDe;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.builder.kbv.KbvPractitionerFaker;
import de.gematik.test.erezept.fhir.profiles.definitions.KbvBasisStructDef;
import de.gematik.test.erezept.fhir.profiles.definitions.KbvItaErpStructDef;
import de.gematik.test.erezept.fhir.profiles.definitions.KbvItaForStructDef;
import de.gematik.test.erezept.fhir.profiles.systems.CommonNamingSystem;
import de.gematik.test.erezept.fhir.profiles.systems.KbvCodeSystem;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpBundle;
import de.gematik.test.erezept.fhir.r4.kbv.KbvMedicalOrganization;
import de.gematik.test.erezept.fhir.r4.kbv.KbvPractitioner;
import de.gematik.test.erezept.fhir.values.BaseANR;
import de.gematik.test.erezept.fhir.values.LANR;
import de.gematik.test.erezept.fhir.values.ZANR;
import de.gematik.test.erezept.fhir.valuesets.Darreichungsform;
import de.gematik.test.erezept.fhir.valuesets.MedicationType;
import de.gematik.test.fuzzing.FuzzingUtils;
import de.gematik.test.fuzzing.core.FuzzingMutator;
import de.gematik.test.fuzzing.core.NamedEnvelope;
import java.sql.Date;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import lombok.val;
import org.hl7.fhir.r4.model.*;

public class KbvBundleManipulatorFactory {

  public static final String URN_UUID = "urn:uuid";
  public static final String URN_OID = "urn:oid";

  private KbvBundleManipulatorFactory() {
    throw new AssertionError("Do not instantiate");
  }

  public static List<NamedEnvelope<FuzzingMutator<KbvErpBundle>>> getAllKbvBundleManipulators() {
    return getAllKbvBundleManipulators(false);
  }

  /**
   * Provides all available manipulators on the KBV-Bundle which includes different manipulators for
   * entries of the KBV-Bundle
   *
   * @return list of NamedParameters containing manipulators
   */
  public static List<NamedEnvelope<FuzzingMutator<KbvErpBundle>>> getAllKbvBundleManipulators(
      boolean includeMvo) {
    val manipulators = new LinkedList<>(getCoverageManipulators());
    manipulators.addAll(getMedicationManipulators());
    manipulators.addAll(getMedicationRequestManipulators());
    manipulators.addAll(getOrganizationManipulators());
    manipulators.addAll(getPatientManipulators());
    manipulators.addAll(getPractitionerManipulators());
    manipulators.addAll(getCompositionManipulators());
    manipulators.addAll(getSystemsManipulators());
    manipulators.addAll(getReferenceToOidReferenceManipulators());
    manipulators.addAll(getResourceIdReduceManipulators());
    manipulators.addAll(getResourceIdAndFullUrlDiffManipulators());
    manipulators.addAll(getCompositionReferencedManipulators());

    if (includeMvo) {
      manipulators.addAll(MvoExtensionManipulatorFactory.getMvoExtensionKennzeichenFalsifier());
    }

    return manipulators;
  }

  public static List<NamedEnvelope<FuzzingMutator<KbvErpBundle>>>
      getReferenceToOidReferenceManipulators() {
    val manipulators = new LinkedList<NamedEnvelope<FuzzingMutator<KbvErpBundle>>>();

    manipulators.add(
        NamedEnvelope.of(
            "der Bundle.entry.Medication.FullUrl eine OID",
            b -> {
              val oid = generateOID();
              val entry =
                  b.getEntry().stream().filter(MEDICATION_PZN::matches).findFirst().orElseThrow();
              entry.setFullUrl(URN_OID + ":" + oid);
              b.getMedication().setId(oid);
              b.getMedicationRequest().getMedicationReference().setReference(URN_OID + ":" + oid);
            }));

    manipulators.add(
        NamedEnvelope.of(
            "der Bundle.entry.Practitioner.FullUrl eine OID",
            b -> {
              val oid = URN_OID + ":" + generateOID();
              b.getComposition().getAuthor().stream()
                  .filter(a -> a.getType().matches("Practitioner"))
                  .map(a -> a.setReference(oid))
                  .findFirst()
                  .orElseThrow();
              val entry =
                  b.getEntry().stream().filter(PRACTITIONER::matches).findFirst().orElseThrow();
              entry.setFullUrl(oid);
              b.getMedicationRequest().getRequester().setReference(oid);
            }));

    manipulators.add(
        NamedEnvelope.of(
            "der Bundle.entry.organization.FullUrl eine OID",
            b -> {
              val oid = URN_OID + ":" + generateOID();
              b.getComposition().getCustodian().setReference(oid);
              val entry =
                  b.getEntry().stream().filter(ORGANIZATION::matches).findFirst().orElseThrow();
              entry.setFullUrl(oid);
            }));

    manipulators.add(
        NamedEnvelope.of(
            "der Bundle.entry.Patient.FullUrl eine OID",
            b -> {
              val oid = URN_OID + ":" + generateOID();
              b.getComposition().getSubject().setReference(oid);
              b.getCoverage().getBeneficiary().setReference(oid);
              val entry = b.getEntry().stream().filter(PATIENT::matches).findFirst().orElseThrow();
              entry.setFullUrl(oid);
            }));

    return manipulators;
  }

  private static String generateOID() {
    String prefix = "1.3.6.1.4.1";
    String uniquePart = UUID.randomUUID().toString().replace("-", "");
    int hash = uniquePart.hashCode();
    long number = hash < 0 ? hash + (1L + Integer.MAX_VALUE) : hash;
    return prefix + "." + number;
  }

  public static List<NamedEnvelope<FuzzingMutator<KbvErpBundle>>>
      getCompositionReferencedManipulators() {
    val manipulators = new LinkedList<NamedEnvelope<FuzzingMutator<KbvErpBundle>>>();

    manipulators.add(
        NamedEnvelope.of(
            "der Bundle.entry.Component.Practitioner.FullUrl eine falsche ID",
            b ->
                b.getComposition().getAuthor().stream()
                    .filter(a -> a.getType().matches("Practitioner"))
                    .map(a -> a.setReference(URN_UUID + ":" + UUID.randomUUID()))
                    .findFirst()
                    .orElseThrow()));

    manipulators.add(
        NamedEnvelope.of(
            "der Bundle.entry.composition.custodian eine falsche ID",
            b ->
                b.getComposition()
                    .getCustodian()
                    .setReference(URN_UUID + ":" + UUID.randomUUID())));
    manipulators.add(
        NamedEnvelope.of(
            "der Bundle.entry.Patient.FullUrl eine falsche ID",
            b -> b.getComposition().getSubject().setReference(URN_UUID + ":" + UUID.randomUUID())));

    return manipulators;
  }

  public static List<NamedEnvelope<FuzzingMutator<KbvErpBundle>>>
      getResourceIdReduceManipulators() {
    val manipulators = new LinkedList<NamedEnvelope<FuzzingMutator<KbvErpBundle>>>();

    manipulators.add(
        NamedEnvelope.of(
            "löscht in der Medication Ressource die ID",
            b ->
                b.getEntry().stream()
                    .filter(MEDICATION_PZN::matches)
                    .map(res -> res.getResource().setId(""))
                    .findFirst()
                    .orElseThrow()));
    manipulators.add(
        NamedEnvelope.of(
            "löscht in der MedicationRequest Ressource die ID",
            b ->
                b.getEntry().stream()
                    .filter(PRESCRIPTION::matches)
                    .map(res -> res.getResource().setId(""))
                    .findFirst()
                    .orElseThrow()));
    manipulators.add(
        NamedEnvelope.of(
            "löscht in der Practitioner Ressource die ID",
            b ->
                b.getEntry().stream()
                    .filter(PRACTITIONER::matches)
                    .map(res -> res.getResource().setId(""))
                    .findFirst()
                    .orElseThrow()));
    manipulators.add(
        NamedEnvelope.of(
            "löscht in der Organization Ressource die ID",
            b ->
                b.getEntry().stream()
                    .filter(ORGANIZATION::matches)
                    .map(res -> res.getResource().setId(""))
                    .findFirst()
                    .orElseThrow()));

    return manipulators;
  }

  public static List<NamedEnvelope<FuzzingMutator<KbvErpBundle>>>
      getResourceIdAndFullUrlDiffManipulators() {
    val manipulators = new LinkedList<NamedEnvelope<FuzzingMutator<KbvErpBundle>>>();

    manipulators.add(
        NamedEnvelope.of(
            "in der Medication Ressource die ID und FullUrl unterschiedlich",
            b ->
                b.getEntry().stream()
                    .filter(MEDICATION_PZN::matches)
                    .map(res -> res.getResource().setId(UUID.randomUUID().toString()))
                    .findFirst()
                    .orElseThrow()));
    manipulators.add(
        NamedEnvelope.of(
            "in der Organization Ressource die ID und FullUrl unterschiedlich",
            b ->
                b.getEntry().stream()
                    .filter(ORGANIZATION::matches)
                    .map(res -> res.getResource().setId(UUID.randomUUID().toString()))
                    .findFirst()
                    .orElseThrow()));
    manipulators.add(
        NamedEnvelope.of(
            "in der Practitioner Ressource die ID und FullUrl unterschiedlich",
            b ->
                b.getEntry().stream()
                    .filter(PRACTITIONER::matches)
                    .map(res -> res.getResource().setId(UUID.randomUUID().toString()))
                    .findFirst()
                    .orElseThrow()));
    manipulators.add(
        NamedEnvelope.of(
            "in der Prescription Ressource die ID und FullUrl unterschiedlich",
            b ->
                b.getEntry().stream()
                    .filter(PRESCRIPTION::matches)
                    .map(res -> res.getResource().setId(UUID.randomUUID().toString()))
                    .findFirst()
                    .orElseThrow()));
    return manipulators;
  }

  public static List<NamedEnvelope<FuzzingMutator<KbvErpBundle>>> getCoverageManipulators() {
    val manipulators = new LinkedList<NamedEnvelope<FuzzingMutator<KbvErpBundle>>>();

    manipulators.add(
        NamedEnvelope.of(
            "DMP Code 59",
            b ->
                changeExtensionCode(
                    b.getCoverage(), DeBasisProfilStructDef.GKV_DMP_KENNZEICHEN, "59")));

    manipulators.add(
        NamedEnvelope.of(
            "DMP Code 15",
            b ->
                changeExtensionCode(
                    b.getCoverage(), DeBasisProfilStructDef.GKV_DMP_KENNZEICHEN, "15")));

    manipulators.add(
        NamedEnvelope.of(
            "DMP Code 99",
            b ->
                changeExtensionCode(
                    b.getCoverage(), DeBasisProfilStructDef.GKV_DMP_KENNZEICHEN, "99")));
    manipulators.add(
        NamedEnvelope.of(
            "DMP Code 29",
            b ->
                changeExtensionCode(
                    b.getCoverage(), DeBasisProfilStructDef.GKV_DMP_KENNZEICHEN, "29")));
    manipulators.add(
        NamedEnvelope.of(
            "DMP Code ' '",
            b ->
                changeExtensionCode(
                    b.getCoverage(), DeBasisProfilStructDef.GKV_DMP_KENNZEICHEN, " ")));

    manipulators.add(
        NamedEnvelope.of(
            "DMP Code ?",
            b ->
                changeExtensionCode(
                    b.getCoverage(), DeBasisProfilStructDef.GKV_DMP_KENNZEICHEN, "?")));

    manipulators.add(
        NamedEnvelope.of(
            "DMP Code 0 (!= 00)",
            b ->
                changeExtensionCode(
                    b.getCoverage(), DeBasisProfilStructDef.GKV_DMP_KENNZEICHEN, "0")));

    manipulators.add(
        NamedEnvelope.of(
            "DMP Code 13",
            b ->
                changeExtensionCode(
                    b.getCoverage(), DeBasisProfilStructDef.GKV_DMP_KENNZEICHEN, "13")));

    manipulators.add(
        NamedEnvelope.of(
            "DMP Code Asthma",
            b ->
                changeExtensionCode(
                    b.getCoverage(), DeBasisProfilStructDef.GKV_DMP_KENNZEICHEN, "asthma")));

    manipulators.add(
        NamedEnvelope.of(
            "Besondere Personengruppen Code -T",
            b ->
                changeExtensionCode(
                    b.getCoverage(), DeBasisProfilStructDef.GKV_PERSON_GROUP, "-T")));

    manipulators.add(
        NamedEnvelope.of(
            "Besondere Personengruppen Code 0 (!= 00)",
            b ->
                changeExtensionCode(
                    b.getCoverage(), DeBasisProfilStructDef.GKV_PERSON_GROUP, "0")));

    manipulators.add(
        NamedEnvelope.of(
            "Besondere Personengruppen Code 10",
            b ->
                changeExtensionCode(
                    b.getCoverage(), DeBasisProfilStructDef.GKV_PERSON_GROUP, "10")));

    manipulators.add(
        NamedEnvelope.of(
            "Coverage Type gkv (lowercase)",
            b -> b.getCoverage().getType().getCodingFirstRep().setCode("gkv")));

    manipulators.add(
        NamedEnvelope.of(
            "Coverage Type Inkasso",
            b -> b.getCoverage().getType().getCodingFirstRep().setCode("Inkasso")));

    manipulators.add(
        NamedEnvelope.of(
            "GKV Versichertenart P",
            b ->
                changeExtensionCode(
                    b.getCoverage(), DeBasisProfilStructDef.GKV_VERSICHERTENART, "P")));

    manipulators.add(
        NamedEnvelope.of(
            "WOP 40",
            b -> changeExtensionCode(b.getCoverage(), DeBasisProfilStructDef.GKV_WOP, "40")));

    manipulators.add(
        NamedEnvelope.of(
            "WOP 1000",
            b -> changeExtensionCode(b.getCoverage(), DeBasisProfilStructDef.GKV_WOP, "1000")));

    manipulators.add(
        NamedEnvelope.of(
            "Invalide IKNR Länge der Versicherung",
            b -> {
              val iknrIdentifier = b.getCoverage().getPayorFirstRep().getIdentifier();
              iknrIdentifier.setValue("0721111100");
            }));

    manipulators.add(
        NamedEnvelope.of(
            "Reguläre StructureDefinition als ModifierExtension in der Versicherung",
            b -> {
              val sd = FuzzingUtils.randomStructureDefinition();
              b.getCoverage()
                  .getModifierExtension()
                  .add(new Extension(sd.getCanonicalUrl(), new StringType("ABC")));
            }));

    manipulators.add(
        NamedEnvelope.of(
            "Invalide ModifierExtension in der Versicherung",
            b -> {
              val url = GemFaker.getFaker().internet().url();
              b.getCoverage()
                  .getModifierExtension()
                  .add(
                      new Extension(
                          url,
                          new IntegerType(
                              GemFaker.getFaker()
                                  .number()
                                  .numberBetween(Integer.MIN_VALUE, Integer.MAX_VALUE))));
            }));

    return manipulators;
  }

  public static List<NamedEnvelope<FuzzingMutator<KbvErpBundle>>> getMedicationManipulators() {
    val manipulators = new LinkedList<NamedEnvelope<FuzzingMutator<KbvErpBundle>>>();

    manipulators.add(
        NamedEnvelope.of(
            "Freitextverordnung mit Mengenangabe",
            b -> {
              val freeTextCoding = new CodeableConcept();
              freeTextCoding
                  .setText(GemFaker.fakerDrugName())
                  .getCodingFirstRep()
                  .setSystem(MedicationType.CODE_SYSTEM.getCanonicalUrl())
                  .setCode("freitext");
              b.getMedication().setCode(freeTextCoding);
            }));

    manipulators.add(
        NamedEnvelope.of(
            "Freitextverordnung mit Typo freiTExt",
            b -> {
              val freeTextCoding = new CodeableConcept();
              freeTextCoding
                  .setText(GemFaker.fakerDrugName())
                  .getCodingFirstRep()
                  .setSystem(MedicationType.CODE_SYSTEM.getCanonicalUrl())
                  .setCode("freiTExt");
              b.getMedication().setCode(freeTextCoding);
              b.getMedication().setAmount(null); // should prevent to run in the amount-check
            }));

    manipulators.add(
        NamedEnvelope.of(
            "Darreichungsform der Medikation als String",
            b -> {
              val freeTextCoding = new CodeableConcept();
              freeTextCoding
                  .setText(GemFaker.fakerDrugName())
                  .getCodingFirstRep()
                  .setSystem(MedicationType.CODE_SYSTEM.getCanonicalUrl())
                  .setCode("freitext");
              b.getMedication().setCode(freeTextCoding);
              b.getMedication().setAmount(null); // should prevent to run in the amount-check
              b.getMedication()
                  .setForm(
                      new CodeableConcept()
                          .addCoding(new Coding().setCode(Darreichungsform.SMT.getCode())));
            }));

    manipulators.add(
        NamedEnvelope.of(
            "Darreichungsform mit Code XYZ",
            b -> b.getMedication().getForm().getCodingFirstRep().setCode("XYZ")));
    manipulators.add(
        NamedEnvelope.of(
            "Darreichungsform mit invalidem random Code",
            b -> {
              String invalidCode;
              boolean isNotUnique;
              do {
                invalidCode = GemFaker.getFaker().regexify(("[A-Z]{3,4}"));
                final String invalidFinalCode = invalidCode;
                isNotUnique =
                    Arrays.stream(Darreichungsform.values())
                        .anyMatch(darr -> darr.getCode().equals(invalidFinalCode));
              } while (isNotUnique);

              b.getMedication().getForm().getCodingFirstRep().setCode(invalidCode);
            }));

    manipulators.add(
        NamedEnvelope.of(
            "Darreichungsform mit invalidem Freitext 'Keine Angabe'",
            b -> b.getMedication().getForm().getCodingFirstRep().setCode("Keine Angabe")));

    manipulators.add(
        NamedEnvelope.of(
            "Normgröße 'Keine Angabe'",
            b ->
                b.getMedication().getExtension().stream()
                    .filter(
                        ext ->
                            ext.getUrl()
                                .equals(DeBasisProfilStructDef.NORMGROESSE.getCanonicalUrl()))
                    .forEach(
                        ext ->
                            ext.getValue().castToCode(ext.getValue()).setValue("Keine Angabe"))));

    manipulators.add(
        NamedEnvelope.of(
            "Normgröße 'ABC'",
            b ->
                b.getMedication().getExtension().stream()
                    .filter(
                        ext ->
                            ext.getUrl()
                                .equals(DeBasisProfilStructDef.NORMGROESSE.getCanonicalUrl()))
                    .forEach(ext -> ext.getValue().castToCode(ext.getValue()).setValue("ABC"))));

    manipulators.add(
        NamedEnvelope.of(
            "Normgröße 'Hello World'",
            b ->
                b.getMedication().getExtension().stream()
                    .filter(
                        ext ->
                            ext.getUrl()
                                .equals(DeBasisProfilStructDef.NORMGROESSE.getCanonicalUrl()))
                    .forEach(
                        ext -> ext.getValue().castToCode(ext.getValue()).setValue("Hello World"))));

    manipulators.add(
        NamedEnvelope.of(
            "Normgröße 'random'",
            b ->
                b.getMedication().getExtension().stream()
                    .filter(
                        ext ->
                            ext.getUrl()
                                .equals(DeBasisProfilStructDef.NORMGROESSE.getCanonicalUrl()))
                    .forEach(
                        ext ->
                            ext.getValue()
                                .castToCode(ext.getValue())
                                .setValue(GemFaker.getFaker().regexify(("\\w{3,100}"))))));

    manipulators.add(
        NamedEnvelope.of(
            "Vaccine-Extension mit StringType",
            b ->
                b.getMedication().getExtension().stream()
                    .filter(KbvItaErpStructDef.MEDICATION_VACCINE::matches)
                    .forEach(ext -> ext.setValue(new StringType("false")))));
    manipulators.add(
        NamedEnvelope.of(
            "Vaccine-Extension mit IntegerType",
            b ->
                b.getMedication().getExtension().stream()
                    .filter(KbvItaErpStructDef.MEDICATION_VACCINE::matches)
                    .forEach(ext -> ext.setValue(new IntegerType(1)))));

    manipulators.add(
        NamedEnvelope.of(
            "Medication mit fehlender ID", b -> b.getMedication().setId((String) null)));

    manipulators.add(
        NamedEnvelope.of(
            "MedicationType-manipulator RandomValue",
            b ->
                b.getMedication().getExtension().stream()
                    .filter(ext -> KbvBasisStructDef.BASE_MEDICATION_TYPE.matches(ext.getUrl()))
                    .forEach(
                        ext -> {
                          val newCode =
                              String.valueOf(GemFaker.getFaker().random().nextInt(10, 990000000));
                          ext.setValue(new CodeType(newCode));
                        })));

    manipulators.add(
        NamedEnvelope.of(
            "MedicationType-manipulator Null-Setter",
            b ->
                b.getMedication().getExtension().stream()
                    .filter(ext -> KbvBasisStructDef.BASE_MEDICATION_TYPE.matches(ext.getUrl()))
                    .forEach(ext -> ext.setValue(new CodeType(null)))));

    manipulators.add(
        NamedEnvelope.of(
            "MedicationCategory-manipulator set 0",
            b ->
                b.getMedication().getExtension().stream()
                    .filter(KbvItaErpStructDef.MEDICATION_CATEGORY::matches)
                    .forEach(ext -> ext.setValue(new CodeType("0")))));

    manipulators.add(
        NamedEnvelope.of(
            "MedicationCategory-manipulator set null",
            b ->
                b.getMedication().getExtension().stream()
                    .filter(KbvItaErpStructDef.MEDICATION_CATEGORY::matches)
                    .forEach(ext -> ext.setValue(new CodeType(null)))));

    manipulators.add(
        NamedEnvelope.of(
            "MedicationCategory-manipulator set 04",
            b ->
                b.getMedication().getExtension().stream()
                    .filter(KbvItaErpStructDef.MEDICATION_CATEGORY::matches)
                    .forEach(ext -> ext.setValue(new CodeType("04")))));

    manipulators.add(
        NamedEnvelope.of(
            "MedicationCategory-manipulator set the 100",
            b ->
                b.getMedication().getExtension().stream()
                    .filter(KbvItaErpStructDef.MEDICATION_CATEGORY::matches)
                    .forEach(ext -> ext.setValue(new CodeType("the 100")))));

    return manipulators;
  }

  public static List<NamedEnvelope<FuzzingMutator<KbvErpBundle>>>
      getMedicationRequestManipulators() {
    val manipulators = new LinkedList<NamedEnvelope<FuzzingMutator<KbvErpBundle>>>();

    manipulators.add(
        NamedEnvelope.of(
            "MedicationRequest Quantity Code {Beutel}",
            b -> b.getMedicationRequest().getDispenseRequest().getQuantity().setCode("{Beutel}")));

    manipulators.add(
        NamedEnvelope.of(
            "MedicationRequest Quantity System",
            b ->
                b.getMedicationRequest()
                    .getDispenseRequest()
                    .getQuantity()
                    .setSystem("HTTP://unitsofmeasure.org")));

    manipulators.add(
        NamedEnvelope.of(
            "MedicationRequest Validity Period Start",
            b ->
                b.getMedicationRequest()
                    .getDispenseRequest()
                    .getValidityPeriod()
                    .getStartElement()
                    .setValue(Date.valueOf(LocalDate.now().minusDays(1)))));

    manipulators.add(
        NamedEnvelope.of(
            "MedicationRequest Validity Period End",
            b ->
                b.getMedicationRequest()
                    .getDispenseRequest()
                    .getValidityPeriod()
                    .getEndElement()
                    .setValue(Date.valueOf(LocalDate.now().plusDays(1)))));

    manipulators.add(
        NamedEnvelope.of(
            "MedicationRequest mit fehlender ID",
            b -> b.getMedicationRequest().setId((String) null)));

    manipulators.add(
        NamedEnvelope.of(
            "MedicationRequest Validity Period Start & End",
            b -> {
              b.getMedicationRequest()
                  .getDispenseRequest()
                  .getValidityPeriod()
                  .getStartElement()
                  .setValue(Date.valueOf(LocalDate.now().minusDays(1)));

              b.getMedicationRequest()
                  .getDispenseRequest()
                  .getValidityPeriod()
                  .getEndElement()
                  .setValue(Date.valueOf(LocalDate.now().plusDays(1)));
            }));

    manipulators.add(
        NamedEnvelope.of(
            "MedicationRequest mit falscher Requester-Referenz",
            b -> {
              b.getMedicationRequestOptional()
                  .ifPresent(
                      mr ->
                          mr.getRequester()
                              .setReference(
                                  format("Practitioner/{0}", UUID.randomUUID().toString())));
              b.getSupplyRequest()
                  .ifPresent(
                      sr ->
                          sr.getRequester()
                              .setReference(
                                  format("Practitioner/{0}", UUID.randomUUID().toString())));
            }));
    manipulators.add(
        NamedEnvelope.of(
            "MedicationRequest mit fehlender Ressource in der Requester-Referenz",
            b -> {
              val originalRef = b.getMedicationRequest().getRequester().getReference();
              val uuidRefTokens = originalRef.split("[/:]");
              val uuidRef = uuidRefTokens[uuidRefTokens.length - 1];
              b.getMedicationRequest().getRequester().setReference(uuidRef);
            }));
    manipulators.add(
        NamedEnvelope.of(
            "MedicationRequest ohne Requester-Referenz",
            b -> b.getMedicationRequest().setRequester(null)));

    return manipulators;
  }

  public static List<NamedEnvelope<FuzzingMutator<KbvErpBundle>>> getOrganizationManipulators() {
    val manipulators = new LinkedList<NamedEnvelope<FuzzingMutator<KbvErpBundle>>>();

    manipulators.add(
        NamedEnvelope.of(
            "IKNR als Identifier anstatt BSNR",
            b -> {
              val iknrIdentifier =
                  b.getMedicalOrganization().getIdentifier().stream()
                      .filter(id -> id.getType().getCodingFirstRep().getCode().equals("BSNR"))
                      .findFirst()
                      .orElseThrow(
                          () -> new MissingFieldException(KbvMedicalOrganization.class, "BSNR"));
              iknrIdentifier
                  .setValue("7421111100")
                  .setSystem(DeBasisProfilNamingSystem.IKNR.getCanonicalUrl());
            }));

    manipulators.add(
        NamedEnvelope.of(
            "BSNR Identifier vom Typ KZVA",
            b -> {
              val bsnrIdentifier =
                  b.getMedicalOrganization().getIdentifier().stream()
                      .filter(id -> id.getType().getCodingFirstRep().getCode().equals("BSNR"))
                      .findFirst()
                      .orElseThrow(
                          () -> new MissingFieldException(KbvMedicalOrganization.class, "BSNR"));
              bsnrIdentifier
                  .getType()
                  .getCodingFirstRep()
                  .setSystem(IdentifierTypeDe.CODE_SYSTEM.getCanonicalUrl())
                  .setCode(IdentifierTypeDe.KZVA.getCode());
            }));

    manipulators.add(
        NamedEnvelope.of(
            "KZVA Alphanummerisch",
            b -> {
              val iknrIdentifier =
                  b.getMedicalOrganization().getIdentifier().stream()
                      .filter(id -> id.getType().getCodingFirstRep().getCode().equals("BSNR"))
                      .findFirst()
                      .orElseThrow(
                          () -> new MissingFieldException(KbvMedicalOrganization.class, "BSNR"));
              iknrIdentifier
                  .getType()
                  .getCodingFirstRep()
                  .setSystem(IdentifierTypeDe.CODE_SYSTEM.getCanonicalUrl())
                  .setCode(IdentifierTypeDe.KZVA.getCode());
              iknrIdentifier
                  .setSystem(
                      DeBasisProfilNamingSystem.KZBV_KZVA_ABRECHNUNGSNUMMER.getCanonicalUrl())
                  .setValue("721L11101");
            }));
    manipulators.add(
        NamedEnvelope.of(
            "KZVA 10-stellig",
            b -> {
              val iknrIdentifier =
                  b.getMedicalOrganization().getIdentifier().stream()
                      .filter(id -> id.getType().getCodingFirstRep().getCode().equals("BSNR"))
                      .findFirst()
                      .orElseThrow(
                          () -> new MissingFieldException(KbvMedicalOrganization.class, "BSNR"));
              iknrIdentifier
                  .getType()
                  .getCodingFirstRep()
                  .setSystem(IdentifierTypeDe.CODE_SYSTEM.getCanonicalUrl())
                  .setCode(IdentifierTypeDe.KZVA.getCode());
              iknrIdentifier
                  .setSystem(
                      DeBasisProfilNamingSystem.KZBV_KZVA_ABRECHNUNGSNUMMER.getCanonicalUrl())
                  .setValue("7211111010");
            }));

    manipulators.add(
        NamedEnvelope.of(
            "Leerzeichen nach der Organization-Adresse",
            b -> {
              val addressLine = b.getPatient().getAddressFirstRep().getLine().get(0);
              addressLine.setValue("Bahnhofstr.  14");
              addressLine
                  .getExtensionByUrl(HL7StructDef.STREET_NAME.getCanonicalUrl())
                  .setValue(new StringType("Bahnhofstr.  "));
              addressLine
                  .getExtensionByUrl(HL7StructDef.HOUSE_NUMBER.getCanonicalUrl())
                  .setValue(new StringType("14"));
            }));

    manipulators.add(
        NamedEnvelope.of(
            "Organization mit fehlender ID", b -> b.getMedicalOrganization().setId((String) null)));

    return manipulators;
  }

  /**
   * These manipulators try to replicate the issue reported in ERPFIND-1068 However, from KBV_FOR
   * 1.2.0 there is no formal restriction on spaces in the BSNR
   *
   * @see <a href="https://service.gematik.de/browse/ERPFIND-1068">ERPFIND-1068</a>
   * @see <a
   *     href="https://simplifier.net/packages/de.basisprofil.r4/1.5.2/files/2720742/~json">KBV_PR_FOR_Organization
   *     1.2.0</a>
   * @return a list of manipualators for the BSNR of the Organization
   */
  public static List<NamedEnvelope<FuzzingMutator<KbvErpBundle>>>
      getOrganizationBsnrManipulators() {
    val manipulators = new LinkedList<NamedEnvelope<FuzzingMutator<KbvErpBundle>>>();
    for (var i = 0; i < 2; i++) {
      // length of BSNR is 9, after manipulation it is expected to be 9 - i
      val newLen = 9 - i;
      int finalI = i;
      manipulators.add(
          NamedEnvelope.of(
              format("BSNR enthält Leerzeichen ({0}-stellig)", newLen),
              b -> {
                val bsnrIdentifier =
                    b.getMedicalOrganization().getIdentifier().stream()
                        .filter(id -> id.getType().getCodingFirstRep().getCode().equals("BSNR"))
                        .findFirst()
                        .orElseThrow(
                            () -> new MissingFieldException(KbvMedicalOrganization.class, "BSNR"));
                val bsnr = bsnrIdentifier.getValue();
                val idx = GemFaker.getFaker().random().nextInt(0, bsnr.length() - 1 - finalI);
                val newBsnr = bsnr.substring(0, idx) + " " + bsnr.substring(idx + 1 + finalI);
                bsnrIdentifier.setValue(newBsnr);
              }));
    }
    return manipulators;
  }

  public static List<NamedEnvelope<FuzzingMutator<KbvErpBundle>>> getPatientManipulators() {
    val manipulators = new LinkedList<NamedEnvelope<FuzzingMutator<KbvErpBundle>>>();

    manipulators.add(
        NamedEnvelope.of(
            "ACME Patient Identifier ohne Type",
            b ->
                b.getPatient()
                    .getIdentifierFirstRep()
                    .setSystem(CommonNamingSystem.ACME_IDS_PATIENT.getCanonicalUrl())
                    .setValue("123456")
                    .setType(null)));

    manipulators.add(
        NamedEnvelope.of(
            "ACME Patient Identifier mit validem Type",
            b ->
                b.getPatient()
                    .getIdentifierFirstRep()
                    .setSystem(CommonNamingSystem.ACME_IDS_PATIENT.getCanonicalUrl())));

    manipulators.add(
        NamedEnvelope.of(
            "GKV KVNR Constraint: beginnt mit Ziffer",
            b -> b.getPatient().getIdentifierFirstRep().setValue("1X11043327")));

    manipulators.add(
        NamedEnvelope.of(
            "GKV KVNR Constraint: beginnt mit Umlaut",
            b -> b.getPatient().getIdentifierFirstRep().setValue("Ü110433273")));

    manipulators.add(
        NamedEnvelope.of(
            "GKV KVNR Constraint: beginnt mit Kleinbuchstaben",
            b -> b.getPatient().getIdentifierFirstRep().setValue("x110433273")));

    manipulators.add(
        NamedEnvelope.of(
            "GKV KVNR Constraint: 9-stellig",
            b -> b.getPatient().getIdentifierFirstRep().setValue("X11043332")));

    manipulators.add(
        NamedEnvelope.of(
            "GKV KVNR Constraint: 11-stellig",
            b -> b.getPatient().getIdentifierFirstRep().setValue("X1104333200")));

    manipulators.add(
        NamedEnvelope.of(
            "Patientenname mit überlangem Prefix",
            b ->
                b.getPatient()
                    .getNameFirstRep()
                    .getFamilyElement()
                    .addExtension(
                        HL7StructDef.HUMAN_OWN_PREFIX.asStringExtension(
                            "von und zu aber echt jetzt"))));

    manipulators.add(
        NamedEnvelope.of(
            "Patientenname mit überlangem Namenszusatz",
            b ->
                b.getPatient()
                    .getNameFirstRep()
                    .getFamilyElement()
                    .addExtension(
                        DeBasisProfilStructDef.HUMAN_NAMENSZUSATZ.getCanonicalUrl(),
                        new StringType("Graf Freiherr der Letzte"))));

    manipulators.add(
        NamedEnvelope.of("Patient mit fehlender ID", b -> b.getPatient().setId((String) null)));

    return manipulators;
  }

  public static List<NamedEnvelope<FuzzingMutator<KbvErpBundle>>> getPractitionerManipulators() {
    val manipulators = new LinkedList<NamedEnvelope<FuzzingMutator<KbvErpBundle>>>();

    manipulators.add(
        NamedEnvelope.of(
            "Practitioner AlphaNum LANR/ZANR Identifier",
            b ->
                b.getPractitioner().getIdentifier().stream()
                    .filter(BaseANR::isPractitioner)
                    .findFirst()
                    .orElseThrow(
                        () ->
                            new MissingFieldException(
                                KbvPractitioner.class, BaseANR.ANRType.validCodeTypes()))
                    .setValue("987G54423")));

    manipulators.add(
        NamedEnvelope.of(
            "Practitioner LANR/ZANR Identifier 8-stellig",
            b ->
                b.getPractitioner().getIdentifier().stream()
                    .filter(BaseANR::isPractitioner)
                    .findFirst()
                    .orElseThrow(
                        () ->
                            new MissingFieldException(
                                KbvPractitioner.class, BaseANR.ANRType.validCodeTypes()))
                    .setValue("87654423")));

    manipulators.add(
        NamedEnvelope.of(
            "Practitioner LANR Identifier 10-stellig",
            b ->
                b.getPractitioner().getIdentifier().stream()
                    .filter(BaseANR::isPractitioner)
                    .findFirst()
                    .orElseThrow(
                        () ->
                            new MissingFieldException(
                                KbvPractitioner.class, BaseANR.ANRType.validCodeTypes()))
                    .setValue("1087654423")));

    manipulators.add(
        NamedEnvelope.of(
            "Practitioner mit mehreren LANR und ZANR",
            b ->
                b.getPractitioner()
                    .getIdentifier()
                    .addAll(List.of(ZANR.random().asIdentifier(), LANR.random().asIdentifier()))));

    manipulators.add(
        NamedEnvelope.of(
            "Practitioner LÜNR/ZÜNR Identifier",
            b -> {
              val coding =
                  b.getPractitioner().getIdentifier().stream()
                      .filter(BaseANR::isPractitioner)
                      .findFirst()
                      .orElseThrow(
                          () ->
                              new MissingFieldException(
                                  KbvPractitioner.class, BaseANR.ANRType.validCodeTypes()))
                      .getType()
                      .getCodingFirstRep();
              coding.setCode(coding.getCode().replace("A", "Ü"));
            }));

    manipulators.add(
        NamedEnvelope.of(
            "Practitioner mit fehlendem Qualification Code",
            b ->
                b.getPractitioner()
                    .getQualificationFirstRep()
                    .getCode()
                    .setCoding(List.of())
                    .setText("Chefarzt")));

    manipulators.add(
        NamedEnvelope.of(
            "Practitioner ohne QualificationTypes",
            b -> b.getPractitioner().setQualification(List.of())));

    manipulators.add(
        NamedEnvelope.of(
            "Practitioner mit ungültigen QualificationTypes",
            b -> {
              val qt1 = new CodeableConcept();
              qt1.setText("Chefarzt");
              val qt2 = new CodeableConcept();
              qt2.setText("Onkologie");
              val qualificationTypes =
                  List.of(
                      new Practitioner.PractitionerQualificationComponent(qt1),
                      new Practitioner.PractitionerQualificationComponent(qt2));
              b.getPractitioner().setQualification(qualificationTypes);
            }));

    manipulators.add(
        NamedEnvelope.of(
            "Practitioner mit fehlender Berufsbezeichnung",
            b -> {
              val practitioner = b.getPractitioner();
              val jobTitleOpt =
                  practitioner.getQualification().stream()
                      .filter(
                          qualification ->
                              qualification.getCode().getCoding().stream()
                                  .anyMatch(
                                      code ->
                                          KbvCodeSystem.BERUFSBEZEICHNUNG.matches(
                                              code.getSystem())))
                      .findFirst();
              jobTitleOpt.ifPresent(jt -> practitioner.getQualification().remove(jt));
            }));

    manipulators.add(
        NamedEnvelope.of(
            "Zusätzlicher nicht referenzierter Practitioner",
            b ->
                b.getEntry()
                    .add(
                        new Bundle.BundleEntryComponent()
                            .setResource(KbvPractitionerFaker.builder().fake()))));

    manipulators.add(
        NamedEnvelope.of(
            "Practitioner mit fehlender ID", b -> b.getPractitioner().setId((String) null)));

    return manipulators;
  }

  public static List<NamedEnvelope<FuzzingMutator<KbvErpBundle>>> getCompositionManipulators() {
    val manipulators = new LinkedList<NamedEnvelope<FuzzingMutator<KbvErpBundle>>>();

    manipulators.add(
        NamedEnvelope.of(
            "Composition Legal Basis 09",
            b -> changeExtensionCode(b.getComposition(), KbvItaForStructDef.BASIS, "09")));

    manipulators.add(
        NamedEnvelope.of(
            "Composition ohne Coverage Section",
            b ->
                b.getComposition()
                    .getSection()
                    .removeIf(s -> s.getCode().getCodingFirstRep().getCode().equals("Coverage"))));

    manipulators.add(
        NamedEnvelope.of(
            "Composition mit invalider Formular Art",
            b -> b.getComposition().getType().getCodingFirstRep().setCode("e16a")));

    manipulators.add(
        NamedEnvelope.of(
            "Composition mit fehlender ID", b -> b.getComposition().setId((String) null)));

    return manipulators;
  }

  public static List<NamedEnvelope<FuzzingMutator<KbvErpBundle>>> getSystemsManipulators() {
    val manipulators = new LinkedList<NamedEnvelope<FuzzingMutator<KbvErpBundle>>>();

    manipulators.add(
        NamedEnvelope.of(
            "snomed system as Identifier instead of PrescriptionId",
            b -> b.getIdentifier().setSystem("http://some.invalid.system/url")));
    manipulators.add(
        NamedEnvelope.of(
            "switch patients KVNR-System to IKNR_SID",
            b ->
                b.getPatient().getIdentifier().stream()
                    .filter(KVID_GKV_SID::matches)
                    .findFirst()
                    .map(id -> id.setSystem(IKNR_SID.getCanonicalUrl()))
                    .orElseThrow()));
    manipulators.add(
        NamedEnvelope.of(
            "switch MedicationRequests Notdienstgebühr URL to DOSAGE_FLAG",
            b ->
                b.getMedicationRequest().getExtension().stream()
                    .filter(EMERGENCY_SERVICES_FEE::matches)
                    .map(ext -> ext.setUrl(DOSAGE_FLAG.getCanonicalUrl()))
                    .findFirst()
                    .orElseThrow()));
    manipulators.add(
        NamedEnvelope.of(
            "coverage.Payors system to SER",
            b ->
                b.getCoverage()
                    .getPayorFirstRep()
                    .getIdentifier()
                    .setSystem(SER.getCanonicalUrl())));

    return manipulators;
  }

  private static void changeExtensionCode(DomainResource resource, WithSystem url, String code) {
    val ext = resource.getExtensionByUrl(url.getCanonicalUrl());
    val coding = ext.getValue().castToCoding(ext.getValue());
    coding.setCode(code);
  }

  public static List<NamedEnvelope<FuzzingMutator<KbvErpBundle>>> getKbvBundleEntryManipulators() {
    return Stream.of(ResourceType.MedicationRequest, ResourceType.Medication)
        .map(
            rt -> {
              String name = String.format("KbvBundle without %s Resource", rt.name());
              FuzzingMutator<KbvErpBundle> mutator =
                  b -> b.getEntry().removeIf(e -> e.getResource().getResourceType().equals(rt));
              return NamedEnvelope.of(name, mutator);
            })
        .toList();
  }
}
