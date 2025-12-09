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
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 */

package de.gematik.test.erezept.fhir.parser;

import ca.uhn.fhir.context.FhirContext;
import de.gematik.bbriccs.fhir.codec.ResourceTypeHint;
import de.gematik.bbriccs.fhir.conf.ProfilesConfigurator;
import de.gematik.bbriccs.fhir.validation.DummyValidator;
import de.gematik.bbriccs.fhir.validation.ReferenzValidator;
import de.gematik.bbriccs.fhir.validation.ValidatorFhir;
import de.gematik.bbriccs.fhir.validation.ValidatorFhirFactory;
import de.gematik.refv.SupportedValidationModule;
import de.gematik.refv.ValidationModuleFactory;
import de.gematik.test.erezept.eml.fhir.profile.EpaMedicationStructDef;
import de.gematik.test.erezept.eml.fhir.profile.EpaMedicationVersion;
import de.gematik.test.erezept.eml.fhir.r4.EpaMedPznIngredient;
import de.gematik.test.erezept.fhir.profiles.definitions.*;
import de.gematik.test.erezept.fhir.profiles.version.*;
import de.gematik.test.erezept.fhir.r4.dav.DavPkvAbgabedatenBundle;
import de.gematik.test.erezept.fhir.r4.erp.ErxAuditEvent;
import de.gematik.test.erezept.fhir.r4.erp.ErxChargeItem;
import de.gematik.test.erezept.fhir.r4.erp.ErxCommunication;
import de.gematik.test.erezept.fhir.r4.erp.ErxMedicationDispense;
import de.gematik.test.erezept.fhir.r4.erp.ErxMedicationDispenseDiGA;
import de.gematik.test.erezept.fhir.r4.erp.ErxTask;
import de.gematik.test.erezept.fhir.r4.erp.GemErpMedication;
import de.gematik.test.erezept.fhir.r4.eu.*;
import de.gematik.test.erezept.fhir.r4.kbv.KbvCoverage;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpBundle;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpMedication;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpMedicationRequest;
import de.gematik.test.erezept.fhir.r4.kbv.KbvEvdgaBundle;
import de.gematik.test.erezept.fhir.r4.kbv.KbvMedicalOrganization;
import de.gematik.test.erezept.fhir.r4.kbv.KbvPatient;
import de.gematik.test.erezept.fhir.r4.kbv.KbvPractitioner;
import de.gematik.test.erezept.fhir.r4.kbv.KbvPractitionerRole;
import java.util.*;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.r4.model.Configuration;

@Slf4j
public class ProfileFhirParserFactory {

  public static final String ERP_FHIR_PROFILES_CONFIG = "fhir/erp-configuration.yaml";
  public static final String ERP_FHIR_PROFILES_TOGGLE = "erp.fhir.profile";
  private static final List<ResourceTypeHint<?, ?>> resourceTypeHints =
      List.of(
          ResourceTypeHint.forStructure(GemErpEuStructDef.CONSENT)
              .forAllVersionsFrom(EuVersion.class)
              .mappingTo(EuConsent.class),
          ResourceTypeHint.forStructure(KbvItaErpStructDef.BUNDLE)
              .forAllVersionsFrom(KbvItaErpVersion.class)
              .mappingTo(KbvErpBundle.class),
          ResourceTypeHint.forStructure(KbvItaErpStructDef.PRESCRIPTION)
              .forAllVersionsFrom(KbvItaErpVersion.class)
              .mappingTo(KbvErpMedicationRequest.class),
          ResourceTypeHint.forStructure(KbvItaErpStructDef.MEDICATION_PZN)
              .forAllVersionsFrom(KbvItaErpVersion.class)
              .mappingTo(KbvErpMedication.class),
          ResourceTypeHint.forStructure(KbvItvEvdgaStructDef.BUNDLE)
              .forAllVersionsFrom(KbvItvEvdgaVersion.class)
              .mappingTo(KbvEvdgaBundle.class),
          ResourceTypeHint.forStructure(KbvItaForStructDef.PRACTITIONER)
              .forAllVersionsFrom(KbvItaForVersion.class)
              .mappingTo(KbvPractitioner.class),
          ResourceTypeHint.forStructure(KbvItaForStructDef.PRACTITIONER_ROLE)
              .forAllVersionsFrom(KbvItaForVersion.class)
              .mappingTo(KbvPractitionerRole.class),
          ResourceTypeHint.forStructure(KbvItaForStructDef.ORGANIZATION)
              .forAllVersionsFrom(KbvItaForVersion.class)
              .mappingTo(KbvMedicalOrganization.class),
          ResourceTypeHint.forStructure(KbvItaForStructDef.COVERAGE)
              .forAllVersionsFrom(KbvItaForVersion.class)
              .mappingTo(KbvCoverage.class),
          ResourceTypeHint.forStructure(KbvItaForStructDef.PATIENT)
              .forAllVersionsFrom(KbvItaForVersion.class)
              .mappingTo(KbvPatient.class),
          ResourceTypeHint.forStructure(ErpWorkflowStructDef.TASK)
              .forAllVersionsFrom(ErpWorkflowVersion.class)
              .mappingTo(ErxTask.class),
          ResourceTypeHint.forStructure(ErpWorkflowStructDef.AUDIT_EVENT)
              .forAllVersionsFrom(ErpWorkflowVersion.class)
              .mappingTo(ErxAuditEvent.class),
          ResourceTypeHint.forStructure(ErpWorkflowStructDef.MEDICATION)
              .forAllVersionsFrom(ErpWorkflowVersion.class)
              .mappingTo(GemErpMedication.class),
          ResourceTypeHint.forStructure(ErpWorkflowStructDef.MEDICATION_DISPENSE)
              .forAllVersionsFrom(ErpWorkflowVersion.class)
              .mappingTo(ErxMedicationDispense.class),
          ResourceTypeHint.forStructure(ErpWorkflowStructDef.MEDICATION_DISPENSE_12)
              .forAllVersionsFrom(ErpWorkflowVersion.class)
              .mappingTo(ErxMedicationDispense.class),
          ResourceTypeHint.forStructure(ErpWorkflowStructDef.MEDICATION_DISPENSE_DIGA)
              .forAllVersionsFrom(ErpWorkflowVersion.class)
              .mappingTo(ErxMedicationDispenseDiGA.class),
          ResourceTypeHint.forStructure(ErpWorkflowStructDef.COM_DISP_REQ)
              .forAllVersionsFrom(ErpWorkflowVersion.class)
              .mappingTo(ErxCommunication.class),
          ResourceTypeHint.forStructure(ErpWorkflowStructDef.COM_INFO_REQ)
              .forAllVersionsFrom(ErpWorkflowVersion.class)
              .mappingTo(ErxCommunication.class),
          ResourceTypeHint.forStructure(ErpWorkflowStructDef.COM_REPLY)
              .forAllVersionsFrom(ErpWorkflowVersion.class)
              .mappingTo(ErxCommunication.class),
          ResourceTypeHint.forStructure(ErpWorkflowStructDef.COM_REPRESENTATIVE)
              .forAllVersionsFrom(ErpWorkflowVersion.class)
              .mappingTo(ErxCommunication.class),
          ResourceTypeHint.forStructure(PatientenrechnungStructDef.CHARGE_ITEM)
              .forAllVersionsFrom(PatientenrechnungVersion.class)
              .mappingTo(ErxChargeItem.class),
          ResourceTypeHint.forStructure(PatientenrechnungStructDef.COM_CHARGE_CHANGE_REPLY)
              .forAllVersionsFrom(PatientenrechnungVersion.class)
              .mappingTo(ErxCommunication.class),
          ResourceTypeHint.forStructure(PatientenrechnungStructDef.COM_CHARGE_CHANGE_REQ)
              .forAllVersionsFrom(PatientenrechnungVersion.class)
              .mappingTo(ErxCommunication.class),
          ResourceTypeHint.forStructure(AbdaErpPkvStructDef.PKV_ABGABEDATENSATZ)
              .forAllVersionsFrom(AbdaErpPkvVersion.class)
              .mappingTo(DavPkvAbgabedatenBundle.class),
          ResourceTypeHint.forStructure(EpaMedicationStructDef.MEDICATION_PZN_INGREDIENT)
              .forAllVersionsFrom(EpaMedicationVersion.class)
              .mappingTo(EpaMedPznIngredient.class),
          ResourceTypeHint.forStructure(GemErpEuStructDef.EU_PRACTITIONER)
              .forAllVersionsFrom(EuVersion.class)
              .mappingTo(EuPractitioner.class),
          ResourceTypeHint.forStructure(GemErpEuStructDef.EU_PRACTITIONER_ROLE)
              .forAllVersionsFrom(EuVersion.class)
              .mappingTo(EuPractitionerRole.class),
          ResourceTypeHint.forStructure(GemErpEuStructDef.EU_ORGANIZATION)
              .forAllVersionsFrom(EuVersion.class)
              .mappingTo(EuOrganization.class),
          ResourceTypeHint.forStructure(GemErpEuStructDef.EU_DISPENSATION)
              .forAllVersionsFrom(EuVersion.class)
              .mappingTo(EuMedicationDispense.class),
          ResourceTypeHint.forStructure(GemErpEuStructDef.EU_MEDICATION)
              .forAllVersionsFrom(EuVersion.class)
              .mappingTo(EuMedication.class));
  private static final Map<ValidatorType, ValidatorFhir> validatorCache =
      new EnumMap<>(ValidatorType.class);

  static {
    /* this will force HAPI to produce error messages in english; by that we can filter messages reliably */
    Locale.setDefault(new Locale("en", "DE"));
  }

  private ProfileFhirParserFactory() {
    throw new IllegalStateException("Utility class");
  }

  public static FhirContext createDecoderContext() {
    val ctx = FhirContext.forR4();

    resourceTypeHints.forEach(th -> th.register(ctx));
    return ctx;
  }

  public static ValidatorFhir getDefaultValidator() {
    return getValidatorFor(ValidatorType.BRICKS);
  }

  public static ValidatorFhir getValidatorFor(ValidatorType validatorType) {
    Configuration.setAcceptInvalidEnums(true); // can be made configurable if required

    // erpConfigurator is required here to load the profile context for the builders as well
    val erpConfigurator =
        ProfilesConfigurator.getConfiguration(ERP_FHIR_PROFILES_CONFIG, ERP_FHIR_PROFILES_TOGGLE);
    return switch (validatorType) {
      case NONE -> validatorCache.computeIfAbsent(
          validatorType, type -> new DummyValidator(FhirContext.forR4()));
      case BRICKS -> validatorCache.computeIfAbsent(
          validatorType, type -> createBricksValidator(erpConfigurator));
      case REF_VAL -> validatorCache.computeIfAbsent(
          validatorType, type -> createReferenzValidator());
    };
  }

  @SneakyThrows
  private static ValidatorFhir createReferenzValidator() {
    val erpModule =
        new ValidationModuleFactory().createValidationModule(SupportedValidationModule.ERP);

    return ReferenzValidator.withValidationModule(FhirContext.forR4(), erpModule);
  }

  private static ValidatorFhir createBricksValidator(ProfilesConfigurator erpConfigurator) {
    return ValidatorFhirFactory.createValidator(
        FhirContext.forR4(), erpConfigurator.getProfileConfigurations());
  }
}
