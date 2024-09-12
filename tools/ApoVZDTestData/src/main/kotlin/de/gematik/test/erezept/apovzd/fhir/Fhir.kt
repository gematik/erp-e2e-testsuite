/*
 * Copyright 2024 gematik GmbH
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

package de.gematik.test.erezept.apovzd.fhir

import ca.uhn.fhir.context.FhirContext
import org.hl7.fhir.instance.model.api.IBaseResource
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.InstantType
import org.hl7.fhir.r4.model.Resource
import java.util.*


private val ctx = FhirContext.forR4()

internal fun IBaseResource.toJson(): String {
  val parser = ctx.newJsonParser();
  return parser.encodeResourceToString(this)
}

internal fun <T : Resource> newResource(factory: () -> T, id: UUID? = UUID.randomUUID()) =
  factory().also { resource ->
    resource.id = id.toString()
    resource.meta.let { meta ->
      meta.lastUpdatedElement = InstantType.now()
      meta.versionId = "1"
    }
  }

internal fun Resource.toComponent() = Bundle.BundleEntryComponent().also {
  it.fullUrl = "${this.fhirType()}/${this.id}"
  it.resource = this
}

internal fun <T : Bundle> newBundle(
  factory: () -> T,
  bundleType: Bundle.BundleType? = Bundle.BundleType.COLLECTION,
  total: Int = 0
) = newResource(factory).also { bundle ->
  bundle.type = bundleType
  bundle.total = total
}