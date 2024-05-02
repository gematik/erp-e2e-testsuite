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

package de.gematik.test.erezept.client.usecases.search;

import de.gematik.test.erezept.client.rest.param.IQueryParameter;
import de.gematik.test.erezept.client.rest.param.QueryParameter;
import de.gematik.test.erezept.client.rest.param.SortOrder;
import de.gematik.test.erezept.client.rest.param.SortParameter;
import de.gematik.test.erezept.client.usecases.CommunicationGetCommand;
import java.util.ArrayList;
import lombok.val;

public class CommunicationSearch {

  private CommunicationSearch() {
    throw new AssertionError();
  }

  public static CommunicationGetCommand getNewCommunications() {
    val searchParams = new ArrayList<IQueryParameter>();
    searchParams.add(new QueryParameter("received", "NULL"));
    return new CommunicationGetCommand(searchParams);
  }

  public static CommunicationGetCommand getNewCommunications(SortOrder order) {
    val searchParams = new ArrayList<IQueryParameter>();
    searchParams.add(new QueryParameter("received", "NULL"));
    searchParams.add(new SortParameter("sent", order));
    return new CommunicationGetCommand(searchParams);
  }

  public static CommunicationGetCommand getRecipientCommunications(String id) {
    val searchParams = new ArrayList<IQueryParameter>();
    searchParams.add(new QueryParameter("recipient", id));
    return new CommunicationGetCommand(searchParams);
  }

  public static CommunicationGetCommand getSenderCommunications(String id) {
    val searchParams = new ArrayList<IQueryParameter>();
    searchParams.add(new QueryParameter("sender", id));
    return new CommunicationGetCommand(searchParams);
  }

  public static CommunicationGetCommand getLatestCommunications() {
    return getAllCommunications(SortOrder.DESCENDING);
  }

  public static CommunicationGetCommand getLatestNewCommunications() {
    return getNewCommunications(SortOrder.DESCENDING);
  }

  public static CommunicationGetCommand getAllCommunications(SortOrder order) {
    val searchParams = new ArrayList<IQueryParameter>();
    searchParams.add(new SortParameter("sent", order));
    return new CommunicationGetCommand(searchParams);
  }
}
