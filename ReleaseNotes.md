# Release Notes ERP E2E Testsuite

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
