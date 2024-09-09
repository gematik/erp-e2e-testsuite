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

package de.gematik.test.erezept.client.rest.param;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.fhir.values.TelematikID;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.val;
import org.apache.http.client.utils.URLEncodedUtils;
import org.hl7.fhir.r4.model.Identifier;

public interface IQueryParameter {

  String encode();

  static SearchQueryBuilder search() {
    return new SearchQueryBuilder();
  }

  String parameter();

  String value();

  static List<IQueryParameter> queryListFromUrl(String url) {
    val pathList = URLEncodedUtils.parsePathSegments(url); // get first param of Path
    val urlPathPart = pathList.get(pathList.size() - 1).split("\\?");
    if (urlPathPart.length == 1) {
      // no query parameter contained in URL
      return List.of();
    }
    val urlPathToken = urlPathPart[1];
    return Stream.of(urlPathToken.split("&"))
        .map(
            q -> {
              val paramToken = q.split("=");
              return (IQueryParameter) new QueryParameter(paramToken[0], paramToken[1]);
            })
        .toList();
  }

  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  class SearchQueryBuilder {

    private final List<IQueryParameter> searchParams = new LinkedList<>();

    public SearchQueryBuilder whenHandedOver(SearchPrefix searchPrefix, LocalDate date) {
      return withParam(new QueryParameter("whenhandedover", searchPrefix.value() + date));
    }

    public SearchQueryBuilder whenPrepared(SearchPrefix searchPrefix, LocalDate date) {
      return withParam(
          new QueryParameter(
              "whenprepared",
              format(
                  "{0}{1}", searchPrefix.value(), date.format(DateTimeFormatter.ISO_LOCAL_DATE))));
    }

    public SearchQueryBuilder fromPerformer(TelematikID telematikID) {
      return withParam(new QueryParameter("performer", telematikID.getValue()));
    }

    public SearchQueryBuilder withOffset(int offset) {
      return withParam(new QueryParameter("__offset", String.valueOf(offset)));
    }

    public SearchQueryBuilder withCount(int count) {
      return withParam(new QueryParameter("_count", String.valueOf(count)));
    }

    public SearchQueryBuilder wasSent(LocalDate localDate, SearchPrefix searchPrefix) {
      return withParam(
          new QueryParameter(
              "sent", searchPrefix.value() + localDate.format(DateTimeFormatter.ISO_LOCAL_DATE)));
    }

    public SearchQueryBuilder hasSender(String id) {
      searchParams.add(new QueryParameter("sender", id));
      return this;
    }

    public SearchQueryBuilder wasReceived(LocalDate localDate, SearchPrefix searchPrefix) {
      return withParam(
          new QueryParameter(
              "received",
              searchPrefix.value() + localDate.format(DateTimeFormatter.ISO_LOCAL_DATE)));
    }

    public SearchQueryBuilder identifier(Identifier identifier) {
      // the format of the identifier is described in A_22070
      val identifierStringValue = format("{0}|{1}", identifier.getSystem(), identifier.getValue());
      searchParams.add(new QueryParameter("identifier", identifierStringValue));
      return this;
    }

    public SearchQueryBuilder andRecipient(String id) {
      searchParams.add(new QueryParameter("recipient", id));
      return this;
    }

    public SearchQueryBuilder sortedBy(String value, SortOrder order) {
      return withParam(new SortParameter(value, order));
    }

    public SearchQueryBuilder sortedByDate(SortOrder order) {
      return sortedBy("date", order);
    }

    public SearchQueryBuilder withParam(IQueryParameter searchParam) {
      searchParams.add(searchParam);
      return this;
    }

    public SearchQueryBuilder withAuthoredOnAndFilter(LocalDate date, SearchPrefix searchPrefix) {
      return withParam(
          new QueryParameter(
              "authored-on", searchPrefix.value() + date.format(DateTimeFormatter.ISO_LOCAL_DATE)));
    }

    public List<IQueryParameter> createParameter() {
      return this.searchParams;
    }
  }
}
