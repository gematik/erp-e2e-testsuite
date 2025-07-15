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

package de.gematik.test.erezept.primsys.controller;

import de.gematik.bbriccs.fhir.EncodingType;
import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.test.erezept.fhir.builder.erp.ErxMedicationDispenseBundleBuilder;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
import de.gematik.test.erezept.primsys.data.DiGADispensedMedicationDto;
import de.gematik.test.erezept.primsys.data.PrescribeEvdgaRequestDto;
import de.gematik.test.erezept.primsys.data.PrescribeRequestDto;
import de.gematik.test.erezept.primsys.data.PznDispensedMedicationDto;
import de.gematik.test.erezept.primsys.mapping.DiGADispensedMedicationDataMapper;
import de.gematik.test.erezept.primsys.mapping.PrescribeEvdgaRequestDataMapper;
import de.gematik.test.erezept.primsys.mapping.PrescribeRequestDataMapper;
import de.gematik.test.erezept.primsys.mapping.PznDispensedMedicationDataMapper;
import de.gematik.test.erezept.primsys.model.ActorContext;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.r4.model.Resource;

@Slf4j
@Path("faker")
public class FakerController {

  static ActorContext actors = ActorContext.getInstance();

  @POST
  @Path("doc/kbvbundle")
  @Produces(MediaType.APPLICATION_XML)
  @Consumes(MediaType.APPLICATION_JSON)
  public Response createKbvBundle(
      @DefaultValue("false") @QueryParam("direct") boolean isDirectAssignment,
      PrescribeRequestDto body) {
    val doctorId = actors.getDoctors().get(0).getIdentifier();
    return createKbvBundle(doctorId, isDirectAssignment, body);
  }

  @POST
  @Path("doc/{doctorId}/kbvbundle")
  @Produces(MediaType.APPLICATION_XML)
  @Consumes(MediaType.APPLICATION_JSON)
  public Response createKbvBundle(
      @PathParam("doctorId") String doctorId,
      @DefaultValue("false") @QueryParam("direct") boolean isDirectAssignment,
      PrescribeRequestDto body) {
    val doctor = actors.getDoctorOrThrowNotFound(doctorId);
    log.info("Doctor {} create faked KBV-Bundle", doctor.getName());

    val bodyMapper = PrescribeRequestDataMapper.from(body);
    val kbvBundle = bodyMapper.createKbvBundle(doctor.getName());
    val insuranceKind = kbvBundle.getCoverage().getInsuranceKind();
    val flowType = PrescriptionFlowType.fromInsuranceKind(insuranceKind, isDirectAssignment);
    val prescriptionId = PrescriptionId.random(flowType);
    kbvBundle.setPrescriptionId(prescriptionId);

    val xml = doctor.encode(kbvBundle, EncodingType.XML);
    return Response.ok(xml).build();
  }

  @POST
  @Path("doc/evdgabundle")
  @Produces(MediaType.APPLICATION_XML)
  @Consumes(MediaType.APPLICATION_JSON)
  public Response createEvdgaBundle(PrescribeEvdgaRequestDto body) {
    val doctorId = actors.getDoctors().get(0).getIdentifier();
    return createEvdgaBundle(doctorId, body);
  }

  @POST
  @Path("doc/{doctorId}/evdgabundle")
  @Produces(MediaType.APPLICATION_XML)
  @Consumes(MediaType.APPLICATION_JSON)
  public Response createEvdgaBundle(
      @PathParam("doctorId") String doctorId, PrescribeEvdgaRequestDto body) {
    val doctor = actors.getDoctorOrThrowNotFound(doctorId);
    log.info("Doctor {} creates faked EVDGA-Bundle", doctor.getName());

    val bodyMapper = PrescribeEvdgaRequestDataMapper.from(body);
    val evdgaBundle = bodyMapper.createEvdgaBundle(doctor.getName());
    val xml = doctor.encode(evdgaBundle, EncodingType.XML);
    return Response.ok(xml).build();
  }

  @POST
  @Path("pharm/pznmedicationdispense")
  @Produces(MediaType.APPLICATION_XML)
  @Consumes(MediaType.APPLICATION_JSON)
  public Response createPznMedicationDispense(
      @QueryParam("kvnr") String kvnrValue,
      @QueryParam("prescriptionId") String pid,
      @QueryParam("substitution") boolean substitution,
      List<PznDispensedMedicationDto> body) {
    val pharmacyId = actors.getPharmacies().get(0).getIdentifier();
    return createPznMedicationDispense(pharmacyId, kvnrValue, pid, substitution, body);
  }

  @POST
  @Path("pharm/{pharmacyId}/pznmedicationdispense")
  @Produces(MediaType.APPLICATION_XML)
  @Consumes(MediaType.APPLICATION_JSON)
  public Response createPznMedicationDispense(
      @PathParam("pharmacyId") String pharmacyId,
      @QueryParam("kvnr") String kvnrValue,
      @QueryParam("prescriptionId") String pid,
      @QueryParam("substitution") boolean substitution,
      List<PznDispensedMedicationDto> body) {
    val pharmacy = actors.getPharmacyOrThrowNotFound(pharmacyId);
    log.info("Pharmacy {} creates faked PZN MedicationDispense", pharmacy.getName());

    if (body == null || body.isEmpty()) {
      body = List.of(PznDispensedMedicationDataMapper.randomDto());
    }
    val kvnr = getKvnr(kvnrValue);
    val prescriptionId = getPrescriptionId(pid);
    val telematikId = pharmacy.getSmcb().getTelematikId();

    val erxMedicationDispenses =
        body.stream()
            .map(
                dto ->
                    PznDispensedMedicationDataMapper.from(
                            dto, kvnr, prescriptionId, telematikId, substitution)
                        .convert())
            .toList();

    Resource faked;
    if (erxMedicationDispenses.size() == 1) {
      faked = erxMedicationDispenses.get(0);
    } else {
      faked = ErxMedicationDispenseBundleBuilder.of(erxMedicationDispenses).build();
    }

    val xml = pharmacy.encode(faked, EncodingType.XML);
    return Response.ok(xml).build();
  }

  @POST
  @Path("pharm/digamedicationdispense")
  @Produces(MediaType.APPLICATION_XML)
  @Consumes(MediaType.APPLICATION_JSON)
  public Response createDiGAMedicationDispense(
      @QueryParam("kvnr") String kvnrValue,
      @QueryParam("prescriptionId") String pid,
      DiGADispensedMedicationDto body) {
    val pharmacyId = actors.getPharmacies().get(0).getIdentifier();
    return createDiGAMedicationDispense(pharmacyId, kvnrValue, pid, body);
  }

  @POST
  @Path("pharm/{pharmacyId}/digamedicationdispense")
  @Produces(MediaType.APPLICATION_XML)
  @Consumes(MediaType.APPLICATION_JSON)
  public Response createDiGAMedicationDispense(
      @PathParam("pharmacyId") String pharmacyId,
      @QueryParam("kvnr") String kvnrValue,
      @QueryParam("prescriptionId") String pid,
      DiGADispensedMedicationDto body) {
    val pharmacy = actors.getPharmacyOrThrowNotFound(pharmacyId);
    log.info("Pharmacy {} creates faked DiGA MedicationDispense", pharmacy.getName());

    if (body == null) {
      body = DiGADispensedMedicationDataMapper.randomDto();
    }
    val kvnr = getKvnr(kvnrValue);
    val prescriptionId = getPrescriptionId(pid);
    val telematikId = pharmacy.getSmcb().getTelematikId();

    val digaMedicationDispense =
        DiGADispensedMedicationDataMapper.from(body, kvnr, prescriptionId, telematikId).convert();
    val xml = pharmacy.encode(digaMedicationDispense, EncodingType.XML);
    return Response.ok(xml).build();
  }

  private static KVNR getKvnr(String kvnrValue) {
    return Optional.ofNullable(kvnrValue).map(KVNR::from).orElseGet(KVNR::random);
  }

  private static PrescriptionId getPrescriptionId(String pid) {
    return Optional.ofNullable(pid).map(PrescriptionId::from).orElseGet(PrescriptionId::random);
  }
}
