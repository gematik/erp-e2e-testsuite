/*
 * Copyright 2025 gematik GmbH
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
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 */

package de.gematik.test.erezept.cli.cmd.replace;

import static java.text.MessageFormat.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.bbriccs.fhir.EncodingType;
import de.gematik.bbriccs.utils.ResourceLoader;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpBundle;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import picocli.CommandLine;
import picocli.CommandLine.ExitCode;

class KvnrReplacerTest extends ErpFhirParsingTest {

  @ParameterizedTest(name = "[{index}] Replace KVNR for {2} Patient from {1}")
  @CsvSource({
    "1.1.0, 1f339db0-9e55-4946-9dfa-f1b30953be9b, GKV",
    "1.1.0, 328ad940-3fff-11ed-b878-0242ac120002, PKV"
  })
  void shouldReplaceKvnrInKbvBundleCorrectly(String version, String id, String insuranceType)
      throws IOException {
    val inputFileName = format("{0}.xml", id);
    val replacedKvnr = "A123123123";
    val input =
        ResourceLoader.getFileFromResource(
                format("fhir/valid/kbv/{0}/bundle/{1}", version, inputFileName))
            .getAbsolutePath();
    val outputDir =
        Path.of(
                System.getProperty("user.dir"),
                "target",
                "tmp",
                "kbvbundles",
                version,
                insuranceType,
                "out")
            .toString();
    val kvnrReplacer = new KvnrReplacer();
    val cmdline = new CommandLine(kvnrReplacer);
    val ret = cmdline.execute("--to", replacedKvnr, input, outputDir);
    assertEquals(ExitCode.OK, ret);

    // ensure the new file was generated
    val expectedFile = Path.of(outputDir, format("{0}_{1}", replacedKvnr, inputFileName)).toFile();
    assertTrue(expectedFile.exists());
    assertTrue(expectedFile.isFile());

    // ensure the new file has a changed KVNR
    val replacedContent = Files.readString(expectedFile.toPath());
    val replacedResource = parser.decode(KbvErpBundle.class, replacedContent);
    assertEquals(replacedKvnr, replacedResource.getPatient().getKvnr().getValue());
  }

  @Test
  void shouldReplaceKvnrInKbvPatientCorrectly() throws URISyntaxException, IOException {
    val bundleInputFileName = "1f339db0-9e55-4946-9dfa-f1b30953be9b.xml";
    val replacedKvnr = "A123123123";
    val input =
        this.getClass()
            .getClassLoader()
            .getResource(format("fhir/valid/kbv/1.1.0/bundle/{0}", bundleInputFileName))
            .toURI()
            .getPath();

    val outputDir =
        Path.of(System.getProperty("user.dir"), "target", "tmp", "kbvpatients", "out").toString();
    Files.createDirectories(Path.of(outputDir));

    // we don't have KbvPatients as separate files thus need to extract from KbvBundle first
    val kbvBundle = parser.decode(KbvErpBundle.class, Files.readString(Path.of(input)));
    val kbvPatient = kbvBundle.getPatient();
    val kbvPatientContent = parser.encode(kbvPatient, EncodingType.XML);
    val patientInputPath = Path.of(outputDir, "patient_input.xml");
    Files.writeString(patientInputPath, kbvPatientContent);

    val kvnrReplacer = new KvnrReplacer();
    val cmdline = new CommandLine(kvnrReplacer);
    val ret = cmdline.execute("--to", replacedKvnr, patientInputPath.toString(), outputDir);
    assertEquals(ExitCode.OK, ret);

    // ensure the new file was generated
    val expectedFile =
        Path.of(outputDir, format("{0}_{1}", replacedKvnr, patientInputPath.toFile().getName()))
            .toFile();
    assertTrue(expectedFile.exists());
    assertTrue(expectedFile.isFile());
  }

  @Test
  void shouldNotReplaceKvnrInKbvPatientWithoutKvnr() throws URISyntaxException, IOException {
    val bundleInputFileName = "1f339db0-9e55-4946-9dfa-f1b30953be9b.xml";
    val replacedKvnr = "A123123123";
    val input =
        this.getClass()
            .getClassLoader()
            .getResource(format("fhir/valid/kbv/1.1.0/bundle/{0}", bundleInputFileName))
            .toURI()
            .getPath();

    val outputDir =
        Path.of(System.getProperty("user.dir"), "target", "tmp", "kbvpatients", "out").toString();
    Files.createDirectories(Path.of(outputDir));

    // we don't have KbvPatients as separate files thus need to extract from KbvBundle first
    val kbvBundle = parser.decode(KbvErpBundle.class, Files.readString(Path.of(input)));
    val kbvPatient = kbvBundle.getPatient();

    // remove KVNR from Patient
    kbvPatient.getIdentifier().remove(0);

    val kbvPatientContent = parser.encode(kbvPatient, EncodingType.XML);
    val patientInputPath = Path.of(outputDir, "patient_input_without_kvid.xml");
    Files.writeString(patientInputPath, kbvPatientContent);

    val kvnrReplacer = new KvnrReplacer();
    val cmdline = new CommandLine(kvnrReplacer);
    val ret = cmdline.execute("--to", replacedKvnr, patientInputPath.toString(), outputDir);
    assertEquals(ExitCode.OK, ret);

    // ensure the new file was generated
    val expectedFile =
        Path.of(outputDir, format("{0}_{1}", replacedKvnr, patientInputPath.toFile().getName()))
            .toFile();
    assertFalse(expectedFile.exists());
  }

  @Test
  void shouldNotReplaceOnOtherResources() throws URISyntaxException, IOException {
    val inputFileName = "43c2b7ae-ad11-4387-910a-e6b7a3c38d3a.xml";
    val replacedKvnr = "A123123123";
    val input =
        this.getClass()
            .getClassLoader()
            .getResource(format("fhir/valid/kbv/1.1.0/medicationrequest/{0}", inputFileName))
            .toURI()
            .getPath();
    val outputDir =
        Path.of(System.getProperty("user.dir"), "target", "tmp", "medicationrequests", "out");
    val kvnrReplacer = new KvnrReplacer();
    val cmdline = new CommandLine(kvnrReplacer);
    val ret = cmdline.execute("--to", replacedKvnr, input, outputDir.toString());
    assertEquals(ExitCode.OK, ret);

    if (Files.exists(outputDir)) {
      // if the outputDir exists for whatever reason, ensure files containing the original filename
      // are contained
      Files.list(outputDir).forEach(p -> assertFalse(p.toFile().getName().contains(inputFileName)));
    } else {
      // if it does not exist: pass the test because no files were generated as expected
      assertFalse(Files.exists(outputDir));
    }
  }
}
