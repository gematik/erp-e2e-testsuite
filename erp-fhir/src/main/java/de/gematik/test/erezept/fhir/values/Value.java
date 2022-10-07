/*
 * Copyright (c) 2022 gematik GmbH
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

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.fhir.parser.profiles.IWithSystem;
import java.util.Objects;
import lombok.Getter;
import lombok.val;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Reference;

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
    return new Identifier()
        .setSystem(this.getSystemAsString())
        .setValue(this.getValue().toString());
  }

  public Reference asReference() {
    val ref = new Reference();
    ref.setIdentifier(asIdentifier());
    return ref;
  }

  @Override
  public String toString() {
    return format("{0}({1})", this.getClass().getSimpleName(), this.value);
  }
}
