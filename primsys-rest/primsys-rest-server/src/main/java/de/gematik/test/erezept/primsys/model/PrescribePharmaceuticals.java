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

package de.gematik.test.erezept.primsys.model;

import de.gematik.bbriccs.fhir.de.valueset.InsuranceTypeDe;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpBundle;
import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
import de.gematik.test.erezept.primsys.actors.Doctor;
import de.gematik.test.erezept.primsys.data.PatientDto;
import de.gematik.test.erezept.primsys.data.PrescribeRequestDto;
import de.gematik.test.erezept.primsys.data.PrescriptionDto;
import de.gematik.test.erezept.primsys.data.PrescriptionDto.PrescriptionDtoBuilder;
import de.gematik.test.erezept.primsys.mapping.KbvPznMedicationDataMapper;
import de.gematik.test.erezept.primsys.mapping.MedicationRequestDataMapper;
import de.gematik.test.erezept.primsys.mapping.PrescribeRequestDataMapper;
import de.gematik.test.erezept.primsys.rest.response.ErrorResponseBuilder;
import jakarta.ws.rs.core.Response;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
public class PrescribePharmaceuticals extends PrescribeUseCase<KbvErpBundle> {

  private final boolean isDirectAssignment;

  private PrescribePharmaceuticals(Doctor doctor, boolean isDirectAssignment) {
    super(doctor);
    this.isDirectAssignment = isDirectAssignment;
  }

  public static Builder as(Doctor doctor) {
    return new Builder(doctor);
  }

  public Response withKbvBundle(String kbvBundleXmlBody) {
    val kbvBundle = doctor.decode(KbvErpBundle.class, kbvBundleXmlBody);
    return withKbvBundle(kbvBundle);
  }

  public Response withDto(PrescribeRequestDto body) {
    val kvnr = Optional.ofNullable(body.getPatient()).map(PatientDto::getKvnr).orElse(null);
    if (kvnr == null) {
      throw ErrorResponseBuilder.createInternalErrorException(
          400, "KVNR is required field for the body");
    }

    val bodyMapper = PrescribeRequestDataMapper.from(body);
    val kbvBundle = bodyMapper.createKbvBundle(doctor.getName());
    return withKbvBundle(kbvBundle);
  }

  public Response withKbvBundle(KbvErpBundle kbvBundle) {
    val insuranceKind =
        kbvBundle.getCoverage().getInsuranceKindOptional().orElse(InsuranceTypeDe.GKV);
    val flowType = PrescriptionFlowType.fromInsuranceKind(insuranceKind, isDirectAssignment);

    val prescriptionData = this.prescribeFor(kbvBundle, flowType);
    return Response.accepted(prescriptionData).build();
  }

  @Override
  protected PrescriptionDto finalise(KbvErpBundle bundle, PrescriptionDtoBuilder builder) {
    val medicationMapper = KbvPznMedicationDataMapper.from(bundle.getMedication());
    val medicationRequestMapper =
        MedicationRequestDataMapper.from(bundle.getMedicationRequest())
            .requestedBy(bundle.getPractitioner())
            .requestedFor(bundle.getPatient())
            .coveredBy(bundle.getCoverage())
            .forMedication(bundle.getMedication());
    return builder
        .medication(medicationMapper.getDto())
        .medicationRequest(medicationRequestMapper.getDto())
        .build();
  }

  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  public static class Builder {
    private final Doctor doctor;

    public PrescribePharmaceuticals asDirectAssignment() {
      return assignDirectly(true);
    }

    public PrescribePharmaceuticals asNormalAssignment() {
      return assignDirectly(false);
    }

    public PrescribePharmaceuticals assignDirectly(boolean isDirectAssignment) {
      return new PrescribePharmaceuticals(doctor, isDirectAssignment);
    }
  }
}
