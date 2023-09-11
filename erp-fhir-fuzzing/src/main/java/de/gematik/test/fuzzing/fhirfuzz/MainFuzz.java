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

package de.gematik.test.fuzzing.fhirfuzz;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpBundleBuilder;
import de.gematik.test.erezept.fhir.parser.EncodingType;
import de.gematik.test.erezept.fhir.parser.FhirParser;
import de.gematik.test.erezept.fhir.resources.kbv.KbvErpBundle;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzConfig;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzOperationResult;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzerContext;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.r4.model.Bundle;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.file.Path;
import java.util.stream.Collectors;

import static java.text.MessageFormat.format;

@Slf4j
public class MainFuzz {
    private static FhirParser fhirParser;
    private static FuzzConfig fuzzConfig;
    private static String isValidBeforeFuzz;
    private static boolean xmlIsValidAfterFuzz;

    private MainFuzz() {
        throw new IllegalStateException("MainFuzz has no Constructor");
    }

    /**
     * Fuzzes a given Bundle and write it out in files
     * If given bundle or given configJson is invalid
     * it will be set random.
     *
     * @param args [0] = bundle
     *             [1] = config Params as json-String
     */
    public static String main(String[] args) {
        String bundleString = getString(args);
        String configString = getConfigString(args);

        fhirParser = new FhirParser();
        fuzzConfig = setupConf(configString);
        if (fuzzConfig == null) {
            fuzzConfig = FuzzConfig.getRandom();
        }
        FuzzerContext fuzzerContext = new FuzzerContext(fuzzConfig);
        FhirFuzzImpl fhirBundleFuzz = new FhirFuzzImpl(fuzzerContext);
        Bundle bundle;
        Bundle bundleCopy = new Bundle();
        val emptyTargetDir = Path.of(System.getProperty("user.dir"), "target", "tmp", "out");
        emptyTargetDir.toFile().mkdirs();
        String xmlFuzzedBundle = "";
        for (int i = 0; i < (fuzzConfig.getIterations() != 0 ? fuzzConfig.getIterations() : 10); i++) {
            fuzzerContext.clearOperationLogs();
            var fuzzLog = "______________________________________________ RUN: " + (i + 1) + " ________________________________________";
            fuzzerContext.getOperationLogs().add(new FuzzOperationResult<>("fuzzConfig parameters: ", "actual used parameters", fuzzerContext.getFuzzConfig().toString()));
            try {
                bundle = decodeBundleOrFake(bundleString, fhirParser);
                bundleCopy.copyValues(bundle);
                // fuzz
                fhirBundleFuzz.fuzz(bundle);
                // encode and validate given one or generated default bundle
                val jsonOriginalBundle = fhirParser.encode(bundleCopy, EncodingType.JSON);
                isValidBeforeFuzz = "_isValid_" + fhirParser.isValid(jsonOriginalBundle);
                fuzzLog = fuzzerContext.getOperationLogs().stream().map(Object::toString).collect(Collectors.joining("\n"));
                // XML
                xmlFuzzedBundle = fhirParser.encode(bundle, EncodingType.XML);
                xmlIsValidAfterFuzz = fhirParser.isValid(xmlFuzzedBundle);
                log.info(" iteration: " + (i + 1) + ", is valid after fuzz: " + xmlIsValidAfterFuzz + "\n" + fuzzConfig);
                possibleSaveFiles(bundle, emptyTargetDir, xmlFuzzedBundle, i, fuzzLog, jsonOriginalBundle);

            } catch (Exception e) {
                log.info(format("writing  failed: {0} out results: {1} ", e, fuzzLog));
                writeStringUsingBufferedWriter(xmlFuzzedBundle, emptyTargetDir, format("Run_{0}_xmlFuzzedBundle{1}.xml", i, false));
                writeStringUsingBufferedWriter(fuzzLog, emptyTargetDir, format("Run_{0}_fuzzLog{1}.txt", i, false));
                writeStringUsingBufferedWriter(e.toString(), emptyTargetDir, format("Run_{0}_StackTrace{1}.txt", i, false));
                throw e;
            }
        }
        return xmlFuzzedBundle;
    }

    private static void possibleSaveFiles(Bundle bundle, Path emptyTargetDir, String xmlFuzzedBundle, int i, String fuzzLog, String jsonOriginalBundle) {
        if (fuzzConfig.getShouldPrintToFile()) {
            val jsonFuzzedBundle = fhirParser.encode(bundle, EncodingType.JSON);
            val isValidAfterFuzz = "_isvalid_" + fhirParser.isValid(jsonFuzzedBundle);
            writeStringUsingBufferedWriter(xmlFuzzedBundle, emptyTargetDir, format("Run_{0}_xmlFuzzedBundle{1}.xml", i, xmlIsValidAfterFuzz));
            writeStringUsingBufferedWriter(jsonFuzzedBundle, emptyTargetDir, format("Run_{0}_fuzzedBundle{1}.json", i, isValidBeforeFuzz));
            writeStringUsingBufferedWriter(fuzzLog, emptyTargetDir, format("Run_{0}_fuzzLog{1}.txt", i, isValidAfterFuzz));
            writeStringUsingBufferedWriter(jsonOriginalBundle, emptyTargetDir, format("Run_{0}_orgBundle{1}.json", i, isValidBeforeFuzz));
            log.info("log.info: " + fuzzLog);
        }
    }


    @Nullable
    private static String getString(String[] args) {
        String bundleString;
        if (args.length > 0) {
            bundleString = args[0];
        } else {
            bundleString = null;
        }
        return bundleString;
    }

    @Nullable
    private static String getConfigString(String[] args) {
        String configString;
        if (args.length > 1) {
            configString = args[1];
        } else {
            configString = null;
        }
        return configString;
    }

    public static FuzzConfig setupConf(String conf) {
        FuzzConfig fuzzConfig = null;
        if (conf != null) {
            try {
                val om = new ObjectMapper();
                fuzzConfig = om.readValue(conf, FuzzConfig.class);
            } catch (JsonProcessingException e) {
                log.info("encode String config failed: {0}", e);
            }
        } else {
            log.info("encode String config failed: config could not be parse");
        }
        return fuzzConfig;
    }

    private static Bundle decodeBundleOrFake(String bundleString, FhirParser fhirParser) {
        Bundle bundle = new Bundle();
        if (bundleString != null && !bundleString.isEmpty())
            try {
                bundle = fhirParser.decode(KbvErpBundle.class, bundleString);
            } catch (Exception e) {
                log.info(format("encode given bundle to KbvErpBundle Failed: {0}", e));
            }
        else {
            bundle = KbvErpBundleBuilder.faker().build();
        }
        return bundle;
    }

    @SneakyThrows
    @SuppressWarnings("java:S6300")
    public static void writeStringUsingBufferedWriter(String bundle, Path dir, String fileName) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(Path.of(dir.toAbsolutePath().toString(), fileName).toFile()))) {
            writer.write(bundle);
        }
    }
}
