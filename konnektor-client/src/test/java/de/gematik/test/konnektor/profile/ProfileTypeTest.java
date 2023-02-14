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

package de.gematik.test.konnektor.profile;

import static org.junit.Assert.*;

import de.gematik.test.konnektor.exceptions.InvalidKonnektorProfileException;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.val;
import org.junit.Test;

public class ProfileTypeTest {

  @Test
  public void shouldParseValidProfileTypes() {
    val inputs = List.of("KonSim", "KONSIM", "Secunet", "SECUNET", "Rise", "RISE", "CGM", "cgm");
    val expected =
        List.of(
            ProfileType.KONSIM,
            ProfileType.KONSIM,
            ProfileType.SECUNET,
            ProfileType.SECUNET,
            ProfileType.RISE,
            ProfileType.RISE,
            ProfileType.CGM,
            ProfileType.CGM);

    assertEquals(inputs.size(), expected.size());
    for (var i = 0; i < inputs.size(); i++) {
      val parsed = ProfileType.fromString(inputs.get(i));
      val exp = expected.get(i);
      assertEquals(exp, parsed);
    }
  }

  @Test
  public void shouldCreateProfileFromType() {
    val inputs = List.of("KonSim", "KONSIM", "Secunet", "SECUNET", "Rise", "RISE", "CGM", "cgm");
    inputs.stream()
        .map(ProfileType::fromString)
        .map(pt -> new Tuple<>(pt, pt.createProfile()))
        .forEach(
            tuple -> {
              val type = tuple.key;
              val value = tuple.value;
              assertNotNull(value);
              assertEquals(type, value.getType());
            });
  }

  @Test
  public void shouldThrowOnInvalidProfileTypes() {
    val inputs = List.of("KonSimulator", "SimKon", "Sekunet", "Reis", "GCM", "");
    inputs.forEach(
        input ->
            assertThrows(
                InvalidKonnektorProfileException.class, () -> ProfileType.fromString(input)));
  }

  @Test
  public void shouldThrowOnNullProfileType() {
    String input = null;
    assertNull(input);
    assertThrows(
        NullPointerException.class,
        () -> ProfileType.fromString(input)); // NOSONAR null by intention
  }

  @AllArgsConstructor
  private static class Tuple<K, V> {
    private K key;
    private V value;
  }
}
