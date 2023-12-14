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

package de.gematik.test.erezept.fhir.references;


import org.hl7.fhir.r4.model.Reference;

import static java.text.MessageFormat.format;

public abstract class CustomReferenceProvider {

    protected final String referenceValue;

    protected CustomReferenceProvider(String referenceValue) {
        this.referenceValue = referenceValue;
    }

    protected CustomReferenceProvider(String prefix, String referenceValue) {
        this.referenceValue = referenceValue.startsWith(prefix) ? referenceValue : format("{0}/{1}", prefix, referenceValue);
    }

    public abstract Reference asReference();

    public String getReference() {
        return this.referenceValue;
    }
}
