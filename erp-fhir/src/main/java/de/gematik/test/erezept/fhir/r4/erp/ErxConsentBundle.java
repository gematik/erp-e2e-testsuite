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
 */

package de.gematik.test.erezept.fhir.r4.erp;

import ca.uhn.fhir.model.api.annotation.*;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.*;
import java.util.*;
import lombok.*;
import org.hl7.fhir.r4.model.*;

@Getter
@ResourceDef(name = "Bundle")
@SuppressWarnings({"java:S110"})
public class ErxConsentBundle extends Bundle {

  public boolean hasConsent() {
    return getConsent().isPresent();
  }

  public Optional<ErxConsent> getConsent() {
    return this.getEntry().stream()
        .map(BundleEntryComponent::getResource)
        .filter(resource -> resource.getResourceType().equals(ResourceType.Consent))
        .filter(PatientenrechnungStructDef.GEM_ERPCHRG_PR_CONSENT::matches)
        .map(ErxConsent::fromConsent)
        .findFirst();
  }
}
