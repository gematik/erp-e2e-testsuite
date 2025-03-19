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

package de.gematik.test.erezept.fhir.builder.dav;

import de.gematik.test.erezept.fhir.parser.profiles.definitions.AbdaErpBasisStructDef;
import de.gematik.test.erezept.fhir.util.Currency;
import de.gematik.test.erezept.fhir.valuesets.dav.KostenVersicherterKategorie;
import lombok.val;
import org.hl7.fhir.r4.model.Extension;

public class DavExtensions {

  private DavExtensions() {
    throw new IllegalStateException("Utility class");
  }

  public static Extension getInsurantCost(float cost) {
    return getInsurantCost(cost, Currency.EUR);
  }

  public static Extension getInsurantCost(float cost, Currency currency) {
    return getInsurantCost(KostenVersicherterKategorie.ZUZAHLUNG, cost, currency);
  }

  public static Extension getInsurantCost(
      KostenVersicherterKategorie category, float cost, Currency currency) {
    val outer = AbdaErpBasisStructDef.KOSTEN_VERSICHERTER.asExtension();

    val categoryExtension = new Extension("Kategorie", category.asCodeableConcept());
    outer.addExtension(categoryExtension);

    val costExtension = new Extension("Kostenbetrag", currency.asMoney(cost));
    outer.addExtension(costExtension);

    return outer;
  }

  public static Extension getGesamtZuzahlung(Currency currency, float value) {
    val ext = AbdaErpBasisStructDef.GESAMTZUZAHLUNG.asExtension();
    val money = currency.asMoney(value);
    ext.setValue(money);
    return ext;
  }
}
