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

package de.gematik.test.core.expectations.verifier;

import static de.gematik.test.erezept.client.rest.param.IQueryParameter.queryListFromUrl;
import static java.text.MessageFormat.format;

import de.gematik.test.core.expectations.requirements.ErpAfos;
import de.gematik.test.erezept.client.rest.param.IQueryParameter;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import lombok.val;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Resource;

public class GenericBundleVerifier {

  private GenericBundleVerifier() {
    throw new AssertionError("do not instantiate!");
  }

  public static <T extends Bundle> VerificationStep<T> containsEntriesOfCount(int count) {
    Predicate<T> predicate = bundle -> bundle.getEntry().size() == count;
    return new VerificationStep.StepBuilder<T>(
            ErpAfos.A_24441, format("Die enthaltene Anzahl an Entries muss {0} sein.", count))
        .predicate(predicate)
        .accept();
  }

  public static <T extends Bundle> VerificationStep<T> minimumCountOfEntriesOf(int count) {
    Predicate<T> predicate = bundle -> bundle.getEntry().size() >= count;
    return new VerificationStep.StepBuilder<T>(
            ErpAfos.A_24441,
            format("Die enthaltene Anzahl an Entries muss mindestens {0} sein.", count))
        .predicate(predicate)
        .accept();
  }

  public static <T extends Bundle> VerificationStep<T> containsTotalCountOf(int totalCount) {
    Predicate<T> predicate = bundle -> bundle.getTotal() == totalCount;
    return new VerificationStep.StepBuilder<T>(
            ErpAfos.A_24443, format("Die enthaltene Angabe in Total muss {0} sein", totalCount))
        .predicate(predicate)
        .accept();
  }

  public static <T extends Bundle> VerificationStep<T> expectedParamsIn(
      String relation, String queryKey, String queryValue) {
    Predicate<T> predicate =
        bundle -> {
          val queryList =
              bundle.getLink().stream()
                  .filter(link -> link.getRelation().equalsIgnoreCase(relation))
                  .map(Bundle.BundleLinkComponent::getUrl)
                  .findFirst()
                  .map(u -> queryListFromUrl(u))
                  .orElse(List.of());
          return queryList.stream()
              .filter(qp -> qp.parameter().equals(queryKey))
              .map(IQueryParameter::value)
              .findFirst()
              .map(resultValue -> resultValue.equals(queryValue))
              .orElse(false);
        };
    return new VerificationStep.StepBuilder<T>(
            ErpAfos.A_24444,
            format(
                "Die enthaltenen Link-Relationen:{0} muss für den Key {1} das Value: {2}"
                    + " enthalten",
                relation, queryKey, queryValue))
        .predicate(predicate)
        .accept();
  }

  public static <T extends Bundle> VerificationStep<T> containsCountOfGivenLinks(
      List<String> possibleLinks, long count) {
    Predicate<T> predicate =
        bundle ->
            bundle.getLink().stream()
                    .map(link -> link.getRelation().toLowerCase())
                    .filter(possibleLinks::contains)
                    .count()
                == count;
    return new VerificationStep.StepBuilder<T>(
            ErpAfos.A_24443,
            format(
                "Es müssen {1} Link-Relation aus der Liste {0} enthaltenen sein.",
                possibleLinks, count))
        .predicate(predicate)
        .accept();
  }

  public static <T extends Bundle> VerificationStep<T> containsAll5Links() {
    return containsCountOfGivenLinks(List.of("next", "prev", "self", "first", "last"), 5L);
  }

  public static <T extends Bundle> VerificationStep<T> hasElementAtPosition(
      Resource expectedElement, int position) {
    Predicate<T> predicate =
        bundle ->
            Optional.ofNullable(bundle.getEntry().get(position))
                .map(entry -> entry.getResource().getIdPart().equals(expectedElement.getIdPart()))
                .orElse(false);
    return new VerificationStep.StepBuilder<T>(
            ErpAfos.A_24441.getRequirement(),
            format(
                "Die übergebenen SearchBundle muss an Position {0} ein Element mit Id"
                    + " {1} enthalten",
                position, expectedElement.getId()))
        .predicate(predicate)
        .accept();
  }

  /**
   * the verifier compares the Ids of the contained and given EntryIds and the length
   *
   * @param compareBundle
   * @return boolean
   */
  public static <T extends Bundle> VerificationStep<T> hasSameEntryIds(
      Bundle compareBundle, ErpAfos erpAfos) {
    Predicate<T> predicate =
        bundle -> {
          val actualIds =
              bundle.getEntry().stream()
                  .map(entry -> entry.getResource().getId())
                  .sorted()
                  .toList();
          val expectedIds =
              compareBundle.getEntry().stream()
                  .map(entry -> entry.getResource().getId())
                  .sorted()
                  .toList();
          return expectedIds.equals(actualIds);
        };

    return new VerificationStep.StepBuilder<T>(
            erpAfos.getRequirement(), "Die übergebenen Bundles Entries müssen übereinstimmen")
        .predicate(predicate)
        .accept();
  }
}
