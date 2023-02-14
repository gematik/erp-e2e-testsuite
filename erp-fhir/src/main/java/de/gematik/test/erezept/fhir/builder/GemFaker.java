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

package de.gematik.test.erezept.fhir.builder;

import static java.text.MessageFormat.format;

import com.github.javafaker.Faker;
import de.gematik.test.erezept.fhir.exceptions.BuilderException;
import de.gematik.test.erezept.fhir.exceptions.FakerException;
import de.gematik.test.erezept.fhir.extensions.kbv.MultiplePrescriptionExtension;
import de.gematik.test.erezept.fhir.resources.erp.ChargeItemCommunicationType;
import de.gematik.test.erezept.fhir.resources.erp.CommunicationType;
import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.fhir.values.DoctorProfession;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.valuesets.Country;
import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
import de.gematik.test.erezept.fhir.valuesets.QualificationType;
import de.gematik.test.erezept.fhir.valuesets.Wop;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.val;

public class GemFaker {
  private static final String DATE_FORMAT_SIMPLE = "dd.MM.yyyy";

  private static final Faker faker = new Faker(new Locale("de"));
  private static final List<String> HEALTH_INSURANCE_NAMES =
      List.of("KOA", "Farmer", "Künstler-Krankenkasse", "KVED Krankenversicherungs-AG");
  private static final List<String> DRUG_NAME_PREFIXES =
      List.of(
          "Aba", "Aca", "Ace", "Aci", "Ada", "Amo", "Aze", "Bac", "Bec", "Bel", "Ben", "Bet", "Bus",
          "Cab", "Cal", "Can", "Cap", "Dab", "Def", "Dex", "Dic", "Dox", "Ecal", "Ecul", "Elag",
          "Enox", "Eren", "Fam", "Fen", "Fes", "Fex", "Flur", "Fom", "Gab", "Gem", "Glim", "Gos",
          "Hem", "Hep", "Hem", "Hom", "Hyd", "Ibal", "Iban", "Ibu", "Ilop");
  private static final List<String> DRUG_NAME_INFIXES =
      List.of("abra", "ali", "ami", "ame", "gluco", "imo", "ter", "vomil", "voflox");
  private static final List<String> DRUG_NAME_POSTFIXES =
      List.of(
          "ab", "an", "at", "cain", "en", "fen", "ib", "id", "um", "mus", "mid", "mib", "nat",
          "nin", "or", "ol", "säure", "tal", "pa", "pam", "pin", "proston", "rat", "zol", "xalon",
          "xital", "xin");

  private GemFaker() {
    throw new AssertionError();
  }

  public static String fakerPzn() {
    return faker.regexify("[0-9]{8}");
  }

  public static String fakerDrugName() {
    val prefix = randomElement(DRUG_NAME_PREFIXES);

    val infix = randomElement(DRUG_NAME_INFIXES);

    val postfix = randomElement(DRUG_NAME_POSTFIXES);
    return prefix + infix + postfix;
  }

  public static String fakerLotNumber() {
    return faker.regexify("[0-9]{10}");
  }

  public static Date fakerFutureExpirationDate() {
    int min = 24 * 7; // at least one week
    int most = 24 * 28; // at most 28 Days
    return faker.date().future(most, min, TimeUnit.HOURS);
  }

  public static String fakerTelematikId() {
    return faker.regexify("[0-9]{8}");
  }

  public static String fakerKvid() {
    return faker.regexify("[A-Z]{1}[0-9]{9}");
  }

  public static String fakerName() {
    return faker.funnyName().name();
  }

  public static String fakerFirstName() {
    return faker.name().firstName();
  }

  public static String fakerLastName() {
    return faker.name().lastName();
  }

  public static Date fakerBirthday() {
    return faker.date().birthday();
  }

  public static String fakerBirthdayAsString() {
    return new SimpleDateFormat(DATE_FORMAT_SIMPLE).format(fakerBirthday());
  }

  /**
   * Identifier der Einrichtung: Institutskennzeichen
   *
   * @see <a href="https://mio.kbv.de/display/BASE1X0/Identifier+der+Einrichtung">IK-Nummer</a>
   * @return random 9-digit number as String
   */
  public static String fakerIknr() {
    return faker.regexify("[0-9]{9}"); // NOSONAR
  }

  /**
   * builds a combination of HEALTH_INSURANCE_NAMES and Wop with max 45 digits uses
   * randomElement(HEALTH_INSURANCE_NAMES) uses wop.getDisplay()
   *
   * @param wop from Wop.class
   * @return StingCombination with max size of 45
   */
  public static String insuranceName(Wop wop) {
    var tmpInsurenceName =
        format("{0} {1}", randomElement(HEALTH_INSURANCE_NAMES), wop.getDisplay());
    if (tmpInsurenceName.length() > 45) {
      tmpInsurenceName = tmpInsurenceName.substring(0, 45);
    }
    return tmpInsurenceName;
  }

  /**
   * generate combination from HEALTH_INSURANCE_NAMES and wop.getDisplay()
   *
   * @return StingCombination with max size of 45
   */
  public static String insuranceName() {
    var rndWop = fakerValueSet(Wop.class);
    while (rndWop.equals(Wop.DUMMY)) {
      rndWop = fakerValueSet(Wop.class); // prevent the DUMMY Wop
    }
    return insuranceName(rndWop);
  }

  public static String pharmacyName() {
    return format("Apotheke {0}", faker.funnyName().name());
  }

  /**
   * Betriebsstättennummer
   *
   * @see <a href="https://www.kbv.de/media/sp/Arztnummern_Richtlinie.pdf"></a>
   * @see <a href="https://reimbursement.institute/glossar/bsnr-betriebsstaettennummer/">BSNR</a>
   *     first and second digits as region number (Wop)
   * @return String (BSNR generated by original format)
   */
  public static String fakerBsnr() {
    var rndWop = fakerValueSet(Wop.class);
    while (rndWop.equals(Wop.DUMMY)) {
      rndWop = fakerValueSet(Wop.class); // prevent the DUMMY Wop
    }
    return rndWop.getCode() + faker.regexify("[0-9]{7}");
  }

  /**
   * Lebenslage Arztnummer uses method generateControlNo(int docID)
   *
   * @see <a href="https://de.wikipedia.org/wiki/Lebenslange_Arztnummer">Wikipedia LANR</a>
   * @see <a href="https://www.kbv.de/media/sp/Arztnummern_Richtlinie.pdf"></a>
   * @see <a href="https://wiki.hl7.de/index.php?title=LANR_und_BSNR>HL7 Wiki</a>
   * @return a random valid LANR as a String
   */
  public static String fakerLanr() {
    String docId = faker.regexify("[0-9]{6}");
    int controlNumber;
    controlNumber = generateControlNo(docId);
    String sectionNo = faker.regexify("[0-9]{2}");
    return format("{0}{1}{2}", docId, controlNumber, sectionNo);
  }
  /**
   * generate a controlNumber by multiply each first digit with 4 and each second digit with 9 until
   * reached last digit (6) result = 10 - ( sum() -> "% 10" );
   *
   * @param docId with 6 digits as String
   * @return 1 digit int
   */
  public static int generateControlNo(String docId) {
    return generateControlNo(Integer.parseInt(docId));
  }

  /**
   * generate a controlNumber by multiply each first digit with 4 and each second digit with 9 until
   * reached last digit (6) result = 10 - ( sum() -> "% 10" );
   *
   * @param docID with 6 digits
   * @return 1 digit int
   */
  public static int generateControlNo(int docID) {
    val difToTen = 10;
    val docIdLength = 6;
    int[] docIdAsArr = new int[docIdLength];
    for (int i = 0; i < docIdAsArr.length; i++) {
      docIdAsArr[docIdLength - 1 - i] = docID % 10;
      docID = docID / 10;
    }
    int result = 0;
    for (int i = 0; i < docIdAsArr.length; i++) {
      if ((i) % 2 == 0) {
        // 1st , 3rd & 5th digit multiply by 4
        result = result + docIdAsArr[i] * 4;
      } else {
        // 2nd, 4th, 6th digit multibly by 9
        result = result + docIdAsArr[i] * 9;
      }
    }
    result = difToTen - result % 10;
    if (result == 10) result = 0;
    return result;
  }

  /**
   * Zahnarztnummer* possible: use ZANR.random() instead
   *
   * <p>uses method generateControlNo(int docID)
   *
   * @see <a
   *     href="https://www.kzbv.de/kzbv-richtlinie-zanr-2021-12-08.download.eba44d2264e87ac03399621ab094f827.pdf">official
   *     pdf from Kassenzahnärztlichen Bundesvereinigung </a>
   * @return a random valid ZANR as a String
   */
  public static String fakerZanr() {
    String docId = faker.regexify("[0-9]{6}");
    int controlNumber = generateControlNo(docId);
    val sectionNo = List.of("50", "91").get(faker.random().nextInt(0, 1));
    return format("{0}{1}{2}", docId, controlNumber, sectionNo);
  }

  public static PrescriptionId fakerPrescriptionId() {
    return fakerPrescriptionId(PrescriptionFlowType.FLOW_TYPE_160);
  }
  /**
   * Build a fake PrescriptionID with valid checking number
   *
   * @see <a
   *     href="https://polarion.int.gematik.de/polarion/#/project/Mainline_OPB1/workitem?id=ML-103379">A_19218</a>
   * @return a valid randomly generated Prescription ID
   */
  public static PrescriptionId fakerPrescriptionId(PrescriptionFlowType flowType) {
    val idUnitWorkflowType = flowType.getCode();
    val idUnitFormat = "[0-9]{3}";

    // create a random Prescription ID without a check number
    val rawIso7064 =
        format(
            "{0}.{1}.{2}.{3}.{4}",
            idUnitWorkflowType,
            faker.regexify(idUnitFormat),
            faker.regexify(idUnitFormat),
            faker.regexify(idUnitFormat),
            faker.regexify(idUnitFormat));

    // parse the Prescription ID to a Long value and multiply by 100 to create an empty (00) check
    // number at the end
    val numIso7064 = Long.parseLong(rawIso7064.replace(".", "")) * 100;

    // calculate the pure check number as Long value
    val checkNumLong = 98 - (numIso7064 % 97);

    // Note: checknum MUST always have 2 digits, so prepend with a zero if smaller 10 which will
    // always ensure 2 digits
    val checkNumString =
        (checkNumLong >= 10) ? String.valueOf(checkNumLong) : format("0{0}", checkNumLong);
    return new PrescriptionId(format("{0}.{1}", rawIso7064, checkNumString));
  }

  public static AccessCode fakerAccessCode() {
    return new AccessCode(faker.regexify("[0-9a-f]{64}"));
  }

  public static String fakerSecret() {
    return faker.regexify("[0-9a-f]{64}");
  }

  public static int fakerAmount() {
    return fakerAmount(1, 20);
  }

  public static int fakerAmount(int min, int max) {
    return faker.random().nextInt(min, max);
  }

  public static boolean fakerBool() {
    return faker.random().nextBoolean();
  }

  public static String fakerDosage() {
    return faker.regexify("([0-3]-){4,6}[0-3]");
  }

  public static String fakerPhone() {
    return faker.phoneNumber().phoneNumber();
  }

  public static String fakerEMail() {
    return eMail(faker.name().firstName(), faker.name().lastName());
  }

  public static String eMail(String firstName, String lasName) {
    val localPart = format("{0}.{1}", firstName, lasName);
    return eMail(localPart);
  }

  public static String eMail(final String localPart) {
    val fixedLocal = localPart.toLowerCase().replace(" ", ".");
    return format("{0}@{1}", fixedLocal, faker.internet().domainName());
  }

  public static String fakerCity() {
    return faker.address().city();
  }

  public static String fakerZipCode() {
    return faker.address().zipCode();
  }

  /**
   * <b>Attention:</b> this might include a secondary appendix to the street name which might be an
   * invalid address in FHIR resources
   *
   * @return a street name
   */
  public static String fullStreetName() {
    return fullStreetName(faker.random().nextBoolean());
  }

  /**
   * <b>Attention:</b> use with caution, if <code>withSecondary</code> argument is set to true, the
   * street might be invalid for certain FHIR profiles
   *
   * @param withSecondary defines if a secondary appendix should be added to the address
   * @return a street name
   */
  public static String fullStreetName(boolean withSecondary) {
    return faker.address().streetAddress(withSecondary);
  }

  public static String fakerStreetName() {
    return faker.address().streetAddress();
  }

  public static String buildingNumber() {
    return faker.address().buildingNumber();
  }

  public static Country fakerCountry() {
    return fakerValueSet(Country.class);
  }

  public static QualificationType fakerQualificationType() {
    return fakerValueSet(QualificationType.class);
  }

  public static String fakerProfession() {
    return faker.company().profession();
  }

  public static String fakerDoctorProfessionAsString() {
    return randomElement(DoctorProfession.values()).getNaming();
  }

  public static DoctorProfession fakerDoctorProfession() {
    return randomElement(DoctorProfession.values());
  }

  public static String fakerCommunicationInfoReqMessage() {
    return faker.hitchhikersGuideToTheGalaxy().marvinQuote();
  }

  public static String fakerCommunicationDispReqMessage() {
    return faker.backToTheFuture().quote();
  }

  public static String fakerCommunicationReplyMessage() {
    return faker.gameOfThrones().quote();
  }

  public static String fakerCommunicationRepresentativeMessage() {
    return faker.chuckNorris().fact();
  }

  public static String fakerCommunicationChargeItemChangeRequest() {
    return faker.buffy().quotes();
  }

  public static String fakerCommunicationChargeItemChangeReply() {
    return faker.dune().quote();
  }

  public static String fakerCommunicationMessage(CommunicationType type) {
    // TODO: this switch case appears on some other places! find a better solution like
    // visitor/command pattern?
    var message = format("No random message was given for message type {0}", type);
    switch (type) {
      case INFO_REQ:
        message = GemFaker.fakerCommunicationInfoReqMessage();
        break;
      case DISP_REQ:
        message = GemFaker.fakerCommunicationDispReqMessage();
        break;
      case REPLY:
        message = GemFaker.fakerCommunicationReplyMessage();
        break;
      case REPRESENTATIVE:
        message = GemFaker.fakerCommunicationRepresentativeMessage();
        break;
    }
    return message;
  }

  public static String fakerChargeItemCommunicationMessage(ChargeItemCommunicationType type) {
    var message = format("No random message was given for message type {0}", type);
    if (type.equals(ChargeItemCommunicationType.CHANGE_REQ)) {
      message = GemFaker.fakerCommunicationChargeItemChangeRequest();
    } else if (type.equals(ChargeItemCommunicationType.CHANGE_REPLY)) {
      message = GemFaker.fakerCommunicationChargeItemChangeReply();
    }
    return message;
  }

  public static MultiplePrescriptionExtension mvo() {
    return mvo(faker.random().nextBoolean());
  }

  public static MultiplePrescriptionExtension mvo(boolean isMultiple) {
    if (!isMultiple) {
      return MultiplePrescriptionExtension.asNonMultiple();
    }

    val denominator = faker.random().nextInt(2, 4);
    val numerator = faker.random().nextInt(1, denominator);
    val startDate = faker.date().future(faker.random().nextInt(1, 30), TimeUnit.DAYS);

    val builder =
        MultiplePrescriptionExtension.asMultiple(numerator, denominator).starting(startDate);

    if (Boolean.TRUE.equals(faker.random().nextBoolean())) {
      val endDate = faker.date().future(faker.random().nextInt(2, 30), TimeUnit.DAYS, startDate);
      return builder.validUntil(endDate);
    } else {
      return builder.withoutEndDate();
    }
  }

  public static float vatRate() {
    return vatRate(10.0f, 30.0f);
  }

  public static float vatRate(float min, float max) {
    if (min <= 0.0f || max >= 100.0f || min >= max) {
      throw new BuilderException(format("Invalid VAT range {0}..{1}", min, max));
    }

    val base = faker.random().nextDouble();
    val diff = max - min;
    return (float) (min + diff * base);
  }

  public static float cost() {
    val minBase = faker.random().nextInt(1, 100);
    val min = (float) (minBase * faker.random().nextDouble());
    return cost(min);
  }

  public static float cost(float min) {
    val maxBase = faker.random().nextInt(1, 10);
    val max = (float) (min + min * maxBase * faker.random().nextDouble());
    return cost(min, max);
  }

  public static float cost(float min, float max) {
    if (min <= 0.0f || max < 0.0f || min >= max) {
      throw new BuilderException(format("Invalid cost range {0}..{1}", min, max));
    }

    val base = faker.random().nextDouble();
    val diff = max - min;
    return (float) (min + diff * base);
  }

  /**
   * Get a random value from a given Enum
   *
   * @param valueSet is the class of the Enum
   * @param <V> is the type of the
   * @return a random choice
   */
  public static <V extends Enum<?>> V fakerValueSet(Class<V> valueSet) {
    return fakerValueSet(valueSet, List.of());
  }

  public static <V extends Enum<?>> V fakerValueSet(Class<V> valueSet, V exclude) {
    return fakerValueSet(valueSet, List.of(exclude));
  }

  public static <V extends Enum<?>> V fakerValueSet(Class<V> valueSet, List<V> exclude) {
    val included =
        Arrays.stream(valueSet.getEnumConstants()).filter(ec -> !exclude.contains(ec)).toList();

    if (included.isEmpty()) {
      throw new FakerException(
          format(
              "List of included choices for {0} is empty: probably all possible choices are excluded {1}",
              valueSet.getSimpleName(),
              exclude.stream().map(Enum::name).collect(Collectors.joining(", "))));
    }

    val idx = faker.random().nextInt(included.size());
    return included.get(idx);
  }

  @SafeVarargs
  public static <T> T randomElement(T... elements) {
    return randomElement(List.of(elements));
  }

  public static <T> T randomElement(List<T> list) {
    val idx = faker.random().nextInt(0, list.size() - 1);
    return list.get(idx);
  }
}
