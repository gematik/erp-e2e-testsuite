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

package de.gematik.test.erezept.fhir.builder;

import static org.junit.Assert.assertThrows;

import de.gematik.test.erezept.fhir.exceptions.BuilderException;
import java.util.List;
import lombok.val;
import org.hl7.fhir.r4.model.Address;
import org.junit.Test;

public class AddressBuilderTest {

  @Test(expected = Test.None.class) /* no exception expected */
  public void testFixedStreetnames() {
    val city = "Berlin";
    val postal = "10117";
    val input =
        List.of(
            "Friedrichstr. 60",
            "Friedrichstraße 38",
            "Friedrichstraße 38/40",
            "Friedrichstr. 60 3 OG",
            "Kolmarer Str. 928",
            "Nikolaus-Ehlen-Weg 21b",
            "Nisbléstr 87",
            "Adalbertstr. 198 Apt. 191");
    input.forEach(
        street -> AddressBuilder.address(city, postal, street, Address.AddressType.PHYSICAL));
  }

  @Test(expected = Test.None.class) /* no exception expected */
  public void testRandomStreetnames() {
    val city = "Berlin";
    val postal = "10117";
    for (int i = 0; i < 10; i++) {
      val rndStreet = GemFaker.fullStreetName();
      AddressBuilder.address(city, postal, rndStreet, Address.AddressType.BOTH);
    }
  }

  @Test
  public void shouldThrowOnInvalidStreetnamePattern() {
    val city = "Berlin";
    val postal = "10117";
    val invalidStreet = "13te Straße";
    assertThrows(
        BuilderException.class,
        () -> AddressBuilder.address(city, postal, invalidStreet, Address.AddressType.BOTH));
  }
}
