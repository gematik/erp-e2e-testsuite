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

package de.gematik.test.erezept.client.usecases;

import static de.gematik.test.erezept.client.rest.param.IQueryParameter.queryListFromUrl;

import de.gematik.bbriccs.rest.HttpRequestMethod;
import de.gematik.test.erezept.client.rest.param.IQueryParameter;
import java.util.List;
import java.util.Optional;
import lombok.val;
import org.apache.http.client.utils.URLEncodedUtils;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Resource;

public class BundlePagingCommand<R extends Bundle> extends BaseCommand<R> {

  private BundlePagingCommand(Class<R> expect, List<IQueryParameter> list, String fhirResource) {
    super(expect, HttpRequestMethod.GET, fhirResource);
    this.queryParameters.addAll(list);
  }

  public static <R extends Bundle> BundlePagingCommand<R> getNextFrom(R bundle) {
    return fromConcrete(bundle, "next");
  }

  public static <R extends Bundle> BundlePagingCommand<R> getSelfFrom(R bundle) {
    return fromConcrete(bundle, "self");
  }

  public static <R extends Bundle> BundlePagingCommand<R> getPreviousFrom(R bundle) {
    return fromConcrete(bundle, "prev");
  }

  @SuppressWarnings("unchecked")
  public static <R extends Bundle> BundlePagingCommand<R> fromConcrete(R bundle, String relation) {
    val urlString =
        bundle.getLink().stream()
            .filter(link -> link.getRelation().equalsIgnoreCase(relation))
            .map(Bundle.BundleLinkComponent::getUrl)
            .findFirst()
            .orElseThrow();

    val pathList = URLEncodedUtils.parsePathSegments(urlString); // get first param of Path
    val urlPathToken = pathList.get(pathList.size() - 1).split("\\?");
    val fhirResource = urlPathToken[0];
    val queryList = queryListFromUrl(urlString);
    val c = (Class<R>) bundle.getClass();
    return new BundlePagingCommand<>(c, queryList, fhirResource);
  }

  /**
   * to get the requested Bundle from Backend for a Concrete URL URL will be cut and converted
   *
   * @param bundleType The expected Type you like to get
   * @param url needs Full url
   * @return the Command
   */
  public static <R extends Bundle> BundlePagingCommand<R> fromConcreteURL(
      Class<R> bundleType, String url) {
    val pathList = URLEncodedUtils.parsePathSegments(url); // get first param of Path
    val urlPath = pathList.get(pathList.size() - 1);
    return new BundlePagingCommand<>(bundleType, List.of(), urlPath); //
  }

  @Override
  public Optional<Resource> getRequestBody() {
    return Optional.empty();
  }
}
