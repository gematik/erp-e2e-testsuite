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

package de.gematik.test.erezept.primsys.model;

import de.gematik.test.erezept.fhir.resources.kbv.KbvEvdgaBundle;
import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
import de.gematik.test.erezept.primsys.actors.Doctor;
import de.gematik.test.erezept.primsys.data.PrescriptionDto;
import de.gematik.test.erezept.primsys.data.PrescriptionDto.PrescriptionDtoBuilder;
import de.gematik.test.erezept.primsys.mapping.HealthAppRequestDataMapper;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
public class PrescribeDiGa extends PrescribeUseCase<KbvEvdgaBundle> {

  private PrescribeDiGa(Doctor doctor) {
    super(doctor);
  }

  public static PrescribeDiGa as(Doctor doctor) {
    return new PrescribeDiGa(doctor);
  }

  public Response withEvdga(String kbvBundleXmlBody) {
    val evdgaBundle = doctor.decode(KbvEvdgaBundle.class, kbvBundleXmlBody);
    return withEvdga(evdgaBundle);
  }

  public Response withEvdga(KbvEvdgaBundle evdgaBundle) {
    val prescriptionData = this.prescribeFor(evdgaBundle, PrescriptionFlowType.FLOW_TYPE_162);

    return Response.accepted(prescriptionData).build();
  }

  @Override
  protected PrescriptionDto finalise(KbvEvdgaBundle bundle, PrescriptionDtoBuilder builder) {
    val healthAppRequestMapper = HealthAppRequestDataMapper.from(bundle.getHealthAppRequest());
    return builder.healthAppRequest(healthAppRequestMapper.build().getDto()).build();
  }
}
