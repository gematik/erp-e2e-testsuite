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

package de.gematik.test.erezept.fhir.values;

import lombok.val;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PZNTest {

    @ParameterizedTest(name = "[{index}]: PZN {0} is valid")
    @ValueSource(strings = {"27580899", "20987327", "91168337"})
    void shouldValidPZN(String value) {
        val pzn = PZN.from(value);
        assertTrue(pzn.isValid());
    }

    @RepeatedTest(5)
    void shouldGenerateRandomValidPZN() {
        val pzn = PZN.random();
        assertTrue(pzn.isValid());
    }

    @ParameterizedTest(name = "[{index}]: PZN {0} is invalid")
    @ValueSource(strings = {"abc", "1234", "123456789"})
    void shouldCheckInvalidPznCheckDigit(String value) {
        val pzn = PZN.from(value);
        assertFalse(pzn.isValid());
    }
}