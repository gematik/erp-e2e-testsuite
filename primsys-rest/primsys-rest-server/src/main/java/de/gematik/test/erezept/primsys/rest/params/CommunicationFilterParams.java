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

package de.gematik.test.erezept.primsys.rest.params;

import de.gematik.test.erezept.client.rest.param.SortOrder;
import jakarta.ws.rs.QueryParam;
import java.util.Optional;

public class CommunicationFilterParams {
  @QueryParam("sender")
  private String senderId;

  @QueryParam("receiver")
  private String receiverId;

  @QueryParam("sort")
  private String sort;

  public Optional<String> getSenderId() {
    return Optional.ofNullable(this.senderId);
  }

  public Optional<String> getReceiverId() {
    return Optional.ofNullable(this.receiverId);
  }

  public SortOrder getSortOrder() {
    return Optional.ofNullable(this.sort)
        .map(
            so ->
                switch (so.toLowerCase()) {
                  case "ascending", "lifo", "oldest":
                    yield SortOrder.ASCENDING;
                  default:
                    yield SortOrder.DESCENDING;
                })
        .orElse(SortOrder.DESCENDING);
  }
}
