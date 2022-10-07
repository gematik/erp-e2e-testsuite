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

package de.gematik.test.erezept.primsys.cli;

import static java.text.MessageFormat.format;

import ca.uhn.fhir.validation.ValidationResult;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.builder.dav.*;
import de.gematik.test.erezept.fhir.builder.kbv.*;
import de.gematik.test.erezept.fhir.parser.EncodingType;
import de.gematik.test.erezept.fhir.parser.FhirParser;
import de.gematik.test.erezept.fhir.resources.dav.DavAbgabedatenBundle;
import de.gematik.test.erezept.fhir.resources.kbv.KbvErpBundle;
import de.gematik.test.erezept.fhir.util.Currency;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.valuesets.*;
import de.gematik.test.erezept.fhir.valuesets.dav.KostenVersicherterKategorie;
import de.gematik.test.erezept.lei.cfg.TestsuiteConfiguration;
import de.gematik.test.fuzzing.kbv.KbvBundleManipulatorFactory;
import de.gematik.test.smartcard.Hba;
import de.gematik.test.smartcard.factory.SmartcardFactory;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import picocli.CommandLine;

@Slf4j
public class FhirFakerGenerator implements Callable<Integer> {

  @CommandLine.Option(
      names = "-n",
      paramLabel = "<NUM>",
      type = Integer.class,
      defaultValue = "10",
      description = "Number of Faker-Resources to produce (default=${DEFAULT-VALUE})")
  private int numOfElements;

  @CommandLine.Option(
      names = "--encoding",
      paramLabel = "<TYPE>",
      type = EncodingType.class,
      defaultValue = "XML",
      description = "Type of encoding (default=${DEFAULT-VALUE})")
  private EncodingType encodingType;

  @CommandLine.Option(
      names = "--pretty",
      paramLabel = "Pretty Print",
      type = Boolean.class,
      description = "Pretty Print the Output")
  private boolean prettyPrint = true;

  @CommandLine.Option(
      names = "--mvo",
      paramLabel = "MVO",
      type = Boolean.class,
      description = "Multiple Prescriptions (MVO) only")
  private boolean mvoOnly = false;

  @CommandLine.Option(
      names = "--fhirfuzzing",
      paramLabel = "BOOL",
      type = Boolean.class,
      description = "Activate FHIR-Fuzzing")
  private boolean fhirFuzzing = false;

  @CommandLine.Option(
      names = "--nodav",
      paramLabel = "BOOL",
      type = Boolean.class,
      description = "If set, no DAV Bundles will be created")
  private boolean noDav = false;

  @CommandLine.Option(
      names = "--nokbv",
      paramLabel = "BOOL",
      type = Boolean.class,
      description = "If set, no KBV Bundles will be created")
  private boolean noKbv = false;

  @CommandLine.Option(
      names = "--output",
      type = Path.class,
      description = "Directory to store the output-file (default=current working directory)")
  private Path outdir;

  private final FhirParser fhir;

  public FhirFakerGenerator() {
    this.fhir = new FhirParser();
  }

  @Override
  public Integer call() throws Exception {
    val smartcards = SmartcardFactory.readArchive();
    val cfg = TestsuiteConfiguration.getInstance();

    val doc = cfg.getActors().getDoctors().get(0);
    val hba = smartcards.getHbaByICCSN(doc.getHbaIccsn(), doc.getCryptoAlgorithm());

    if (!noKbv) {
      for (var i = 0; i < numOfElements; i++) {
        val kbvBundle = createPrescription(hba);
        val xml = fhir.encode(kbvBundle, encodingType, prettyPrint);

        val result = fhir.validate(xml);
        val baseName = createBaseName(kbvBundle, result.isSuccessful());
        writeResource(xml, format("{0}.{1}", baseName, encodingType.toFileExtension()));
        log.info(format("Created Prescription: {0}", baseName));
        if (!result.isSuccessful()) {
          writeHapiResult(result, format("{0}.hapi", baseName));
        }

        if (fhirFuzzing) {
          kbvFhirFuzzing(kbvBundle);
        }
      }
    }

    if (!noDav) {
      for (var i = 0; i < numOfElements; i++) {
        val davBundle = createDavAbgabedaten();
        val xml = fhir.encode(davBundle, encodingType, true);

        val result = fhir.validate(xml);
        val baseName = createBaseName(davBundle, result.isSuccessful());
        writeResource(xml, format("{0}.{1}", baseName, encodingType.toFileExtension()));
        log.info(format("Created Abgabedatum: {0}", baseName));
        if (!result.isSuccessful()) {
          writeHapiResult(result, format("{0}.hapi", baseName));
        }
      }
    }

    smartcards.destroy();
    return 0;
  }

  private void kbvFhirFuzzing(KbvErpBundle bundle) {
    val manipulators = KbvBundleManipulatorFactory.getAllKbvBundleManipulators();

    manipulators.forEach(
        m -> {
          val fBundle = KbvErpBundle.fromBundle(bundle);
          m.getParameter().accept(fBundle);
          val xml = fhir.encode(fBundle, encodingType, prettyPrint);

          val result = fhir.validate(xml);
          val baseName = createBaseName(bundle, result.isSuccessful());
          val fuzzingName = m.getName().replace(" ", "_").replace(":", "");
          val fName = format("{0}_{1}", baseName, fuzzingName);
          writeResource(xml, format("{0}.{1}", fName, encodingType.toFileExtension()));
          log.info(format("Created Prescription: {0}", fName));
          if (!result.isSuccessful()) {
            writeHapiResult(result, format("{0}.hapi", fName));
          }
        });
  }

  private DavAbgabedatenBundle createDavAbgabedaten() {
    val prescriptionId = PrescriptionId.random(GemFaker.fakerValueSet(PrescriptionFlowType.class));

    val pharmacy =
        PharmacyOrganizationBuilder.builder()
            .name(GemFaker.pharmacyName())
            .iknr(GemFaker.fakerIknr())
            .address(
                Country.D, GemFaker.fakerCity(), GemFaker.fakerZipCode(), GemFaker.fullStreetName())
            .build();

    val pc1 =
        PriceComponentBuilder.builder(GemFaker.fakerValueSet(KostenVersicherterKategorie.class))
            .currency(Currency.EUR) // EUR by default
            .type("informational")
            .insurantCost(5.8f)
            .totalCost(289.99f)
            .build();

    val invoice =
        DavInvoiceBuilder.builder()
            .currency(Currency.EUR) // EUR by default
            .status("issued")
            .vatRate(19.0f)
            .addPriceComponent(pc1, GemFaker.fakerPzn())
            .build();

    val dispensedMedication =
        DavDispensedMedicationBuilder.builder()
            .status("completed")
            .prescription(prescriptionId)
            .pharmacy(pharmacy)
            .invoice(invoice)
            .build();

    val davBundle =
        DavAbgabedatenBuilder.builder(prescriptionId)
            .pharmacy(pharmacy)
            .medication(dispensedMedication)
            .invoice(invoice);

    return davBundle.build();
  }

  private KbvErpBundle createPrescription(Hba hba) {
    val qualification = GemFaker.fakerQualificationType();
    val practitioner =
        PractitionerBuilder.builder()
            .lanr(GemFaker.fakerLanr())
            .name(hba.getOwner().getGivenName(), hba.getOwner().getSurname(), "Dr.")
            .addQualification(qualification)
            .addQualification("Super-Facharzt für alles Mögliche")
            .build();

    val medOrgName = format("{0} {1}", qualification.getDisplay(), hba.getOwner().getSurname());
    val medicalOrganization =
        MedicalOrganizationBuilder.builder()
            .name(medOrgName)
            .bsnr(GemFaker.fakerBsnr())
            .phone(GemFaker.fakerPhone())
            .email(GemFaker.fakerEMail())
            .address(
                Country.D, GemFaker.fakerCity(), GemFaker.fakerZipCode(), GemFaker.fullStreetName())
            .build();

    // will be required only for PKV
    val assignerOrganization =
        AssignerOrganizationBuilder.builder()
            .name(GemFaker.insuranceName())
            .iknr(GemFaker.fakerIknr())
            .phone(GemFaker.fakerPhone())
            .build();

    val patientIdentifierType = GemFaker.randomElement(IdentifierTypeDe.GKV, IdentifierTypeDe.PKV);
    val patient =
        PatientBuilder.builder()
            .kvIdentifierDe(GemFaker.fakerKvid(), patientIdentifierType)
            .name(GemFaker.fakerFirstName(), GemFaker.fakerLastName())
            .assigner(assignerOrganization) // will be used only for PKV patients
            .birthDate(new SimpleDateFormat("dd.MM.yyyy").format(GemFaker.fakerBirthday()))
            .address(
                Country.D, GemFaker.fakerCity(), GemFaker.fakerZipCode(), GemFaker.fullStreetName())
            .build();

    val insurance =
        CoverageBuilder.insurance(GemFaker.fakerIknr(), GemFaker.insuranceName())
            .beneficiary(patient)
            .personGroup(GemFaker.fakerValueSet(PersonGroup.class))
            .dmpKennzeichen(GemFaker.fakerValueSet(DmpKennzeichen.class))
            .wop(GemFaker.fakerValueSet(Wop.class))
            .versichertenStatus(GemFaker.fakerValueSet(VersichertenStatus.class))
            .build();

    val medication =
        KbvErpMedicationBuilder.builder()
            .category(GemFaker.fakerValueSet(MedicationCategory.class))
            .isVaccine(GemFaker.fakerBool())
            .normgroesse(GemFaker.fakerValueSet(StandardSize.class))
            .darreichungsform(GemFaker.fakerValueSet(Darreichungsform.class))
            .amount(GemFaker.fakerAmount(), "Stk")
            .pzn(GemFaker.fakerPzn(), GemFaker.fakerDrugName())
            .build();

    val mvo = mvoOnly ? GemFaker.mvo(true) : GemFaker.mvo();
    val medicationRequest =
        MedicationRequestBuilder.forPatient(patient)
            .insurance(insurance)
            .requester(practitioner)
            .medication(medication)
            .dosage(GemFaker.fakerDosage())
            .quantityPackages(GemFaker.fakerAmount())
            .status("active") // default ACTIVE
            .intent("order") // default ORDER
            .isBVG(GemFaker.fakerBool())
            .mvo(mvo)
            .hasEmergencyServiceFee(GemFaker.fakerBool())
            .substitution(GemFaker.fakerBool())
            .coPaymentStatus(GemFaker.fakerValueSet(StatusCoPayment.class))
            .build();

    val flowType =
        patientIdentifierType == IdentifierTypeDe.GKV
            ? GemFaker.randomElement(
                PrescriptionFlowType.FLOW_TYPE_160, PrescriptionFlowType.FLOW_TYPE_169)
            : GemFaker.randomElement(
                PrescriptionFlowType.FLOW_TYPE_200, PrescriptionFlowType.FLOW_TYPE_209);
    val prescriptionId = PrescriptionId.random(flowType);
    val kbvBundleBuilder =
        KbvErpBundleBuilder.forPrescription(prescriptionId)
            .practitioner(practitioner)
            .custodian(medicalOrganization)
            .patient(patient)
            .insurance(insurance)
            .assigner(assignerOrganization) // will be used only for PKV patients
            .medicationRequest(medicationRequest)
            .medication(medication);

    return kbvBundleBuilder.build();
  }

  private String createBaseName(KbvErpBundle bundle, boolean isValid) {
    var type = "prescription";
    if (bundle.isMultiple()) {
      type = "mvo_" + type;
    }
    val fhirStatus = isValid ? "valid" : "invalid";
    val insuranceKind = bundle.getPatient().getInsuranceKind().getCode();
    val workflow = bundle.getFlowType();
    val name = bundle.getPatient().getNameFirstRep().getNameAsSingleString().replaceAll(" ", "_");
    return format("{0}_{1}_{2}_{3}_{4}", fhirStatus, type, insuranceKind, workflow.getCode(), name);
  }

  private String createBaseName(DavAbgabedatenBundle bundle, boolean isValid) {
    val type = "abgabe";
    val fhirStatus = isValid ? "valid" : "invalid";
    val workflow = bundle.getFlowType();
    val pzn = bundle.getInvoice().getPzn();
    return format("{0}_{1}_{2}_{3}", fhirStatus, type, workflow.getCode(), pzn);
  }

  @SneakyThrows
  @SuppressWarnings({"java:S6300"}) // writing to File by intention; not an issue!
  private void writeResource(String kbvBundle, String filename) {
    if (outdir == null) {
      outdir = Path.of(System.getProperty("user.dir"));
    }

    val out = Path.of(outdir.toAbsolutePath().toString(), filename).toFile();
    log.trace(format("Write KbvBundle to {0}", out.getAbsolutePath()));
    try (val writer = new BufferedWriter(new FileWriter(out))) {
      writer.write(kbvBundle);
    }
  }

  @SneakyThrows
  @SuppressWarnings({"java:S6300"}) // writing to File by intention; not an issue!
  private void writeHapiResult(ValidationResult result, String filename) {

    val out = Path.of(outdir.toAbsolutePath().toString(), filename).toFile();
    log.trace(format("Write HAPI Validation Messages to {0}", out.getAbsolutePath()));

    val r =
        result.getMessages().stream()
            .map(m -> "(" + m.getLocationString() + ") " + m.getMessage())
            .collect(Collectors.joining("\n"));
    val summary = format("Errors: {0}\n{1}", result.getMessages().size(), r);
    try (val writer = new BufferedWriter(new FileWriter(out))) {
      writer.write(summary);
    }
  }
}
