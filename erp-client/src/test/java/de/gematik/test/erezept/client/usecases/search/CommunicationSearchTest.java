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

package de.gematik.test.erezept.client.usecases.search;

import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.test.erezept.fhir.testutil.PrivateConstructorsUtil;
import lombok.val;
import org.junit.jupiter.api.Test;

class CommunicationSearchTest {

  @Test
  void shouldNotInstantiate() {
    assertTrue(PrivateConstructorsUtil.throwsInvocationTargetException(CommunicationSearch.class));
  }
  
  @Test
  void getNewCommunications() {
    val cmd = CommunicationSearch.getNewCommunications();
    assertTrue(cmd.getRequestLocator().contains("?received=NULL"));
  }

  @Test
  void getLatestCommunicationsTest() {
    val cmd = CommunicationSearch.getLatestCommunications();
    assertTrue(cmd.getRequestLocator().contains("_sort=-sent"));
  }

  @Test
  void getLatestNewCommunicationsTest() {
    val cmd = CommunicationSearch.getLatestNewCommunications();
    assertTrue(cmd.getRequestLocator().contains("_sort=-sent"));
    assertTrue(cmd.getRequestLocator().contains("?received=NULL"));
  }
}
