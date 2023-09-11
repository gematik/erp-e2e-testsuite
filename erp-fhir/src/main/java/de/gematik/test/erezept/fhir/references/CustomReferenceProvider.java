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
