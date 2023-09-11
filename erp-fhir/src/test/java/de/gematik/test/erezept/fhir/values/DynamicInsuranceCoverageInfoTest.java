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

import de.gematik.test.erezept.fhir.valuesets.VersicherungsArtDeBasis;
import lombok.val;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DynamicInsuranceCoverageInfoTest {

    @Test
    void shouldGenerateDynamicCoverageData() {
        val data = DynamicInsuranceCoverageInfo.named("Farmer").ofType(VersicherungsArtDeBasis.SEL).withIknr("123123123");
        assertEquals("Farmer", data.getName());
        assertEquals("123123123", data.getIknr());
        assertEquals(VersicherungsArtDeBasis.SEL, data.getInsuranceType());
    }

    @Test
    void shouldGenerateRandomCoverageData() {
        val data = DynamicInsuranceCoverageInfo.random();
        assertNotNull(data.getName());
        assertNotNull(data.getIknr());
    }
}
