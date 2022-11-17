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

package de.gematik.test.fuzzing.kbv;

import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.exceptions.MissingFieldException;
import de.gematik.test.erezept.fhir.parser.profiles.IWithSystem;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.DeBasisStructDef;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.Hl7StructDef;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.KbvItaForStructDef;
import de.gematik.test.erezept.fhir.parser.profiles.systems.CommonNamingSystem;
import de.gematik.test.erezept.fhir.parser.profiles.systems.DeBasisNamingSystem;
import de.gematik.test.erezept.fhir.resources.kbv.KbvErpBundle;
import de.gematik.test.erezept.fhir.resources.kbv.KbvPractitioner;
import de.gematik.test.erezept.fhir.resources.kbv.MedicalOrganization;
import de.gematik.test.erezept.fhir.values.BaseANR;
import de.gematik.test.erezept.fhir.values.LANR;
import de.gematik.test.erezept.fhir.values.ZANR;
import de.gematik.test.erezept.fhir.valuesets.Darreichungsform;
import de.gematik.test.erezept.fhir.valuesets.IdentifierTypeDe;
import de.gematik.test.erezept.fhir.valuesets.MedicationType;
import de.gematik.test.fuzzing.core.FuzzingMutator;
import de.gematik.test.fuzzing.core.NamedEnvelope;
import java.sql.Date;
import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;
import lombok.val;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.StringType;

public class KbvBundleManipulatorFactory {

  private KbvBundleManipulatorFactory() {
    throw new AssertionError("Do not instantiate");
  }

  /**
   * Provides all available manipulators on the KBV-Bundle which includes different manipulators for
   * entries of the KBV-Bundle
   *
   * @return list of NamedParameters containing manipulators
   */
  public static List<NamedEnvelope<FuzzingMutator<KbvErpBundle>>> getAllKbvBundleManipulators() {
    val manipulators = new LinkedList<>(getCoverageManipulators());
    manipulators.addAll(getMedicationManipulators());
    manipulators.addAll(getMedicationRequestManipulators());
    manipulators.addAll(getOrganizationManipulators());
    manipulators.addAll(getPatientManipulators());
    manipulators.addAll(getPractitionerManipulators());
    manipulators.addAll(getCompositionManipulators());

    return manipulators;
  }

  public static List<NamedEnvelope<FuzzingMutator<KbvErpBundle>>> getCoverageManipulators() {
    val manipulators = new LinkedList<NamedEnvelope<FuzzingMutator<KbvErpBundle>>>();

    manipulators.add(
        NamedEnvelope.of(
            "DMP Code 42",
            b -> changeExtensionCode(b.getCoverage(), DeBasisStructDef.GKV_DMP_KENNZEICHEN, "42")));

    manipulators.add(
        NamedEnvelope.of(
            "DMP Code 0 (!= 00)",
            b -> changeExtensionCode(b.getCoverage(), DeBasisStructDef.GKV_DMP_KENNZEICHEN, "0")));

    manipulators.add(
        NamedEnvelope.of(
            "DMP Code 12",
            b -> changeExtensionCode(b.getCoverage(), DeBasisStructDef.GKV_DMP_KENNZEICHEN, "12")));

    manipulators.add(
        NamedEnvelope.of(
            "DMP Code Asthma",
            b ->
                changeExtensionCode(
                    b.getCoverage(), DeBasisStructDef.GKV_DMP_KENNZEICHEN, "asthma")));

    manipulators.add(
        NamedEnvelope.of(
            "Besondere Personengruppen Code -T",
            b -> changeExtensionCode(b.getCoverage(), DeBasisStructDef.GKV_PERSON_GROUP, "-T")));

    manipulators.add(
        NamedEnvelope.of(
            "Besondere Personengruppen Code 0 (!= 00)",
            b -> changeExtensionCode(b.getCoverage(), DeBasisStructDef.GKV_PERSON_GROUP, "0")));

    manipulators.add(
        NamedEnvelope.of(
            "Besondere Personengruppen Code 10",
            b -> changeExtensionCode(b.getCoverage(), DeBasisStructDef.GKV_PERSON_GROUP, "10")));

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
            b -> changeExtensionCode(b.getCoverage(), DeBasisStructDef.GKV_VERSICHERTENART, "P")));

    manipulators.add(
        NamedEnvelope.of(
            "WOP 40", b -> changeExtensionCode(b.getCoverage(), DeBasisStructDef.GKV_WOP, "40")));

    manipulators.add(
        NamedEnvelope.of(
            "WOP 1000",
            b -> changeExtensionCode(b.getCoverage(), DeBasisStructDef.GKV_WOP, "1000")));

    manipulators.add(
        NamedEnvelope.of(
            "Invalide IKNR Länge der Versicherung",
            b -> {
              val iknrIdentifier = b.getCoverage().getPayorFirstRep().getIdentifier();
              iknrIdentifier.setValue("0721111100");
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
            "MedicationRequest Quantity 1000",
            b -> b.getMedicationRequest().getDispenseRequest().getQuantity().setValue(1000)));

    manipulators.add(
        NamedEnvelope.of(
            "MedicationRequest DosageInstruction Text Length",
            b ->
                b.getMedicationRequest()
                    .getDosageInstructionFirstRep()
                    .setText(
                        "jeweils zu den Mahlzeiten und die Tablette 32 mal ordentlich zerkauen")));

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
                          () -> new MissingFieldException(MedicalOrganization.class, "BSNR"));
              iknrIdentifier
                  .setValue("7421111100")
                  .setSystem(DeBasisNamingSystem.IKNR.getCanonicalUrl());
            }));

    manipulators.add(
        NamedEnvelope.of(
            "BSNR Identifier vom Typ KZVA",
            b -> {
              val iknrIdentifier =
                  b.getMedicalOrganization().getIdentifier().stream()
                      .filter(id -> id.getType().getCodingFirstRep().getCode().equals("BSNR"))
                      .findFirst()
                      .orElseThrow(
                          () -> new MissingFieldException(MedicalOrganization.class, "BSNR"));
              iknrIdentifier
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
                          () -> new MissingFieldException(MedicalOrganization.class, "BSNR"));
              iknrIdentifier
                  .getType()
                  .getCodingFirstRep()
                  .setSystem(IdentifierTypeDe.CODE_SYSTEM.getCanonicalUrl())
                  .setCode(IdentifierTypeDe.KZVA.getCode());
              iknrIdentifier
                  .setSystem(DeBasisNamingSystem.KZVA_ABRECHNUNGSNUMMER.getCanonicalUrl())
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
                          () -> new MissingFieldException(MedicalOrganization.class, "BSNR"));
              iknrIdentifier
                  .getType()
                  .getCodingFirstRep()
                  .setSystem(IdentifierTypeDe.CODE_SYSTEM.getCanonicalUrl())
                  .setCode(IdentifierTypeDe.KZVA.getCode());
              iknrIdentifier
                  .setSystem(DeBasisNamingSystem.KZVA_ABRECHNUNGSNUMMER.getCanonicalUrl())
                  .setValue("7211111010");
            }));

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
                        Hl7StructDef.HUMAN_OWN_PREFIX.getCanonicalUrl(),
                        new StringType("von und zu aber echt jetzt"))));

    manipulators.add(
        NamedEnvelope.of(
            "Patientenname mit überlangem Namenszusatz",
            b ->
                b.getPatient()
                    .getNameFirstRep()
                    .getFamilyElement()
                    .addExtension(
                        DeBasisStructDef.HUMAN_NAMENSZUSATZ.getCanonicalUrl(),
                        new StringType("Graf Freiherr der Letzte"))));

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

    return manipulators;
  }

  private static void changeExtensionCode(DomainResource resource, IWithSystem url, String code) {
    val ext = resource.getExtensionByUrl(url.getCanonicalUrl());
    val coding = ext.getValue().castToCoding(ext.getValue());
    coding.setCode(code);
  }
}
