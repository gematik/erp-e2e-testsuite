package de.gematik.test.erezept.app.mobile.elements;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.app.exceptions.UnsupportedPlatformException;
import de.gematik.test.erezept.app.mobile.PlatformType;
import io.appium.java_client.AppiumBy;
import java.util.List;
import java.util.function.Supplier;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.val;
import org.openqa.selenium.By;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ErrorAlert implements PageElement {

  public static ErrorAlert pageElement() {
    return new ErrorAlert();
  }

  @Override
  public String getElementName() {
    return "Generic App Error Alert";
  }

  @Override
  public String extractSourceLabel(PlatformType platform) {
    if (!platform.equals(PlatformType.IOS)) {
      throw new UnsupportedPlatformException(platform, "Error Alert Handling");
    }

    /* Important:
    this one will be used for exact string-match on the page-source and must therefore be not changed without reason.
    The main difference to getIosLocator, for example, is the spaces before and after the equal-sign
    */
    return "type=\"XCUIElementTypeAlert\"";
  }

  @Override
  public Supplier<By> getAndroidLocator() {
    return () -> null;
  }

  @Override
  public Supplier<By> getIosLocator() {
    return () -> AppiumBy.iOSNsPredicateString("type = \"XCUIElementTypeAlert\"");
  }

  public String extractErrorMessage(List<String> errorLines) {
    val sb = new StringBuilder();
    errorLines.stream()
        .map(line -> line.replace("\n", " "))
        .forEach(
            line -> {
              if (line.contains("Fehlernummer")) {
                val errorCodeMessage = line.substring(line.indexOf("Fehlernummer"));
                val errorCodes = errorCodeMessage.split(":")[1];
                sb.append(errorCodes);
              } else {
                sb.append(format("{0}: ", line));
              }
            });
    return sb.toString();
  }
}
