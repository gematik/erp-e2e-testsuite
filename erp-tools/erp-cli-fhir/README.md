# E-Rezept FHIR Commandline Tool

## Usage

### Prepare
To [pull](https://docs.docker.com/engine/reference/commandline/pull/) the Docker-Image from DockerHub simply use the following command:

`docker pull gematik1/erp-cli-fhir`

### Start erp-cli-fhir Container
To [run](https://docs.docker.com/engine/reference/commandline/run/) the Docker-Image use the following command:

`docker run --name ${CONTAINER_NAME} -v ${FULL_HOST_PATH}:/mnt/shared -dt gematik1/erp-cli-fhir`

- `${CONTAINER_NAME}` is a name you can freely choose to identify your container
- `${FULL_HOST_PATH}` is the full path of the shared directory on the host machine. _erp-cli-fhir_ will use this directory to write and read the FHIR resources

**NOTE:** the mount-directory `/mnt/shared` for the container is fixed and must not be changed.

#### Examples

`docker run --name erp-cli-fhir -v ~/Desktop/workdir/tmp/fhir_fails/shared:/mnt/shared -dt gematik1/erp-cli-fhir`

### Run erp-cli-fhir in Container
To [execute](https://docs.docker.com/engine/reference/commandline/exec/) the _**erp-cli-fhir**_ inside a running Docker-Container use the following command:

`docker exec ${CONTAINER_NAME} ${CMD} ${PARAMETERS}`

- `${CONTAINER_NAME}` is the name you have chosen for your container in the previous step
- `${CMD}` which should be executed by _**erp-cli-fhir**_
  
    - the only implemented command for now is `example` 
- `${PARAMETERS}` are the parameters which will be passed to _**erp-cli-fhir**_ in the container. For more details read how to [create random FHIR-Resources](#create-random-fhir-resources)

# Commands 

## Create random FHIR-Resources
To create a single random [DAV-PKV-PR-ERP-AbgabedatenBundle](https://simplifier.net/packages/de.abda.erezeptabgabedatenpkv/1.1.0-rc11/files/776371) use the following command:

`docker exec ${CONTAINER_NAME} generate -n 1 --dav ./dav/1_1_0`

**NOTE:** the last argument (`./dav/1_1_0`) must be a relative path (thus stating with `./`). The _**erp-cli-fhir**_ will use the relativ path to generate the output in `/mnt/shared/${RELATIVE_OUT_PATH}` which will be available on `${FULL_HOST_PATH}/${RELATIVE_OUT_PATH}` on your host machine. If you provide an absolute path (or something like `../../`) _**erp-cli-fhir**_ will still place the output there, but you won't be able to access the generated files on your host machine.

To create a single random [KBV_PR_ERP_Bundle](https://simplifier.net/packages/kbv.ita.erp/1.1.0/files/720156) and apply available _mutators_ (which will invalidate the Bundle) use the following command:

`docker exec ${CONTAINER_NAME} generate -n 1 --kbv --invalidate ./kbv/1_0_2`
