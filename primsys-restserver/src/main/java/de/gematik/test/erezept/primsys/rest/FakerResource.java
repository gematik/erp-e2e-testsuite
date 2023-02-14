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

package de.gematik.test.erezept.primsys.rest;

import de.gematik.test.erezept.primsys.rest.data.CoverageData;
import de.gematik.test.erezept.primsys.rest.data.MedicationData;
import de.gematik.test.erezept.primsys.rest.data.PatientData;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Path("faker")
public class FakerResource {

  @GET
  @Path("patient")
  @Produces(MediaType.APPLICATION_JSON)
  public PatientData getFakePatient() {
    log.info("GET random patient data from faker");
    return PatientData.create();
  }

  @GET
  @Path("coverage")
  @Produces(MediaType.APPLICATION_JSON)
  public CoverageData getFakeCoverage() {
    log.info("GET random coverage data from faker");
    return CoverageData.create();
  }

  @GET
  @Path("medication")
  @Produces(MediaType.APPLICATION_JSON)
  public MedicationData getFakeMedication() {
    log.info("GET random medication data from faker");
    return MedicationData.create();
  }
}
