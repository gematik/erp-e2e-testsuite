#!/bin/sh

#
# Copyright 2023 gematik GmbH
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

if [ $# -eq 0 ] ; then
    echo "No Module selected"
    exit 1
fi

shopt -s nocasematch
doc_dir=""
case $1 in
  ("testsuites") doc_dir="testsuites";;
  ("erpf") doc_dir="erp-cli-fhir";;
  ("erpp") doc_dir="erp-cli-patient";;
  ("primsys") doc_dir="primsys-rest";;
  (*) echo "$1 is not a valid module to create a user manual for"; exit 1;;
esac

echo "Create user manual $1 from $doc_dir"

docker create --name erp-doc eu.gcr.io/gematik-all-infra-prod/shared/gematik-asciidoc-converter:latest /tmp/doc/$doc_dir/user_manual.adoc
docker cp ../docs erp-doc:/tmp/doc
docker start --attach erp-doc
mkdir ./target
docker cp erp-doc:/tmp/doc/$doc_dir/user_manual.pdf ./target/
docker rm erp-doc