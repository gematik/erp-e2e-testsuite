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

package de.gematik.test.erezept.screenplay.task;

import static java.text.MessageFormat.*;

import de.gematik.test.erezept.client.exceptions.*;
import de.gematik.test.erezept.client.usecases.*;
import de.gematik.test.erezept.fhir.builder.*;
import de.gematik.test.erezept.fhir.builder.kbv.*;
import de.gematik.test.erezept.fhir.extensions.kbv.*;
import de.gematik.test.erezept.fhir.parser.*;
import de.gematik.test.erezept.fhir.parser.profiles.version.*;
import de.gematik.test.erezept.fhir.resources.erp.*;
import de.gematik.test.erezept.fhir.resources.kbv.*;
import de.gematik.test.erezept.fhir.values.*;
import de.gematik.test.erezept.fhir.valuesets.*;
import de.gematik.test.erezept.fhirdump.*;
import de.gematik.test.erezept.screenplay.abilities.*;
import de.gematik.test.erezept.screenplay.util.*;
import java.nio.file.*;
import java.util.*;
import javax.annotation.*;
import lombok.*;
import lombok.extern.slf4j.*;
import net.serenitybdd.core.*;
import net.serenitybdd.screenplay.*;
import net.serenitybdd.screenplay.Task;
import net.thucydides.core.annotations.*;
import org.hl7.fhir.r4.model.*;

@Slf4j
public class IssuePrescription implements Task {

  private final Actor patient;
  private final Actor pharmacy;
  private final List<Map<String, String>> medications;
  private final PrescriptionAssignmentKind type;
  private String patientKvnr;

  private IssuePrescription(
      @Nullable String kvnr,
      @Nullable Actor patient,
      @Nullable Actor pharmacy,
      PrescriptionAssignmentKind type,
      List<Map<String, String>> medications) {
    this.patientKvnr = kvnr;
    this.medications = medications;
    this.patient = patient;
    this.pharmacy = pharmacy;
    this.type = type;

    if (kvnr == null && patient != null) {
      this.patientKvnr = SafeAbility.getAbility(patient, ProvideEGK.class).getKvnr();
    }
  }

  public static IssuePrescriptionBuilder forKvnr(String kvnr) {
    return new IssuePrescriptionBuilder(kvnr);
  }

  public static IssuePrescriptionBuilder forPatient(Actor patient) {
    return new IssuePrescriptionBuilder(patient);
  }

  @Override
  @Step("{0} issues ePrescription with PZN #pzn to #patientKvnr")
  public <T extends Actor> void performAs(final T actor) {
    val erpClientAbility = SafeAbility.getAbility(actor, UseTheErpClient.class);
    val konnektorAbility = SafeAbility.getAbility(actor, UseTheKonnektor.class);
    val baseDataAbility = SafeAbility.getAbility(actor, ProvideDoctorBaseData.class);
    val managePrescriptions = SafeAbility.getAbility(actor, ManageDoctorsPrescriptions.class);

    // Practitioner base data (Stammdaten)
    val kbvPractitioner = baseDataAbility.getPractitioner();
    val kbvOrganization = baseDataAbility.getMedicalOrganization();
    val kbvPatient = this.getPatientBaseData();
    val kbvPatientCoverage = this.getPatientInsuranceCoverage();

    medications.forEach(
        med -> {
          // get the flowtype either from med or reason from patient's insurance kind
          val workflowType = getFlowType(med.get("Workflow"));
          // create Task in state draft
          log.info(
              format(
                  "Create Task for {0} with WorkflowType {1} ({2})",
                  type, workflowType.getCode(), workflowType.getDisplay()));
          val createCmd = new TaskCreateCommand(workflowType);
          val createResponse = erpClientAbility.request(createCmd);
          val draftTask = createResponse.getResource(createCmd.expectedResponseBody());
          val prescriptionId = draftTask.getPrescriptionId();

          log.info(
              format(
                  "Issue ePrescription with ID {0} for {1}",
                  prescriptionId.getValue(), patientKvnr));
          // create a KBV-Bundle
          val kbvBundle =
              createKbvBundle(
                  kbvPractitioner,
                  kbvOrganization,
                  kbvPatient,
                  kbvPatientCoverage,
                  prescriptionId,
                  med);

          // activate the Task
          val activeTask =
              this.activateTask(erpClientAbility, konnektorAbility, draftTask, kbvBundle);

          // store the issued prescription
          managePrescriptions.append(activeTask);

          // handover DMC if a concrete actor is available
          this.handoverDmc(activeTask);
        });
  }

  /**
   * Perform the mapping from a single DataTable-Row to fully built KBV-Bundle
   *
   * @param prescriptionId is the Prescription-ID of the Task (in state draft)
   * @param medMap is a single Row-Entry for the DataTable
   * @return a fully built KBV-Bundle
   */
  private KbvErpBundle createKbvBundle(
      Practitioner practitioner,
      MedicalOrganization organization,
      KbvPatient patient,
      Coverage insurance,
      PrescriptionId prescriptionId,
      Map<String, String> medMap) {

    // read the row of the prescription and fill up with faker values if required
    val statusKennzeichen = medMap.getOrDefault("KBV_Statuskennzeichen", "00");
    val pzn = medMap.getOrDefault("PZN", GemFaker.fakerPzn());
    val name = medMap.getOrDefault("Name", GemFaker.fakerDrugName());
    val substitution = Boolean.parseBoolean(medMap.getOrDefault("Substitution", "true"));
    val category = medMap.getOrDefault("Verordnungskategorie", "00");
    val isVaccine = Boolean.parseBoolean(medMap.getOrDefault("Impfung", "false"));
    val size = medMap.getOrDefault("Normgröße", "NB");
    val darreichungsForm = medMap.getOrDefault("Darreichungsform", "TAB");
    val darreichungsMenge = medMap.getOrDefault("Darreichungsmenge", "1");
    val dosage = medMap.getOrDefault("Dosierung", "1-0-0-0");
    val amount = medMap.getOrDefault("Menge", "1");
    val emergencyServiceFee = Boolean.parseBoolean(medMap.getOrDefault("Notdiensgebühr", "false"));
    val paymentStatus = medMap.getOrDefault("Zahlungsstatus", "0");

    MultiplePrescriptionExtension mvo;
    val isMvo = Boolean.parseBoolean(medMap.getOrDefault("MVO", "false"));
    if (isMvo) {
      val denominator = Integer.parseInt(medMap.getOrDefault("Denominator", "4"));
      val numerator = Integer.parseInt(medMap.getOrDefault("Numerator", "1"));

      val mvoBuilder = MultiplePrescriptionExtension.asMultiple(numerator, denominator);

      val start = medMap.getOrDefault("Gueltigkeitsstart", "leer");
      if (!start.equalsIgnoreCase("leer")) {
        mvoBuilder.starting(Integer.parseInt(start));
      }

      val end = medMap.getOrDefault("Gueltigkeitsende", "leer");
      if (!end.equalsIgnoreCase("leer")) {
        mvo = mvoBuilder.validForDays(Integer.parseInt(end), false);
      } else {
        mvo = mvoBuilder.withoutEndDate(false);
      }
    } else {
      mvo = MultiplePrescriptionExtension.asNonMultiple();
    }

    // create the medication
    val medication =
        KbvErpMedicationBuilder.builder()
            .category(MedicationCategory.fromCode(category))
            .isVaccine(isVaccine)
            .normgroesse(StandardSize.fromCode(size))
            .darreichungsform(Darreichungsform.fromCode(darreichungsForm))
            .amount(Integer.decode(darreichungsMenge))
            .pzn(pzn, name)
            .build();

    val medicationRequest =
        MedicationRequestBuilder.forPatient(patient)
            .insurance(insurance)
            .requester(practitioner)
            .medication(medication)
            .dosage(dosage)
            .quantityPackages(Integer.decode(amount))
            .status("active")
            .intent("order")
            .isBVG(false)
            .mvo(mvo)
            .hasEmergencyServiceFee(emergencyServiceFee)
            .substitution(substitution)
            .coPaymentStatus(StatusCoPayment.fromCode(paymentStatus))
            .build();

    // create and return the KBV Bundle
    val kbvBuilder =
        KbvErpBundleBuilder.forPrescription(prescriptionId)
            .statusKennzeichen(statusKennzeichen)
            .practitioner(practitioner)
            .custodian(organization)
            .patient(patient)
            .insurance(insurance)
            .medicationRequest(medicationRequest) // what is the medication
            .medication(medication);

    if (patient.getInsuranceKind() == VersicherungsArtDeBasis.PKV) {
      // assigner organization was only required in KbvItaFor 1.0.3
      if (KbvItaForVersion.getDefaultVersion().compareTo(KbvItaForVersion.V1_0_3) == 0) {
        // for now, we do not have the AssignerOrganization (which was faked anyway for getting a
        // Reference + Name
        // build a faked one matching the Reference of the patient
        val fakedAssignerOrganization = AssignerOrganizationBuilder.faker(patient);
        kbvBuilder.assigner(fakedAssignerOrganization.build());
      }
    }

    return kbvBuilder.build();
  }

  /**
   * Create a draft task and activate with the values from given Map
   *
   * @param clientAbility is the ability to communication with the eRP-FD
   * @param konnektorAbility is the ability to sign the KBV Bundle
   * @return an activated Task
   */
  private ErxTask activateTask(
      UseTheErpClient clientAbility,
      UseTheKonnektor konnektorAbility,
      ErxTask draftTask,
      KbvErpBundle kbvBundle) {
    // create a draft Task
    val taskId = draftTask.getUnqualifiedId();
    val accessCode = draftTask.getOptionalAccessCode().orElseThrow();

    val kbvXml = clientAbility.encode(kbvBundle, EncodingType.XML);
    val signedKbv = konnektorAbility.signDocumentWithHba(kbvXml).getPayload();

    // activate the task
    val activate = new TaskActivateCommand(taskId, accessCode, signedKbv);
    val activateResponse = clientAbility.request(activate);
    val expectedBodyType = activate.expectedResponseBody();
    return activateResponse
        .getResourceOptional(expectedBodyType)
        .orElseThrow(
            () ->
                new AssertionError(
                    new UnexpectedResponseResourceError(
                        expectedBodyType, activateResponse.getResource())));
  }

  private KbvPatient getPatientBaseData() {
    KbvPatient ret;
    if (patient != null) {
      val baseData = SafeAbility.getAbility(patient, ProvidePatientBaseData.class);
      log.info(
          format(
              "Issue ePrescription to patient {0} with insurance type {1}",
              baseData.getFullName(), baseData.getVersicherungsArt()));
      ret = baseData.getPatient();
    } else {
      ret = PatientBuilder.faker(patientKvnr, IdentifierTypeDe.GKV).build();
      log.info(
          format(
              "Issue ePrescription to KVNR {0} with insurance type {1}",
              patientKvnr, ret.getInsuranceKind()));
    }
    return ret;
  }

  /**
   * The PrescriptionFlowType can be reasoned from the insurance kind (VersicherungsArtDeBasis) and
   * the PrescriptionAssignementKind. However, if a different WorkflowType is given via the
   * DataTable as the code, this one will overwrite the reasoned one.
   *
   * @param code is the String representation of the WorkflowType
   * @return a PrescriptionFlowType which was reasoned from Data; if provided via code (code !=
   *     null) then the PrescriptionFlowType from this code
   */
  private PrescriptionFlowType getFlowType(@Nullable String code) {
    VersicherungsArtDeBasis insuranceKind = VersicherungsArtDeBasis.GKV;
    if (patient != null) {
      val baseData = SafeAbility.getAbility(patient, ProvidePatientBaseData.class);
      insuranceKind = baseData.getVersicherungsArt();
    }

    return FlowTypeUtil.getFlowType(code, insuranceKind, type);
  }

  private KbvCoverage getPatientInsuranceCoverage() {
    KbvCoverage ret;
    if (patient != null) {
      ret = SafeAbility.getAbility(patient, ProvidePatientBaseData.class).getInsuranceCoverage();
    } else {
      ret = KbvCoverageBuilder.faker(VersicherungsArtDeBasis.GKV).build();
    }
    return ret;
  }

  /**
   * If this Task was created with a concrete Patient and not only with a KVNR, the activated Task
   * will be transformed to a DmcPrescription (Data Matrix Code) and handed over to this patient
   *
   * <p>If the Task was create with PrescriptionAssignmentKind.DIRECT_ASSIGNMENT and a pharmacy was
   * given handover the DMC to the pharmacy (Direktzuweisung)
   *
   * @param activatedTask is the Task which represents the E-Rezept
   */
  private void handoverDmc(ErxTask activatedTask) {
    val dmc =
        DmcPrescription.ownerDmc(activatedTask.getUnqualifiedId(), activatedTask.getAccessCode());
    writeDmcToReport(dmc);

    // if we have the not only the KVRN but also the actor, give him also a DMC Prescription
    if (patient != null) {
      log.info(format("Doctor hands over DMC for {0} to {1}", dmc.getTaskId(), patient.getName()));
      SafeAbility.getAbility(patient, ManageDataMatrixCodes.class).appendDmc(dmc);
    } else {
      log.info(format("No concrete Actor was given to handover the DMC for {0}", dmc.getTaskId()));
    }

    if (pharmacy != null) {
      if (type.equals(PrescriptionAssignmentKind.DIRECT_ASSIGNMENT)) {
        SafeAbility.getAbility(pharmacy, ManagePharmacyPrescriptions.class)
            .appendAssignedPrescription(dmc);
      } else {
        log.error(
            format(
                "The pharmacy {0} was given for direct assignment but assignment is of type {1}",
                pharmacy.getName(), type));
      }
    }
  }

  @SneakyThrows
  private void writeDmcToReport(DmcPrescription dmc) {
    // write the DMC to file and append to the Serenity Report
    val dmcPath =
        Path.of("target", "site", "serenity", "dmcs", format("dmc_{0}.png", dmc.getTaskId()));
    val bitMatrix = DataMatrixCodeGenerator.generateDmc(dmc.getTaskId(), dmc.getAccessCode());
    DataMatrixCodeGenerator.writeToFile(bitMatrix, dmcPath.toFile());

    Serenity.recordReportData()
        .withTitle("Data Matrix Code for " + dmc.getTaskId())
        .downloadable()
        .fromFile(dmcPath);

    FhirDumper.getInstance()
        .writeDump(
            format(
                "DMC for {0} with AccessCode {1}", dmc.getTaskId(), dmc.getAccessCode().getValue()),
            format("dmc_{0}.png", dmc.getTaskId()),
            file -> DataMatrixCodeGenerator.writeToFile(bitMatrix, file));
  }

  public static class IssuePrescriptionBuilder {
    private String kvnr;
    private Actor patient;
    private Actor pharmacy;
    private PrescriptionAssignmentKind type =
        PrescriptionAssignmentKind.PHARMACY_ONLY; // as default

    private IssuePrescriptionBuilder(String kvnr) {
      this.kvnr = kvnr;
    }

    private IssuePrescriptionBuilder(Actor patient) {
      this.patient = patient;
    }

    public IssuePrescriptionBuilder as(PrescriptionAssignmentKind type) {
      this.type = type;
      return this;
    }

    public IssuePrescriptionBuilder to(Actor pharmacy) {
      this.pharmacy = pharmacy;
      return this;
    }

    public IssuePrescription from(List<Map<String, String>> medications) {
      return new IssuePrescription(kvnr, patient, pharmacy, type, medications);
    }

    public IssuePrescription randomPrescription() {
      // concrete content of does not matter; we just simply need a single entry to issue a single
      // random prescription
      val medications = List.of(Map.of("prescription", "1"));
      return from(medications);
    }
  }
}
