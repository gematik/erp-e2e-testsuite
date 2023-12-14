package de.gematik.test.erezept.fhir.builder;

import de.gematik.test.erezept.fhir.parser.profiles.systems.CommonCodeSystem;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.hl7.fhir.r4.model.Quantity;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class QuantityBuilder {

  private final Quantity quantity;

  public static QuantityBuilder asUcumPackage() {
    return asUcum("{Package}");
  }

  public static QuantityBuilder asUcum(String code) {
    val q = new Quantity();
    q.setSystem(CommonCodeSystem.UCUM.getCanonicalUrl()).setCode(code);
    return new QuantityBuilder(q);
  }

  public Quantity withValue(int amount) {
    return this.quantity.setValue(amount);
  }
}
