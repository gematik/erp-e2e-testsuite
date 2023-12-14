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

package de.gematik.test.fuzzing.fhirfuzz;

import de.gematik.test.erezept.fhir.builder.kbv.KbvErpBundleBuilder;
import de.gematik.test.fuzzing.core.FuzzingMutator;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzOperationResult;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzerContext;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.Coverage;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Medication;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.Signature;

import java.util.LinkedList;
import java.util.List;

@Slf4j
public class FhirFuzzImpl implements FhirResourceFuzz<Bundle> {
    private final FuzzerContext fuzzerContext;

    public FhirFuzzImpl(FuzzerContext fuzzerContext) {
        this.fuzzerContext = fuzzerContext;
    }

    @Override
    public FuzzerContext getContext() {
        return fuzzerContext;
    }

    @Override
    public Bundle generateRandom() {
        return KbvErpBundleBuilder.faker().build();
    }


    private List<FuzzingMutator<Bundle>> getMutators() {
        val manipulators = new LinkedList<FuzzingMutator<Bundle>>();
        manipulators.add(this::idFuzz);
        manipulators.add(this::identifyFuzz);
        manipulators.add(this::typeFuzz);
        manipulators.add(this::metaFuzz);
        manipulators.add(this::langFuzz);
        manipulators.add(this::signatureFuzz);
        return manipulators;
    }

    @Override

    public Bundle fuzz(Bundle orgBundle) {
        val m = fuzzerContext.getRandomPart(getMutators());
        for (FuzzingMutator<Bundle> f : m) {
            f.accept(orgBundle);
        }
        val entry = orgBundle.getEntry();
        processEntries(entry);
        return orgBundle;
    }

    @SuppressWarnings({"java:S1066", "java:S3776"})
    private void processEntries(List<Bundle.BundleEntryComponent> entries) {
        for (Bundle.BundleEntryComponent e : entries) {
            if (e.getResource() instanceof Composition composition) {
                val fhirResourceFuzz = fuzzerContext.getFuzzerFor(Composition.class);
                if (fuzzerContext.shouldFuzz(composition))
                    fhirResourceFuzz.ifPresent(rf -> rf.fuzz(composition));
            }
            if (e.getResource() instanceof Patient patient) {
                if (fuzzerContext.shouldFuzz(patient))
                    fuzzerContext.getFuzzerFor(Patient.class).ifPresent(rf -> rf.fuzz(patient));
            }
            if (e.getResource() instanceof Bundle bundle && (fuzzerContext.shouldFuzz(bundle))) {
                log.info("Bundle has Bundle as entry and will be called recursive !!!");
                this.fuzz(bundle);
            }
            if (e.getResource() instanceof Medication medication) {
                if (fuzzerContext.shouldFuzz(medication))
                    fuzzerContext.getFuzzerFor(Medication.class).ifPresent(rf -> rf.fuzz(medication));
            }
            if (e.getResource() instanceof MedicationRequest medicationR) {
                if ((fuzzerContext.shouldFuzz(medicationR)))
                    fuzzerContext.getFuzzerFor(MedicationRequest.class).ifPresent(rf -> rf.fuzz(medicationR));
            }

            if (e.getResource() instanceof Coverage coverage) {
                if ((fuzzerContext.shouldFuzz(coverage)))
                    fuzzerContext.getFuzzerFor(Coverage.class).ifPresent(rf -> rf.fuzz(coverage));
            }
            if (e.getResource() instanceof Practitioner practitioner) {
                if ((fuzzerContext.shouldFuzz(practitioner)))
                    fuzzerContext.getFuzzerFor(Practitioner.class).ifPresent(rf -> rf.fuzz(practitioner));
            }
            if (e.getResource() instanceof Organization organization) {
                if ((fuzzerContext.shouldFuzz(organization)))
                    fuzzerContext.getFuzzerFor(Organization.class).ifPresent(rf -> rf.fuzz(organization));
            }

        }
    }

    private void idFuzz(Bundle b) {
        val orgId = b.hasId() ? b.getId() : null;
        fuzzerContext.getIdFuzzer().fuzz(b::hasId, b::getId, b::setId);
        fuzzerContext.addLog(new FuzzOperationResult<>("set ID in Bundle:", orgId, b.hasId() ? b.getId() : null));
    }

    private void signatureFuzz(Bundle b) {
        val sigFuzzer = fuzzerContext.getTypeFuzzerFor(Signature.class);
        val sig = b.hasSignature() ? b.getSignature() : null;
        if (sig == null) {
            sigFuzzer.ifPresent(tf -> b.setSignature(tf.generateRandom()));
            fuzzerContext.addLog(new FuzzOperationResult<>("set Signature in Bundle:", sig, b.getSignature()));
        } else {
            sigFuzzer.ifPresent(tf -> tf.fuzz(b::hasSignature, b::getSignature, b::setSignature));
            fuzzerContext.addLog(new FuzzOperationResult<>("fuzz Signature in Bundle:", sig, b.hasSignature() ? b.getSignature() : null));
        }
    }

    private void identifyFuzz(Bundle b) {
        val ident = b.hasIdentifier() ? b.getIdentifier() : null;
        fuzzerContext.getTypeFuzzerFor(Identifier.class).ifPresent(tf -> tf.fuzz(b::hasIdentifier, b::getIdentifier, b::setIdentifier));
        fuzzerContext.addLog(new FuzzOperationResult<>("set Identifier in Bundle:", ident, b.hasIdentifier() ? b.getIdentifier() : null));
    }

    private void typeFuzz(Bundle b) {
        if (!b.hasType()) {
            val type = fuzzerContext.getRandomOneOfClass(Bundle.BundleType.class, Bundle.BundleType.NULL);
            b.setType(type);
            fuzzerContext.addLog(new FuzzOperationResult<>("set Type in Bundle:", null, type));
        } else {
            val org = b.getType();
            val newType = fuzzerContext.getRandomOneOfClass(Bundle.BundleType.class, List.of(org, Bundle.BundleType.NULL));
            b.setType(newType);
            fuzzerContext.addLog(new FuzzOperationResult<>("fuzz Type in Bundle:", org, newType));
        }
    }

    private void metaFuzz(Bundle b) {
        val meta = b.hasMeta() ? b.getMeta() : null;
        fuzzerContext.getTypeFuzzerFor(Meta.class).ifPresent(tf -> tf.fuzz(b::hasMeta, b::getMeta, b::setMeta));
        fuzzerContext.addLog(new FuzzOperationResult<>("set Meta in Bundle:", meta, b.hasMeta() ? b.getMeta() : null));
    }

    private void langFuzz(Bundle b) {
        if (!b.hasLanguage()) {
            val lang = fuzzerContext.getLanguageCodeFuzzer().generateRandom();
            b.setLanguage(lang);
            fuzzerContext.addLog(new FuzzOperationResult<>("set Language in Bundle:", null, lang));
        } else {
            val lang = b.getLanguage();
            fuzzerContext.getLanguageCodeFuzzer().fuzz(b::getLanguage, b::setLanguage);
            fuzzerContext.addLog(new FuzzOperationResult<>("set Language in Bundle:", lang, b.hasLanguage() ? b.getLanguage() : null));
        }
    }

}
