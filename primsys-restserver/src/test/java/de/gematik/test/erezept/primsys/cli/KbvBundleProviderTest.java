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

package de.gematik.test.erezept.primsys.cli;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import de.gematik.test.erezept.fhir.parser.FhirParser;
import de.gematik.test.erezept.fhir.resources.kbv.KbvErpBundle;
import de.gematik.test.erezept.fhir.util.ResourceUtils;
import java.nio.file.Path;
import lombok.val;
import org.hl7.fhir.r4.model.Coverage;
import org.junit.Before;
import org.junit.Test;

public class KbvBundleProviderTest {

  private FhirParser parser;

  @Before
  public void setUp() {
    this.parser = new FhirParser();
  }

  @Test
  public void shouldProvideKbvBundleNameOnly() {
    val fileName = "example_kbv_bundle.xml";
    val path = Path.of("some_base_path", fileName);
    val provider = new KbvBundleProvider(path, null, mock(FhirParser.class));
    assertEquals(fileName, provider.getFileName());
  }

  @Test
  public void shouldProvideMixedName() {
    val kbvBundleFileName = "example_kbv_bundle.xml";
    val coverageName = "barmer.xml";
    val kbvPath = Path.of("some_base_path", kbvBundleFileName);
    val coveragePath = Path.of("some_base_path", coverageName);
    val provider = new KbvBundleProvider(kbvPath, coveragePath, mock(FhirParser.class));

    val expected = "example_kbv_bundle_barmer.xml";
    assertEquals(expected, provider.getFileName());
  }

  @Test
  public void shouldProvideKbvBundleOnly() {
    val baseDir = "./src/test/resources/bundles/";
    val fileName = "X110413580_IK104940005_Beispiel_2_PZN_08850519_autidem.xml";
    val path = Path.of(baseDir, fileName);

    val provider = new KbvBundleProvider(path, null, parser);

    val provided = provider.getKbvBundle();
    assertNotNull(provided);

    val expected =
        parser.decode(
            KbvErpBundle.class, ResourceUtils.readFileFromResource("bundles/" + fileName));
    assertEquals(expected.getId(), provided.getId());
    assertEquals(expected.getCoverageName(), provided.getCoverageName());
    assertEquals(expected.getCoverageIknr(), provided.getCoverageIknr());
  }

  @Test
  public void shouldProvideMixedBundle() {
    val baseDir = "./src/test/resources/";
    val bundlesBaseDir = baseDir + "bundles/";
    val coverageBaseDir = baseDir + "coverages/";
    val kbvBundleFileName = "X110413580_IK104940005_Beispiel_2_PZN_08850519_autidem.xml";
    val coverageFileName = "aok_bw.xml";
    val kbvBundlePath = Path.of(bundlesBaseDir, kbvBundleFileName);
    val coveragePath = Path.of(coverageBaseDir, coverageFileName);

    val provider = new KbvBundleProvider(kbvBundlePath, coveragePath, parser);

    val provided = provider.getKbvBundle();
    assertNotNull(provided);

    val expectedBundle =
        parser.decode(
            KbvErpBundle.class, ResourceUtils.readFileFromResource("bundles/" + kbvBundleFileName));
    val expectedCoverage =
        parser.decode(
            Coverage.class, ResourceUtils.readFileFromResource("coverages/" + coverageFileName));
    assertEquals(expectedBundle.getId(), provided.getId());
    assertEquals(expectedCoverage.getPayorFirstRep().getDisplay(), provided.getCoverageName());
    assertEquals(
        expectedCoverage.getPayorFirstRep().getIdentifier().getValue(), provided.getCoverageIknr());
  }
}
