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

package de.gematik.test.fuzzing.fhirfuzz.impl.resourcefuzzer;

import de.gematik.test.fuzzing.core.FuzzingMutator;
import de.gematik.test.fuzzing.fhirfuzz.FhirResourceFuzz;
import de.gematik.test.fuzzing.fhirfuzz.impl.ListFuzzerImpl;
import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.AddressFuzzerImpl;
import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.ExtensionFuzzerImpl;
import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.HumanNameFuzzerImpl;
import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.IdentifierFuzzerImpl;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzOperationResult;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzerContext;
import lombok.val;
import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Narrative;
import org.hl7.fhir.r4.model.Patient;

import java.util.LinkedList;
import java.util.List;

public class PatientFuzzerImpl implements FhirResourceFuzz<Patient> {
    private final FuzzerContext fuzzerContext;

    public PatientFuzzerImpl(FuzzerContext fuzzerContext) {
        this.fuzzerContext = fuzzerContext;
    }

    @Override
    public Patient fuzz(Patient patient) {
        val m = fuzzerContext.getRandomPart(getMutators());
        for (FuzzingMutator<Patient> f : m) {
            f.accept(patient);
        }
        return patient;
    }

    private List<FuzzingMutator<Patient>> getMutators() {
        val manipulators = new LinkedList<FuzzingMutator<Patient>>();
        manipulators.add(this::idFuzz);
        manipulators.add(this::metaFuzz);
        manipulators.add(this::identifyFuzz);
        manipulators.add(this::langFuzz);
        manipulators.add(this::activeFuzz);
        manipulators.add(this::textFuzz);
        // !! List of Human Name !!
        manipulators.add(this::nameFuzz);
        manipulators.add(this::birthdayFuzz);
        manipulators.add(this::addressFuzz);
        manipulators.add(this::extensionFuzz);
        manipulators.add(this::codeConFuzz);
        return manipulators;
    }

    private void idFuzz(Patient p) {
            val id = p.hasId() ? p.getId() : null;
            fuzzerContext.getIdFuzzer().fuzz(p::hasId, p::getId, p::setId);
            fuzzerContext.addLog(new FuzzOperationResult<>("fuzzed Id in Patient", id, p.hasId() ? p.getId() : null));
    }

    private void metaFuzz(Patient p) {
        val meta = p.hasMeta() ? p.getMeta() : null;
        fuzzerContext.getTypeFuzzerFor(Meta.class).ifPresent(tf -> tf.fuzz(p::hasMeta, p::getMeta, p::setMeta));
        fuzzerContext.addLog(new FuzzOperationResult<>("fuzz Meta in Patient", meta, p.hasMeta() ? p.getMeta() : null));
    }

    private void identifyFuzz(Patient p) {
        val identifyFuzzer = fuzzerContext.getTypeFuzzerFor(Identifier.class, () -> new IdentifierFuzzerImpl(fuzzerContext));
        if (!p.hasIdentifier()) {
            val newIdent = identifyFuzzer.generateRandom();
            p.setIdentifier(List.of(newIdent));
            fuzzerContext.addLog(new FuzzOperationResult<>("set Identifier in Patient", null, newIdent));
        } else {
            val org = p.getIdentifierFirstRep().copy();
            val listFuzz = new ListFuzzerImpl<>(fuzzerContext, identifyFuzzer);
            listFuzz.fuzz(p::getIdentifier, p::setIdentifier);
            fuzzerContext.addLog(new FuzzOperationResult<>("fuzz Identifier in Patient:", org, p.hasIdentifier() ? p.getIdentifierFirstRep() : null));
        }
    }

    private void langFuzz(Patient p) {
        var org = p.hasLanguage() ? p.getLanguage() : null;
        fuzzerContext.getLanguageCodeFuzzer().fuzz(p::getLanguage, p::setLanguage);
        fuzzerContext.addLog(new FuzzOperationResult<>("set Language in Patient", org, p.hasLanguage() ? p.getLanguage() : null));
    }

    private void activeFuzz(Patient p) {
        if (!p.hasActive()) {
            p.setActive(true);
            fuzzerContext.addLog(new FuzzOperationResult<>("set Active in Patient", null, true));
        } else {
            val old = p.getActive();
            p.setActive(!old);
            fuzzerContext.addLog(new FuzzOperationResult<>("fuzz Active in Patient", old, !old));
        }
    }

    private void textFuzz(Patient p) {
        val org = p.hasText() ? p.getText() : null;
        fuzzerContext.getTypeFuzzerFor(Narrative.class).ifPresent(tf -> tf.fuzz(p::hasText, p::getText, p::setText));
        fuzzerContext.addLog(new FuzzOperationResult<>("fuzz Text in Patient", org, p.hasText() ? p.getText() : null));

    }

    private void nameFuzz(Patient p) {
        val nameFuzzer = fuzzerContext.getTypeFuzzerFor(HumanName.class, () -> new HumanNameFuzzerImpl(fuzzerContext));
        if (!p.hasName()) {
            val hName = nameFuzzer.generateRandom();
            p.setName(List.of(hName));
            fuzzerContext.addLog(new FuzzOperationResult<>("set HumanName 1st Entry in Patient", null, hName));
        } else {
            val listFuzzer = new ListFuzzerImpl<>(fuzzerContext, nameFuzzer);
            listFuzzer.fuzz(p::hasName, p::getName, p::setName);
        }
    }

    private void birthdayFuzz(Patient p) {
        if (!p.hasBirthDate()) {
            val birth = fuzzerContext.getRandomDate(5);
            p.setBirthDate(birth);
            fuzzerContext.addLog(new FuzzOperationResult<>("set BirthDate in Patient", null, birth));
        } else {
            if (fuzzerContext.conditionalChance()) {
                val org = p.getBirthDate();
                p.setBirthDate(null);
                fuzzerContext.addLog(new FuzzOperationResult<>("set BithDate in Patient", org, null));
            } else {
                val org = p.getBirthDate();
                val birth = fuzzerContext.getRandomDate();
                p.setBirthDate(birth);
                fuzzerContext.addLog(new FuzzOperationResult<>("set BithDate in Patient", org, birth));
            }
        }
    }

    private void extensionFuzz(Patient p) {
        val extensionFuzz = fuzzerContext.getTypeFuzzerFor(Extension.class, () -> new ExtensionFuzzerImpl(fuzzerContext));
        if (!p.hasExtension()) {
            val ex = extensionFuzz.generateRandom();
            p.setExtension(List.of(ex));
            fuzzerContext.addLog(new FuzzOperationResult<>("set Extension in Patient", null, ex.getValue()));
        } else {
            val listFuzzer = new ListFuzzerImpl<>(fuzzerContext, extensionFuzz);
            val org = p.getExtension();
            listFuzzer.fuzz(p::getExtension, p::setExtension);
            fuzzerContext.addLog(new FuzzOperationResult<>("fuzz Extension in Patient", org.get(0).getValue(), p.hasExtension() ? p.getExtension().get(0).getValue() : null));
        }
    }

    private void addressFuzz(Patient p) {
        val addressFuzzer = fuzzerContext.getTypeFuzzerFor(Address.class, () -> new AddressFuzzerImpl(fuzzerContext));
        if (!p.hasAddress()) {
            val address = addressFuzzer.generateRandom();
            p.setAddress(List.of(address));
            fuzzerContext.addLog(new FuzzOperationResult<>("set Address in Patient", null, address));
        } else {
            val listFuzzer = new ListFuzzerImpl<>(fuzzerContext, addressFuzzer);
            listFuzzer.fuzz(p::getAddress, p::setAddress);
            val org = p.getAddress();
            fuzzerContext.addLog(new FuzzOperationResult<>("fuzz Address in Patient", org, p.hasAddress() ? p.getAddress() : null));
        }
    }

    private void codeConFuzz(Patient p) {
        val codeableCon = fuzzerContext.getTypeFuzzerFor(CodeableConcept.class);
        if (!p.hasMaritalStatus()) {
            codeableCon.ifPresent(tf -> p.setMaritalStatus(tf.generateRandom()));
            fuzzerContext.addLog(new FuzzOperationResult<>("set MaritalStatus in Patient", null, p.hasMaritalStatus() ? p.getMaritalStatus() : null));
        } else {
            codeableCon.ifPresent(tf -> tf.fuzz(p::getMaritalStatus, p::setMaritalStatus));
            fuzzerContext.addLog(new FuzzOperationResult<>("fuzz MaritalStatus in Patient", null, p.hasMaritalStatus() ? p.getMaritalStatus() : null));
        }
    }

    @Override
    public FuzzerContext getContext() {
        return fuzzerContext;
    }

    @Override
    public Patient generateRandom() {
        val p = new Patient();
        fuzzerContext.getTypeFuzzerFor(Address.class).ifPresent(tf -> p.setAddress(List.of(tf.generateRandom())));
        fuzzerContext.getTypeFuzzerFor(HumanName.class).ifPresent(tf -> p.setName(List.of(tf.generateRandom())));
        p.setBirthDate(fuzzerContext.getRandomDate());
        fuzzerContext.getTypeFuzzerFor(Identifier.class).ifPresent(tf -> p.setIdentifier(List.of(tf.generateRandom())));
        p.setId(fuzzerContext.getUrlFuzz().generateRandom());
        return p;
    }
}
