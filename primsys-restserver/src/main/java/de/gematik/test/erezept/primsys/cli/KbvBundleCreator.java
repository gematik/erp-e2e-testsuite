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

import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.builder.kbv.*;
import de.gematik.test.erezept.fhir.parser.EncodingType;
import de.gematik.test.erezept.fhir.parser.FhirParser;
import de.gematik.test.erezept.fhir.resources.kbv.KbvErpBundle;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.valuesets.*;
import de.gematik.test.erezept.lei.cfg.TestsuiteConfiguration;
import de.gematik.test.smartcard.Hba;
import de.gematik.test.smartcard.SmartcardFactory;
import java.text.SimpleDateFormat;
import java.util.concurrent.Callable;
import lombok.val;
import picocli.CommandLine;

public class KbvBundleCreator implements Callable<Integer> {

  @CommandLine.Option(
      names = "--kvnr",
      paramLabel = "KVNR",
      type = String.class,
      description = "The KVID of the receiving user")
  private String kvnrParam = GemFaker.fakerKvid();

  @CommandLine.Option(
      names = "--name",
      paramLabel = "Patient Name",
      type = String.class,
      description = "The name of the insured patient with format \"<givenName> <familyName>\"")
  private String nameParam = GemFaker.fakerName();

  @CommandLine.Option(
      names = "--birthDate",
      paramLabel = "Data",
      type = String.class,
      description = "The birth date of the insured patient with format dd.MM.yyyy")
  private String birthDateParam =
      new SimpleDateFormat("dd.MM.yyyy").format(GemFaker.fakerBirthday());

  @CommandLine.Option(
      names = "--city",
      paramLabel = "City Name",
      type = String.class,
      description = "The city of the insured patient")
  private String cityParam = GemFaker.fakerCity();

  @CommandLine.Option(
      names = "--postal",
      paramLabel = "Postal Code of the City",
      type = String.class,
      description = "The postal code of the city of the insured patient")
  private String postalParam = GemFaker.fakerZipCode();

  @CommandLine.Option(
      names = "--street",
      paramLabel = "Street",
      type = String.class,
      description = "The name of the street with a house number")
  private String streetParam = GemFaker.fullStreetName();

  @CommandLine.Option(
      names = "--insurance",
      paramLabel = "Name of the Insurance",
      type = String.class,
      description = "The name of the insurance of the patient")
  private String insuranceParam = GemFaker.insuranceName();

  @CommandLine.Option(
      names = "--iknr",
      paramLabel = "IKNR of the Insurance",
      type = String.class,
      description = "The IKNR of the insurance of the patient")
  private String iknrParam = GemFaker.fakerIknr();

  @CommandLine.Option(
      names = "--wop",
      paramLabel = "WOP of the Insurance",
      type = Wop.class,
      description = "The WOP of the insurance of the patient")
  private Wop wopParam = GemFaker.fakerValueSet(Wop.class);

  @CommandLine.Option(
      names = "--pzn",
      paramLabel = "PZN",
      type = String.class,
      description = "The PZN of the medication")
  private String pznParam = GemFaker.fakerPzn();

  @CommandLine.Option(
      names = "--med",
      paramLabel = "Drug name",
      type = String.class,
      description = "The name of the prescribed drug")
  private String drugName = GemFaker.fakerDrugName();

  @CommandLine.Option(
      names = "--pretty",
      paramLabel = "Pretty Print",
      type = Boolean.class,
      description = "Pretty Print the Output")
  private boolean prettyPrint = false;

  @Override
  public Integer call() throws Exception {
    val fhir = new FhirParser();
    val smartcards = SmartcardFactory.getArchive();
    val cfg = TestsuiteConfiguration.getInstance();

    val doc = cfg.getActors().getDoctors().get(0);
    val hba = smartcards.getHbaByICCSN(doc.getHbaIccsn(), doc.getCryptoAlgorithm());

    val kbvBundle = create(hba);
    val xml = fhir.encode(kbvBundle, EncodingType.XML, prettyPrint);
    System.out.println(format("\n{0}\n-----", xml));

    return 0;
  }

  private KbvErpBundle create(Hba hba) {
    val practitioner =
        PractitionerBuilder.builder()
            .lanr(GemFaker.fakerLanr())
            .name(hba.getOwner().getGivenName(), hba.getOwner().getSurname(), "Dr.")
            .addQualification(QualificationType.DOCTOR)
            .addQualification("Super-Facharzt für alles Mögliche")
            .build();

    val medicalOrganization =
        MedicalOrganizationBuilder.builder()
            .name("Arztpraxis Meyer")
            .bsnr("757299999")
            .phone("+490309876543")
            .email("info@praxis.de")
            .address(Country.D, "Berlin", "10623", "Wegelystraße 3")
            .build();

    val names = nameParam.split(" ");
    val patient =
        PatientBuilder.builder()
            .kvIdentifierDe(kvnrParam, IdentifierTypeDe.GKV)
            .name(names[0], names[1])
            .birthDate(birthDateParam)
            .address(Country.D, cityParam, postalParam, streetParam)
            .build();

    val insurance =
        CoverageBuilder.insurance(iknrParam, insuranceParam)
            .beneficiary(patient)
            .personGroup(PersonGroup.NOT_SET) // default NOT_SET
            .dmpKennzeichen(DmpKennzeichen.NOT_SET) // default NOT_SET
            .wop(wopParam) // default DUMMY
            .versichertenStatus(VersichertenStatus.MEMBERS) // default MEMBERS
            .build();

    val medication =
        KbvErpMedicationBuilder.builder()
            .category(MedicationCategory.C_00) // default C_00
            .isVaccine(false) // default false
            .normgroesse(StandardSize.N1) // default NB (nicht betroffen)
            .darreichungsform(Darreichungsform.TKA) // default TAB
            .amount(5, "Stk") // default 10 {tbl}
            .pzn(pznParam, drugName)
            .build();

    val medicationRequest =
        MedicationRequestBuilder.forPatient(patient)
            .insurance(insurance)
            .requester(practitioner)
            .medication(medication)
            .dosage("1-0-0-0")
            .quantityPackages(20)
            .status("active") // default ACTIVE
            .intent("order") // default ORDER
            .isBVG(false) // Bundesversorgungsgesetz default true
            .hasEmergencyServiceFee(true) // default false
            .substitution(false) // default true
            .coPaymentStatus(StatusCoPayment.STATUS_1) // default StatusCoPayment.STATUS_0
            .build();

    val kbvBundleBuilder =
        KbvErpBundleBuilder.forPrescription(PrescriptionId.random())
            .practitioner(practitioner)
            .custodian(medicalOrganization)
            .patient(patient)
            .insurance(insurance)
            .medicationRequest(medicationRequest) // what is the medication
            .medication(medication);

    return kbvBundleBuilder.build();
  }
}
