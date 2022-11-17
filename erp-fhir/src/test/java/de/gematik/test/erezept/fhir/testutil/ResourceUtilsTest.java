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

package de.gematik.test.erezept.fhir.testutil;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import de.gematik.test.erezept.fhir.util.ResourceUtils;
import java.io.File;
import java.util.List;
import java.util.stream.Collectors;
import lombok.val;
import org.junit.Test;

public class ResourceUtilsTest {

  @Test
  public void getResourceFilesInDirectory() {
    val path = "simple";
    val expectedFiles = List.of("sample_01.txt", "sample_01.xml");
    val files = ResourceUtils.getResourceFilesInDirectory(path);

    assertEquals(expectedFiles.size(), files.size());
    assertTrue(
        files.stream().map(File::getName).collect(Collectors.toList()).containsAll(expectedFiles));
  }
}