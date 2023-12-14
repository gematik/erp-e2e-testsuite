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

package de.gematik.test.erezept.cli.cmd.generate;

import static java.text.MessageFormat.*;

import de.gematik.test.erezept.cli.cmd.generate.param.*;
import de.gematik.test.erezept.fhir.builder.*;
import de.gematik.test.erezept.fhir.builder.kbv.*;
import de.gematik.test.erezept.fhir.resources.kbv.*;
import de.gematik.test.erezept.fhir.values.*;
import de.gematik.test.erezept.fhir.valuesets.*;
import de.gematik.test.fuzzing.kbv.*;
import lombok.*;
import lombok.extern.slf4j.*;
import picocli.CommandLine.*;

@Slf4j
@Command(
    name = "kbvbundle",
    description = "generate exemplary KBV-Bundle FHIR Resources",
    mixinStandardHelpOptions = true)
public class KbvBundleGenerator extends BaseResourceGenerator {

  @Mixin private PractitionerParameter practitionerParam;
  @Mixin private MedicalOrganizationParameter medicalOrganizationParam;
  @Mixin private PatientParameter patientParam;
  @Mixin private InsuranceCoverageParameter coverageParam;
  @Mixin private MedicationParameter medicationParam;
  @Mixin private MedicationRequestParameter medReqParam;

  @Override
  public Integer call() throws Exception {
    return this.create(
        "KBV",
        this::createPrescription,
        KbvBundleManipulatorFactory.getAllKbvBundleManipulators(medReqParam.isMvoOnly()),
        original -> {
          val copy = new KbvErpBundle();
          original.copyValues(copy);
          return copy;
        });
  }

  private KbvErpBundle createPrescription() {
    val practitioner = practitionerParam.createPractitioner();
    if (!medicalOrganizationParam.hasCLIOrganizationName()) {
      val medOrgName =
          format(
              "{0} {1}",
              practitionerParam.getQualificationType().getDisplay(),
              practitionerParam.getFullName().getLastName());
      medicalOrganizationParam.changeOrganizationName(medOrgName);
    }

    val medicalOrganization = medicalOrganizationParam.createMedicalOrganization();
    val patient = patientParam.createPatient();
    coverageParam.setPatient(patient);
    val insurance = coverageParam.createCoverage();
    val medication = medicationParam.createMedication();
    medReqParam.setInsurance(insurance);
    medReqParam.setMedication(medication);
    medReqParam.setPractitioner(practitioner);
    medReqParam.setPatient(patient);
    val medicationRequest = medReqParam.createMedicationRequest();

    val flowType =
        patientParam.getKvnrParameter().getInsuranceType() == VersicherungsArtDeBasis.GKV
            ? GemFaker.randomElement(
                PrescriptionFlowType.FLOW_TYPE_160, PrescriptionFlowType.FLOW_TYPE_169)
            : PrescriptionFlowType.FLOW_TYPE_200;
    val prescriptionId = PrescriptionId.random(flowType);
    val kbvBundleBuilder =
        KbvErpBundleBuilder.forPrescription(prescriptionId)
            .practitioner(practitioner)
            .custodian(medicalOrganization)
            .patient(patient)
            .insurance(insurance)
            .assigner(patientParam.getAssignerOrganization()) // will be used only for PKV patients
            .medicationRequest(medicationRequest)
            .medication(medication);

    return kbvBundleBuilder.build();
  }
}
