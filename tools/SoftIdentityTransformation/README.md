# SoftIdentity Transformation

This tool shall create CardImages and CardConfiguration files for CATS from p12 files containing
the relevant certificates and private keys. In addition, the file "images.json" is generated for
the test suite. This tool is for internal use only. It shall not be provided on gitHub at the
moment.

## Motivation

SoftIdentities are used to communicate with e-rezept components without real cards,
for example to authenticate for a specific role or user.

For the e2e test it is necessary that these identities are also available for the card terminal
simulation CATS. For CATS, we do not have a suitable CardImage for every identity. Therefore, the
existing SoftIdentities have to be transformed into CardImages and CardConfigurations files.

## Execution with Maven

It must be ensured that the erp-crypto and smart card modules are installed locally. Working
Directory
is the erp-e2e folder.

- mvn install -DskipTests

After that you can start with:

- mvn -f tools/SoftIdentityTransformation/pom.xml test compile
- mvn -f tools/SoftIdentityTransformation/pom.xml test compile exec:java -Dexec.mainClass="
  de.gematik.test.erezept.MainKt" -Dexec.args="smartcard/src/main/resources/cardimages/
  tools/SoftIdentityTransformation/target"

Currently, the transfer of the program arguments is still kept simple. There is one argument which
describes where the p12 files are located. Another argument describes the destination, where the
generated files should be stored.

## Todo's:

- better program argument handling
- Support for OSIG certificates