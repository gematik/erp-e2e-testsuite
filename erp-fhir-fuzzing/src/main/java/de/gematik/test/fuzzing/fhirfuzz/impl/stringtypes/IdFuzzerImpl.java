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

package de.gematik.test.fuzzing.fhirfuzz.impl.stringtypes;

import de.gematik.test.fuzzing.fhirfuzz.BaseFuzzer;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzerContext;
import de.gematik.test.fuzzing.fhirfuzz.utils.UnmutableFuzzingMutator;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@Slf4j
public class IdFuzzerImpl implements BaseFuzzer<String> {
    private final FuzzerContext fuzzerContext;

    public IdFuzzerImpl(FuzzerContext fuzzerContext) {
        this.fuzzerContext = fuzzerContext;
    }

    @Override
    public FuzzerContext getContext() {
        return fuzzerContext;
    }

    @Override
    public String fuzz(String id) {
        if (id == null || id.isEmpty()) {
            log.info("given Id to fuzz was null!, generated random one");
            return this.generateRandom();
        }

        List<UnmutableFuzzingMutator<String>> m;

        if (id.startsWith("http")) {
            m = fuzzerContext.getRandomPart(getUrlMutators());
            for (UnmutableFuzzingMutator<String> f : m) {
                id = f.apply(id);
            }
            if (id == null || id.isEmpty()) {
                return this.generateRandom();
            }
            return id;
        }
        if (id.matches("[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}")) {
            m = (fuzzerContext.getRandomPart(getUuidMutators()));
            for (UnmutableFuzzingMutator<String> f : m) {
                id = f.apply(id);
            }
            if (id == null || id.isEmpty()) {
                return String.valueOf(UUID.randomUUID());
            }
            return id;
        } else {
            return simpleIdStringFuzz(id);
        }
    }


    private List<UnmutableFuzzingMutator<String>> getUrlMutators() {
        val manipulators = new LinkedList<UnmutableFuzzingMutator<String>>();
        manipulators.add(this::urlFuzz1);
        manipulators.add(this::urlFuzz2);
        manipulators.add(this::urlFuzz3);
        manipulators.add(this::urlFuzz4);
        manipulators.add(this::urlFuzz5);
        return manipulators;
    }

    private String urlFuzz1(String url) {
        val urlArray = url.split("/");
        StringBuilder res = new StringBuilder();
        for (val s : urlArray) {
            if (!fuzzerContext.conditionalChance(fuzzerContext.getFuzzConfig().getPercentOfEach())) {
                res.append(s).append("/");
            }
        }
        return res.toString();
    }

    private String urlFuzz2(String url) {
        val urlArray = url.split("/");
        StringBuilder res = new StringBuilder();
        for (String s : urlArray) {
            if (fuzzerContext.conditionalChance(fuzzerContext.getFuzzConfig().getPercentOfEach())) {
                s = fuzzerContext.getStringFuzz().fuzz(s);
            }
            res.append(s).append("/");
        }
        return res.toString();
    }

    private String urlFuzz3(String url) {
        val urlArray = url.split("/");
        StringBuilder res = new StringBuilder();
        for (String s : urlArray) {
            if (fuzzerContext.conditionalChance(fuzzerContext.getFuzzConfig().getPercentOfEach() / 3)) {
                s = fuzzerContext.getStringFuzz().fuzz(s);
            }
            res.append(s).append("/");
        }
        return res.toString();
    }

    private String urlFuzz4(String url) {
        if (url.contains("https://") && url.length() > 20) {
            return "https://" + url.substring(8, 11) + url.substring(13);
        }
        if (url.contains("Https://")) {
            return "Https://" + url.substring(9);
        }
        if (url.contains("http://") && url.length() > 20) {
            return "http://" + url.substring(7, 15) + url.substring(17);
        }
        if (url.contains("Http://") && url.length() > 20) {
            return "Http://" + url.substring(7);
        }
        return fuzzerContext.getStringFuzz().fuzz(url);
    }

    private String urlFuzz5(String url) {
        return fuzzerContext.getStringFuzz().fuzz(url);
    }

    private List<UnmutableFuzzingMutator<String>> getUuidMutators() {
        val manipulators = new LinkedList<UnmutableFuzzingMutator<String>>();
        manipulators.add(this::uuidFuzz1);
        manipulators.add(this::uuidFuzz2);
        manipulators.add(this::uuidFuzz3);
        manipulators.add(this::uuidFuzz4);
        return manipulators;
    }

    private String uuidFuzz1(String uuid) {
        val uuidArray = uuid.split("-");
        StringBuilder res = new StringBuilder();
        for (val s : uuidArray) {
            if (!fuzzerContext.conditionalChance(fuzzerContext.getFuzzConfig().getPercentOfEach())) {
                res.append(s).append("-");
            }
        }
        return res.toString();
    }

    private String uuidFuzz2(String uuid) {
        val uuidArray = uuid.split("-");
        StringBuilder res = new StringBuilder();
        for (String s : uuidArray) {
            if (!fuzzerContext.conditionalChance(fuzzerContext.getFuzzConfig().getPercentOfEach())) {
                s = fuzzerContext.getStringFuzz().fuzz(s);
            }
            res.append(s).append("-");
        }
        return res.toString();
    }

    private String uuidFuzz3(String uuid) {
        val uuidArray = uuid.split("-");
        StringBuilder res = new StringBuilder();
        for (String s : uuidArray) {
            if (!fuzzerContext.conditionalChance(fuzzerContext.getFuzzConfig().getPercentOfEach() / 1.5f)) {
                s = fuzzerContext.getStringFuzz().fuzz(s);
            }
            res.append(s).append("-");
        }
        return res.toString();
    }

    private String uuidFuzz4(String uuid) {
        return UUID.randomUUID().toString();
    }

    private String simpleIdStringFuzz(String id) {
        char[] chars = id.toCharArray();
        for (int iter = 0; iter < chars.length; iter++) {
            if (fuzzerContext.conditionalChance(fuzzerContext.getFuzzConfig().getPercentOfEach())) {
                val cd = String.valueOf(fuzzerContext.nextInt(99));
                chars[iter] = cd.charAt(0);
            }
        }
        if (id.isEmpty()) return this.generateRandom();
        return String.valueOf(chars);
    }

    public String generateRandom() {
        return fuzzerContext.getFaker().regexify("[0-9]{3}[\\.]{1}[0-9]{3}[\\.]{1}[0-9]{3}[\\.]{1}[0-9]{3}[\\.]{1}[0-9]{3}");
    }

}
