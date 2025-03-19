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
 */

package de.gematik.test.erezept.primsys.model;

import de.gematik.test.erezept.client.usecases.search.CommunicationSearch;
import de.gematik.test.erezept.primsys.actors.Pharmacy;
import de.gematik.test.erezept.primsys.mapping.CommunicationDataMapper;
import de.gematik.test.erezept.primsys.rest.params.CommunicationFilterParams;
import de.gematik.test.erezept.primsys.rest.response.ErrorResponseBuilder;
import jakarta.ws.rs.core.Response;
import lombok.val;

public class GetCommunicationsUseCase {

  private final Pharmacy pharmacy;

  public GetCommunicationsUseCase(Pharmacy pharmacy) {
    this.pharmacy = pharmacy;
  }

  public Response getCommunications(CommunicationFilterParams params) {
    val searchBuilder = CommunicationSearch.searchFor();
    params.getReceiverId().ifPresent(searchBuilder::recipient);
    params.getSenderId().ifPresent(searchBuilder::sender);
    searchBuilder.sortedBySendDate(params.getSortOrder());

    val response = pharmacy.erpRequest(searchBuilder.unsorted());

    val comBundle =
        response.getExpectedOrThrow(ErrorResponseBuilder::createFachdienstErrorException);
    val dtos = comBundle.getCommunications().stream().map(CommunicationDataMapper::from).toList();
    return Response.status(response.getStatusCode()).entity(dtos).build();
  }
}
