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

package de.gematik.test.fuzzing.fhirfuzz.playground;

import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.test.erezept.fhir.util.ResourceUtils;
import de.gematik.test.fuzzing.fhirfuzz.FuzzMain;
import lombok.val;
import org.junit.Ignore;

class MainFuzzTest {

  String confJson =
      """
            {"name":"MainFuzzTest Conf",
            "usedPercentOfMutators":50,
            "detailSetup":
            {
            "BreakRanges":true,
            "KBV":"false"
            },
            "percentOfAll":50.0,
            "percentOfEach":50.0,
            "useAllMutators":true,
            "iterations":1,
            "pathToPrintFile":null,
            "shouldPrintToFile":true}""";

  /**
   * that Test-Call is only an Example how to use the Main.exe and how to format and handed over
   * possible Json - Configs
   */
  @Ignore
  void main() {
    val pathToBundle = "fhir/valid/kbv/1.0.2/bundle/5a3458b0-8364-4682-96e2-b262b2ab16eb.xml";
    // val pathToBundle2 = "fhir/valid/kbv/1.1.0/bundle/5a3458b0-8364-4682-96e2-b262b2ab16eb.xml";
    val path3 = "erp-fhir-fuzzing/target/tmp/test-bundle/erp_bundle_missing_id_01.xml";
    val stringBundle = readFromFile(pathToBundle);
    // val stringBundle2 = readFromFile(pathToBundle2);
    // val xmlBundle = MainFuzz.main(new String[]{stringBundle, confJson});
    val xmlBundle = FuzzMain.main(new String[] {null, confJson});
    // MainFuzz.main(new String[]{null, null});
    assertTrue(xmlBundle.length() > 10);
  }

  private String readFromFile(String file) {
    return ResourceUtils.readFileFromResource(file);
  }

  // Method to test single Fuzzer ( + Children) intensively
  /* public static void main(String[] args) {
          val fhir = new FhirParser();
          for (int i = 0; i < 5000; i++) {
              FuzzConfig fuzzConfig = new FuzzConfig();
              fuzzConfig.setPercentOfEach(50f);
              fuzzConfig.setPercentOfAll(50f);
              val fuzzContext = new FuzzerContext(fuzzConfig);
              val comFuzz = new CompositionFuzzImpl(fuzzContext);

              val kbvBundle = KbvErpBundleBuilder.faker().build();
              comFuzz.fuzz(kbvBundle.getComposition());

              try {
                  val test = fhir.encode(kbvBundle, EncodingType.XML);
              } catch (Exception e) {
                  System.out.println(fuzzContext.getOperationLogs().stream().map(Object::toString).collect(Collectors.joining("\n")));
                  throw e;
              }
              //System.out.println(i);
              //System.out.println(test);
          }

      }
  */
}
