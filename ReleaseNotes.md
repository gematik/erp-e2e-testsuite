# Release Notes ERP E2E Testsuite

## Release 1.0.0

* erp-fhir:
    - add EuConsentBuilder for NCPeH
    - add EuConsent Commands
    - Remove FHIR profile view `1.1.1`
    - add GemErpMedCompoundingBuilder and GemErpMedicationCompoundingFaker
    - add GemErpMedicationIngredientBuilder and GemErpMedicationIngredientFaker
    - add GemErpMedicationFreeTextBuilder and GemErpMedicationFreeTextFaker
    - add GemErpMedicationCompoundingBuilder and GemErpMedicationCompoundingFaker
    - add new FHIR profile version 1.5.2 for `de.gematik.erezept-workflow.r4`
    - add new FHIR profile version 1.5.2 for `de.gematik.erezeptabgabe.r4`
    - add new FHIR profile version 1.5.2 for `de.gematik.eps.r4`
    - adapt builder and faker for KbvErpMedications to support new FHIR profile version 1.5.2
    - reduce the FHIR profile version 1.3.0 for `de.gematik.erezept-workflow.r4`
    - reduce the FHIR profile version 1.2.0 for `de.gematik.erezept-workflow.r4`
    - reduce switches for FHIR profile version 1.3.0 and 1.2.0
    - update erp-configuration to use ErpWorkFlowVersion 1.5.2 instead of 1.5.0
    - add EuPatchTaskInputBuilder and TaskPatchCommand

* primsys-bbd:
    - for Workflow 162 add new FeatureFile for "diga_usecase" with Tasks and Questions

## Release 0.13.0

* erp-eml:
    - adapt CheckEpaOpProvideDispensation to handle medicationDispenses in Profile 1.3.0 and 1.4.0
* erp-fhir:
    - Update `de.abda.erezeptabgabedatenpkv` to version 1.3.0
* erp-e2e:
    - Add new testcases for C_11881 and C_12143 (Feature Egk in der Apotheke)
    - Add IssueDiGAPrescription for workflow 162
    - Add a new actor type for health insurers as a prerequisite for DiGA use cases.
* erp-fd-product-test:
    - Add new testcases C_12162 and C_12143 (Feature Egk in der Apotheke)
    - Add a new actor type for health insurers as a prerequisite for DiGA use cases.
* primsys-rest:
    - Add a new actor type for health insurers as a prerequisite for DiGA use cases.
* smartcard:
    - add two HBAs without TelematikId in QES Certificates.

## Release 0.10.0

Module:

* erp-e2e:
    - MedicationDispense in ErpProfile version 1.4.0 is enabled in E2E scope while using gematik-Medication and
      ErpDispense in parameter structure
* erp-fd-product-test:
    - Adapted the TestSzenario to validate the availability of PUE and LYE
    - Add toggle to activate Validation for PUE and LYE
    - GemErpMedicationBuilder got a "from()" Method to map KbvMedications to GemErpMedication in types of: ingredient,
      compounding,
      freetext and PZN. you´ll need it to transfer Medications from Version 1.3 and below up to 1.4.x

* erp-fhir:
    - Extension of the key table "Darreichungsform" to include "PUE - Pulver zum Einnehmen" and "LYE - Lyophilisat zum
      Einnehmen"
* smartcard:
    - Adding test identities for DIGA and Egk to the pharmacy

## Release 0.9.1

Module:

* erp-eml:
    - new Epa-Resources available, for Example EpaMedication, EpaMedicationDispense, EpaMedicationRequest,
      EpaOrganisation, EpaPractitioner, ErpPrescriptionId, RxPrescriptionId, EpaOpCancelDispensation,
      EpaOpCancelPrescription, EpaOpProvideDispensation, EpaOpProvideDispensation
    - add Verifier for Eml-MedicationDispense
    - EpaFhir Version 1.0.3
* erp-fd-product-test:
    - new Testcase for epa-op-provide-prescription-erp-input-parameters with
      6 validators for epa-op-provide-prescription-erp-input-parameters
    - new Testcase for epa-op-provide-dispensation-erp-input-parameters with
      5 validators for epa-op-provide-prescription-erp-input-parameters
    - Add two verifiers for epa-op-cancel-prescription-erp-input-parameters
    - Add two verifiers for epa-op-cancel-dispensation-erp-input-parameters
    - new Testcases for epa-op-cancel-prescription-erp-input-parameters
* primsys-rest:
    - Faker REST-API for FHIR-Resources
* Remote FdV
    - Add status in-progress and cancelled to the prescription scheme
    - Add lastMedicationDispense (date-time) to the prescription scheme
* erp-e2e
    - refactored IssuePrescription to build different medicationTypes in E2E Testsuite

## Release 0.8.1

Module:

* erp-eml:
    - Add new Module named epa-eml to test and validate Epa-Fhir content
    - this Module contains an EpaFhirFactory Class, in which the B²ric²s validator is accessible and configurable with
      src/main/resources/fhir/configuration.yaml
    - also the first profiles and Examples are stored
    - Implement epa-mock-client for ePa-Mock
    - Implement polling logic in the ePa mock client for the downloadRequestByKvnr and downloadRequestByPrescriptionId
      functions.
    - Implement epa-mock-client for ePa-Mock
* erp-client:
    - GetOcspRequestParamBuilder to use in GetOCSPRequest
    - C_11530: add generic BundlePagingCommand to get previous or next bundleSet based on specific bundle
    - add QueryParam extracting Method to IQueryParameters
    - extends SearchQueryBuilder in IQueryParameter
    - add QueryParams builder in IQueryParameters for whenHandedOver, whenPrepared, fromPerformer, withOffset, -Count,
      -Sort
    - C_11574: split $dispense and $close operation to do both separately.
    - C_11574: add DispenseOperation and CloseOperation to handle Dispense and Close operations separate
    - C_11574: add new dispense operation without sending a Secret for special Testcases
* erp-fd-product-test:
    - add action GetOCSPRequest to call Certificate from FD for C_11598
    - add action GetTslList to download filter it.
    - add StoreChargeItem with minimal Builder in Pr-Test for C_11617
    - C_11530: add generic DownloadBundle Question
    - C_11530: add GenericBundleVerifier to use on SearchSetBundles for basic BundleStructures
    - C_11530: extends TaskBundleVerifier to handle SearchSetBundles and compare their entries and relations
    - C_11530: add MedicationRequestVerifier to handle MedicationRequests and compare their entries and relations
    - extends CommunicationBundleVerifier to handle SearchSetBundles and compare their entries and relations
    - Extension of the key table "Darreichungsform" to include "Injektions- und Infusionsdispersion (IID)" and "Lösung
      zur intravesikalen Anwendung (LIV)"
    - Implenmented TestSzenario to validate the availability of IID and LIV
    - Add toggle to activate Validation for IID and LIV
    - Implemented TestScenario to test if the KbvBundle without MedicationRequest resource and using MedicationCategory
      different than 00 the FHIR response is equal to 400.
* erp-fhir-fuzzing:
    - Add Manipulator to handle different entries in the KbvBundle.
* erp-fhir:
    - add FHIR E-Rezept Workflow Version 1.3 (de.gematik.erezept-workflow.r4)
* primsys-rest:
    - Communication search endpoint for pharmacies
    - Communication replyWithSender Endpoint &sender=<value>

* konnektor-client:
    - upgrade from `javax.ws` to `jakarta.ws`
* openapi:
    - update ErpTestDriver OpenAPI specification to version 1.0.1

* primsys-bbd:
    - Add new FeatureFile for "Zeitnahe Bereitstellung" with Tasks and Questions

## Release 0.7.0

Implemented Features:

- PrimSysRest - Add a new Endpoint (pharm{id}/withEvidence) to call a list of prescriptions that fits to the given
  evidence
- Add verification steps, to check if Receipts has "fullUrl as urn:uuid", references in composition as "urn:uuid"
- C_11669 - Extends test cases for the feature “Egk in der Pharmacy” with PN3
- C_11595 - Add test case to verify that only e-prescriptions with workflow 160 are retrieved with the feature "eGK in
  pharmacy"
- Implemented _anonymization_ feature for KBV-Bundles in `erp-cli-fhir`
- Implenmented TestSzenario to validate QES-HASH from activated Prescription and Receipt-Binary
- C_11582: Add toggle to activate Validation for ANR-Validation in Error-Configuration
- Add Manipulators in KbvBundleManipulatorFactory.class for MedicationCategory, MedicationType, StandardSize and
  Darreichungsform
- Add actions for RejectConsent, ReadConsent, DeposeConsent, extends their Builder classes and Constructors, add
  Verifier
  for Consent
- CR0071: Extension of the key table "Darreichungsform" to include "Lyophilizate for the preparation of a solution for
  infusion"
- Add action "GetMessage" (as Communications)
- Add actions to call the endpoints OCSPList and CertList from Backend

## Release 0.6.0

Implemented Features:

- TSERP-8: Create different medication types (Medication_Ingredient, Medication_Free_Text und Medication_Compounding)
- TMD-2341: OpenAPI for test drivers of the FdVs
- Implements Testcases to check if PZN, ANR, IKNR, KVNR will validate correctly

## Release 0.5.0

Implemented Features:

- `primsys-rest-client` for `primsys-rest-server` with common DTOs from `primsys-rest-data`
- Renewal of the certificates for Dr. Schraßer because the QES certificates have expired
- Change the default signature and encryption algorithm from RSA to ECC
- Implement configuration for `STRICT`  and `PEDANTIC` FHIR-Validation
- C_11467: Add test cases to verify that the E-Rezept Fachdienst does not return an AccessCode if the pharmacist changes
  the ChargeItem
- C_11399: Add test cases to verify that the AccessCode is not contained in the ChangeItem if it has been changed by the
  pharmacist
- Upgrade Kbv Fhir Package kbv.ita.erp to 1.1.2
- C_11498: Add test cases to verify that the E-Rezept Fachdienst does check identifier KVNR for insured persons

## Release 0.4.0

Implemented Features:

- Implement test cases for the feature "eGK in the pharmacy"
- Improve PKV test cases
- Further development of test data generation, especially for the prescription data set, based on fuzzing concepts
- Improved Fhir Validation of Collection and Searchset Bundles which contain resources from different profile versions
- Implement alternative payor in case of VersicherungsArt BG (Berufsgenossenschaft)

## Release 0.3.0

Implemented features:

- Fhir Profile Support for KBV, DAV, GKV, PKV and gematik for the generation of test data and
  validation of fhir resources from July 1, 2023
    - An overview of the Fhir profile versions can be found
      at https://github.com/gematik/api-erp/blob/master/docs/erp_fhirversion.adoc#%C3%BCbersicht-timeline
- Refactoring of the smartcard module
- Expansion of test cases of the test preparation for the admission tests for the e-prescription
  service
  for version 1.9.0

## Release 0.2.0

Implemented features:

- Additional test cases (E2E and product tests) for the feature "Egk in the pharmacy" are
  implemented
- Support different versions of fhir profiles for validation and generation of fhir resources
- Migration to Java 17

## Release 0.1.3

This is the initial release of the erp e2e testsuite and their modules.
For usage instructions and further information please look at [README.md](README.md)

Implemented features:

- Test scenarios for the e-prescription Worfklow 160 are specified and implemented
- Test scenarios for the e-prescription Worfklow 169 are specified and implemented
- Test scenarios for the e-prescription Worfklow 200 are specified and
  implemented (Expansion Stage 1)