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

package de.gematik.test.erezept.fhir.values;

import static java.text.MessageFormat.*;

import de.gematik.test.erezept.fhir.parser.profiles.*;
import java.util.*;
import lombok.*;
import org.hl7.fhir.r4.model.*;

@Getter
public abstract class Value<T> {

  private final IWithSystem system;
  private final T value; // NOSONAR Value<T> is wrapping the concrete T value with a naming system

  protected Value(final IWithSystem system, final T value) {
    this.system = system;
    this.value = value;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    final Value<?> value1 = (Value<?>) o;
    return getSystem() == value1.getSystem() && Objects.equals(getValue(), value1.getValue());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getSystem(), getValue());
  }

  public String getSystemAsString() {
    return this.system.getCanonicalUrl();
  }

  public Identifier asIdentifier() {
    return asIdentifier(this.system);
  }

  /**
   * This method is required because depending on the version of the used profile the concrete
   * system can vary This method might become obsolete in future once the systems of values settled
   *
   * @param system to be used to denote the identifier
   * @return this value as an identifier
   */
  public Identifier asIdentifier(IWithSystem system) {
    return new Identifier()
        .setSystem(system.getCanonicalUrl())
        .setValue(format("{0}", this.getValue()));
  }

  public Reference asReference() {
    return asReference(this.system);
  }

  /**
   * This method is required because depending on the version of the used profile the concrete
   * system can vary This method might become obsolete in future once the systems of values settled
   *
   * @param system to be used to denote the identifier
   * @return this value as reference
   */
  public Reference asReference(IWithSystem system) {
    val ref = new Reference();
    ref.setIdentifier(asIdentifier(system));
    return ref;
  }

  @Override
  public String toString() {
    return format(
        "{0}(System: {1} Value: {2})",
        this.getClass().getSimpleName(), this.system.getCanonicalUrl(), this.value);
  }
}
