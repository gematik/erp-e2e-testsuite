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

package de.gematik.test.erezept.fhir.parser.profiles;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.support.IValidationSupport;
import ca.uhn.fhir.validation.FhirValidator;
import de.gematik.test.erezept.fhir.parser.ErrorMessageFilter;
import de.gematik.test.erezept.fhir.parser.profiles.cfg.ParserConfigurations;
import de.gematik.test.erezept.fhir.parser.profiles.cfg.ProfilesIndex;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.*;
import de.gematik.test.erezept.fhir.parser.profiles.version.ProfileVersion;
import de.gematik.test.erezept.fhir.resources.dav.DavAbgabedatenBundle;
import de.gematik.test.erezept.fhir.resources.erp.ErxAuditEvent;
import de.gematik.test.erezept.fhir.resources.erp.ErxChargeItem;
import de.gematik.test.erezept.fhir.resources.erp.ErxCommunication;
import de.gematik.test.erezept.fhir.resources.erp.ErxMedicationDispense;
import de.gematik.test.erezept.fhir.resources.erp.ErxMedicationDispenseDiGA;
import de.gematik.test.erezept.fhir.resources.erp.ErxTask;
import de.gematik.test.erezept.fhir.resources.erp.GemErpMedication;
import de.gematik.test.erezept.fhir.resources.kbv.*;
import java.util.*;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.common.hapi.validation.support.InMemoryTerminologyServerValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.SnapshotGeneratingValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.ValidationSupportChain;
import org.hl7.fhir.common.hapi.validation.validator.FhirInstanceValidator;
import org.hl7.fhir.r4.model.Configuration;
import org.hl7.fhir.r4.model.Resource;

@Slf4j
public class ProfileFhirParserFactory {

  static {
    /* this will force HAPI to produce error messages in english; by that we can filter messages reliably */
    Locale.setDefault(new Locale("en", "DE"));
  }

  private static final Map<CustomProfiles, List<TypeHint<?, ?>>> TYPE_HINTS =
      Map.of(
          CustomProfiles.KBV_ITA_ERP,
          List.of(
              new TypeHint<>(KbvItaErpStructDef.BUNDLE, KbvErpBundle.class),
              new TypeHint<>(KbvItaErpStructDef.PRESCRIPTION, KbvErpMedicationRequest.class),
              new TypeHint<>(KbvItaErpStructDef.MEDICATION_PZN, KbvErpMedication.class)),
          CustomProfiles.KBV_ITA_FOR,
          List.of(
              new TypeHint<>(KbvItaForStructDef.PRACTITIONER, KbvPractitioner.class),
              new TypeHint<>(KbvItaForStructDef.PRACTITIONER_ROLE, KbvPractitionerRole.class),
              new TypeHint<>(KbvItaForStructDef.ORGANIZATION, MedicalOrganization.class),
              new TypeHint<>(KbvItaForStructDef.COVERAGE, KbvCoverage.class),
              new TypeHint<>(KbvItaForStructDef.PATIENT, KbvPatient.class)),
          CustomProfiles.KBV_ITV_EVDGA,
          List.of(
              new TypeHint<>(KbvItvEvdgaStructDef.BUNDLE, KbvEvdgaBundle.class),
              new TypeHint<>(KbvItvEvdgaStructDef.HEALTH_APP_REQUEST, KbvHealthAppRequest.class)),
          CustomProfiles.GEM_PATIENTENRECHNUNG,
          List.of(
              new TypeHint<>(PatientenrechnungStructDef.CHARGE_ITEM, ErxChargeItem.class),
              new TypeHint<>(
                  PatientenrechnungStructDef.COM_CHARGE_CHANGE_REPLY, ErxCommunication.class),
              new TypeHint<>(
                  PatientenrechnungStructDef.COM_CHARGE_CHANGE_REQ, ErxCommunication.class)),
          CustomProfiles.GEM_ERP_WORKFLOW,
          List.of(
              new TypeHint<>(ErpWorkflowStructDef.TASK, ErxTask.class),
              new TypeHint<>(ErpWorkflowStructDef.TASK_12, ErxTask.class),
              new TypeHint<>(ErpWorkflowStructDef.AUDIT_EVENT, ErxAuditEvent.class),
              new TypeHint<>(ErpWorkflowStructDef.CHARGE_ITEM, ErxChargeItem.class),
              new TypeHint<>(ErpWorkflowStructDef.MEDICATION, GemErpMedication.class),
              new TypeHint<>(ErpWorkflowStructDef.MEDICATION_DISPENSE, ErxMedicationDispense.class),
              new TypeHint<>(
                  ErpWorkflowStructDef.MEDICATION_DISPENSE_12, ErxMedicationDispense.class),
              new TypeHint<>(
                  ErpWorkflowStructDef.MEDICATION_DISPENSE_DIGA, ErxMedicationDispenseDiGA.class),
              new TypeHint<>(ErpWorkflowStructDef.COM_DISP_REQ, ErxCommunication.class),
              new TypeHint<>(ErpWorkflowStructDef.COM_DISP_REQ_12, ErxCommunication.class),
              new TypeHint<>(ErpWorkflowStructDef.COM_INFO_REQ, ErxCommunication.class),
              new TypeHint<>(ErpWorkflowStructDef.COM_INFO_REQ_12, ErxCommunication.class),
              new TypeHint<>(ErpWorkflowStructDef.COM_REPLY, ErxCommunication.class),
              new TypeHint<>(ErpWorkflowStructDef.COM_REPLY_12, ErxCommunication.class),
              new TypeHint<>(ErpWorkflowStructDef.COM_REPRESENTATIVE, ErxCommunication.class),
              new TypeHint<>(ErpWorkflowStructDef.COM_REPRESENTATIVE_12, ErxCommunication.class)),
          CustomProfiles.ABDA_ERP_ABGABE_PKV,
          List.of(
              new TypeHint<>(AbdaErpPkvStructDef.PKV_ABGABEDATENSATZ, DavAbgabedatenBundle.class)));

  private static List<FhirProfiledValidator> profiledParsers;

  private ProfileFhirParserFactory() {
    throw new IllegalStateException("Utility class");
  }

  public static FhirContext createDecoderContext() {
    val ctx = FhirContext.forR4();

    // register the type hints to the FhirContext
    TYPE_HINTS.forEach((profile, hints) -> hints.forEach(th -> th.register(ctx, profile)));
    return ctx;
  }

  public static FhirValidator createGenericFhirValidator(FhirContext ctx) {
    val validator = ctx.newValidator();
    val validationSupports = new ArrayList<IValidationSupport>();
    validationSupports.add(ctx.getValidationSupport());
    validationSupports.add(new InMemoryTerminologyServerValidationSupport(ctx));
    validationSupports.add(new SnapshotGeneratingValidationSupport(ctx));

    // configure the HAPI FhirParser
    val fiv = new FhirInstanceValidator(ctx);
    val validationSupportChain =
        new ValidationSupportChain(validationSupports.toArray(IValidationSupport[]::new));

    fiv.setValidationSupport(validationSupportChain);
    fiv.setErrorForUnknownProfiles(false);
    fiv.setNoTerminologyChecks(true);
    fiv.setNoExtensibleWarnings(true);
    fiv.setAnyExtensionsAllowed(true);

    validator.registerValidatorModule(fiv);

    val parserConfigurations = ParserConfigurations.getInstance();
    val filter =
        parserConfigurations.getProfileSettings().stream()
            .flatMap(cfg -> cfg.getErrorFilter().stream())
            .distinct()
            .collect(Collectors.toList());

    // the generic validator does not know any profiles!
    filter.add("^Profile reference '.*' has not been checked because it is unknown$");
    filter.add("^Unknown extension .*");

    validator.registerValidatorModule(new ErrorMessageFilter(filter));
    return validator;
  }

  public static List<FhirProfiledValidator> getProfiledValidators() {
    if (profiledParsers == null) {
      profiledParsers = createProfiledValidators();
    }

    return profiledParsers;
  }

  @SneakyThrows
  private static List<FhirProfiledValidator> createProfiledValidators() {
    val profilesIndex = ProfilesIndex.getInstance();
    val parserConfigurations = ParserConfigurations.getInstance();

    Configuration.setAcceptInvalidEnums(true); // can be made configurable if required

    val parsers = new LinkedList<FhirProfiledValidator>();
    parserConfigurations
        .getProfileSettings()
        .forEach(
            parserCfg -> {
              val ctx = FhirContext.forR4();
              val supports =
                  parserCfg.getProfiles().stream()
                      .map(profile -> profilesIndex.getProfile(profile.getVersionedProfile()))
                      .map(
                          profileSourceDto ->
                              (IValidationSupport)
                                  new ValidationSupport(
                                      profileSourceDto.getVersionedProfile(),
                                      profileSourceDto.getFiles(),
                                      ctx))
                      .toList();
              parsers.add(new FhirProfiledValidator(parserCfg, ctx, supports));
            });

    return parsers;
  }

  private record TypeHint<T extends ProfileVersion<T>, R extends Resource>(
      IStructureDefinition<T> definition, Class<R> mappingClass) {

    @SuppressWarnings("unchecked")
    private void register(FhirContext ctx, CustomProfiles profile) {
      // register to the default StructureDefinition without any version
      ctx.setDefaultTypeForProfile(definition.getCanonicalUrl(), mappingClass);

      // get the corresponding enum class holding all available version for this profile
      val versionEnumClass = profile.getVersionClass();

      // register each available version
      Arrays.stream(versionEnumClass.getEnumConstants())
          .forEach(
              version -> {
                // register StructureDefinition with full SemVer e.g. http://my.profile|1.2.3
                ctx.setDefaultTypeForProfile(
                    definition.getVersionedUrl((T) version, false), mappingClass);

                // register StructureDefinition with cropped Patch from SemVer e.g.
                // http://my.profile|1.2
                ctx.setDefaultTypeForProfile(
                    definition.getVersionedUrl((T) version, true), mappingClass);
              });
    }
  }
}
