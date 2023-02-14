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

import de.gematik.test.erezept.primsys.model.ActorContext;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
@Path("info")
public class InfoResource {

  private static final ActorContext actors = ActorContext.getInstance();

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Map<String, String> getInfo() {
    log.info("GET /info");
    val numDocs = String.valueOf(actors.getDoctors().size());
    val numPharmacies = String.valueOf(actors.getPharmacies().size());
    return Map.of("doctors", numDocs, "pharmacies", numPharmacies);
  }
}
