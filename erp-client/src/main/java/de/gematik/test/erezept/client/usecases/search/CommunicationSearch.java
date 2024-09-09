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

package de.gematik.test.erezept.client.usecases.search;

import de.gematik.test.erezept.client.rest.param.IQueryParameter;
import de.gematik.test.erezept.client.rest.param.QueryParameter;
import de.gematik.test.erezept.client.rest.param.SortOrder;
import de.gematik.test.erezept.client.rest.param.SortParameter;
import de.gematik.test.erezept.client.usecases.CommunicationGetCommand;
import java.util.LinkedList;
import java.util.List;

public class CommunicationSearch {

  private CommunicationSearch() {
    throw new AssertionError();
  }

  public static Builder searchFor() {
    return new Builder();
  }

  public static CommunicationGetCommand getNewCommunications() {
    return searchFor().nonReceived().unsorted();
  }

  public static CommunicationGetCommand getNewCommunications(SortOrder order) {
    return searchFor().nonReceived().sortedBySendDate(order);
  }

  public static CommunicationGetCommand getRecipientCommunications(String id) {
    return searchFor().recipient(id).unsorted();
  }

  public static CommunicationGetCommand withAdditionalQuery(List<IQueryParameter> queryParameter) {
    return searchFor().specificQuery(queryParameter).unsorted();
  }

  public static CommunicationGetCommand getSenderCommunications(String id) {
    return searchFor().sender(id).unsorted();
  }

  public static CommunicationGetCommand getLatestCommunications() {
    return getAllCommunications(SortOrder.DESCENDING);
  }

  public static CommunicationGetCommand getLatestNewCommunications() {
    return getNewCommunications(SortOrder.DESCENDING);
  }

  public static CommunicationGetCommand getAllCommunications(SortOrder order) {
    return searchFor().sortedBySendDate(order);
  }

  public static class Builder {
    List<IQueryParameter> searchParams = new LinkedList<>();

    public Builder specificQuery(List<IQueryParameter> queryParameter) {
      searchParams.addAll(queryParameter);
      return this;
    }

    public Builder nonReceived() {
      searchParams.add(new QueryParameter("received", "NULL"));
      return this;
    }

    public Builder recipient(String id) {
      searchParams.add(new QueryParameter("recipient", id));
      return this;
    }

    public Builder sender(String id) {
      searchParams.add(new QueryParameter("sender", id));
      return this;
    }

    public CommunicationGetCommand unsorted() {
      return new CommunicationGetCommand(searchParams);
    }

    public CommunicationGetCommand sortedBySendDate(SortOrder order) {
      searchParams.add(new SortParameter("sent", order));
      return unsorted();
    }
  }
}
