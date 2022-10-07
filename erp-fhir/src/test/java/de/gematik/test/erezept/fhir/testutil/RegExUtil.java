/*
 * Copyright (c) 2022 gematik GmbH
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

package de.gematik.test.erezept.fhir.testutil;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;
import java.util.regex.Pattern;
import lombok.val;
import org.hl7.fhir.r4.model.AuditEvent;

public class RegExUtil {

  private static final Pattern FHIR_DATETIME =
      Pattern.compile(
          "([0-9]([0-9]([0-9][1-9]|[1-9]0)|[1-9]00)|[1-9]000)(-(0[1-9]|1[0-2])(-(0[1-9]|[1-2][0-9]|3[0-1])(T([01][0-9]|2[0-3]):[0-5][0-9]:([0-5][0-9]|60)(\\.[0-9]+)?(Z|([+\\-])((0[0-9]|1[0-3]):[0-5][0-9]|14:00)))?)?)?");

  private static final Pattern PRESCRIPTION_ID =
      Pattern.compile(
          "<system value=\"https://gematik\\.de/fhir/NamingSystem/PrescriptionID\"\\W/>\\W*<value value=\"([\\w.]+)");

  private static final Pattern AUDIT_EVENT_ACTION = Pattern.compile("\"action\":\\s?\"(\\w)");
  private static final Pattern AUDIT_EVENT_RECORDED =
      Pattern.compile(
          "\"recorded\":\\s?\"(([0-9]([0-9]([0-9][1-9]|[1-9]0)|[1-9]00)|[1-9]000)(-(0[1-9]|1[0-2])(-(0[1-9]|[1-2][0-9]|3[0-1])(T([01][0-9]|2[0-3]):[0-5][0-9]:([0-5][0-9]|60)(\\.[0-9]+)?(Z|([+\\-])((0[0-9]|1[0-3]):[0-5][0-9]|14:00)))?)?)?)");

  private RegExUtil() {
    throw new AssertionError();
  }

  public static Optional<String> getPrescriptionId(final String content) {
    return match(content, PRESCRIPTION_ID, 1);
  }

  public static Optional<AuditEvent.AuditEventAction> getAuditEventAction(final String content) {
    val match = match(content, AUDIT_EVENT_ACTION, 1);
    return match.map(AuditEvent.AuditEventAction::fromCode);
  }

  public static Optional<Date> getAuditEventRecorded(final String content) {
    val match = match(content, AUDIT_EVENT_RECORDED, 1);
    return match.map(
        d -> Date.from(LocalDateTime.parse(d).atZone(ZoneId.systemDefault()).toInstant()));
  }

  private static Optional<String> match(
      final String content, final Pattern pattern, final int groupIdx) {
    val matcher = pattern.matcher(content);
    Optional<String> ret;
    if (matcher.find()) {
      ret = Optional.of(matcher.group(groupIdx));
    } else {
      ret = Optional.empty();
    }
    return ret;
  }
}
