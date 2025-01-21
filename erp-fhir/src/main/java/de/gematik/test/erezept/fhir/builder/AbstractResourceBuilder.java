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

package de.gematik.test.erezept.fhir.builder;

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.fhir.builder.exceptions.BuilderException;
import de.gematik.test.erezept.fhir.valuesets.IValueSet;
import java.util.List;
import java.util.UUID;
import lombok.val;

public class AbstractResourceBuilder<B extends AbstractResourceBuilder<B>> {

  private String resourceId;

  public B setResourceId(String resourceId) {
    this.resourceId = resourceId;
    return self();
  }

  /**
   * The resource ID is always required but does not necessarily need to be provided by the user. In
   * case the user didn't provide one, generate a random UUID
   *
   * @return Resource ID provided by user or a randomly generated one if no ID was provided
   */
  public String getResourceId() {
    if (this.resourceId == null) {
      this.resourceId = UUID.randomUUID().toString();
    }
    return resourceId;
  }

  @SuppressWarnings("unchecked")
  public final B self() {
    return (B) this;
  }

  /**
   * This method works similar to Objects.requireNonNull but instead of throwing a
   * NullPointerException this one will throw BuilderException with a message supplied by the
   * errorMsgSupplier
   *
   * @param obj is the object which will be checked for Null
   * @param errorMsg will be shown in the BuilderException in case of an Error
   * @param <T> is the generic type of obj
   */
  protected final <T> void checkRequired(T obj, String errorMsg) {
    if (obj == null) {
      val prefixedErrorMsg = format("Missing required property: {0}", errorMsg);
      throw new BuilderException(prefixedErrorMsg);
    }
  }

  protected final <T> void checkRequiredOneOfTwoNotBoth(T obj1, T obj2, String errorMsg) {
    if (obj1 == null && obj2 == null) {
      val prefixedErrorMsg = format("Missing required property: {0}", errorMsg);
      throw new BuilderException(prefixedErrorMsg);
    }
    if (obj1 != null && obj2 != null) {
      val prefixedErrorMsg = format("to much properties, one is Max: {0}", errorMsg);
      throw new BuilderException(prefixedErrorMsg);
    }
  }

  protected final <T> void checkRequiredList(List<T> list, int min, String errorMsg) {
    checkRequired(list, errorMsg);
    if (min <= 0) {
      throw new BuilderException(format("Minimum amount must be >= 1 but was given {0}", min));
    }

    if (list.size() < min) {
      val prefixedErrorMsg =
          format("List (size {0}) missing required elements: {1}", list.size(), min);
      throw new BuilderException(prefixedErrorMsg);
    }
  }

  protected final <T extends IValueSet> void checkValueSet(T obj, T... oneOf) {
    checkValueSet(obj, List.of(oneOf));
  }

  protected final <T extends IValueSet> void checkValueSet(T obj, List<T> oneOf) {
    checkRequired(obj, "Valueset choice");
    checkRequiredList(oneOf, 1, format("Expected valueset choice"));
    if (!oneOf.contains(obj)) {
      val errorMsg = format("Given valueset is not in the list of expected choices");
      throw new BuilderException(errorMsg);
    }
  }
}
