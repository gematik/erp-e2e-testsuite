#!/bin/sh

if [ $# -eq 0 ] ; then
    echo "No Module selected"
    exit 1
fi

shopt -s nocasematch
doc_dir=""
case $1 in
  ("testsuites") doc_dir="testsuites";;
  ("erpf") doc_dir="erp-cli-fhir";;
  (*) echo "$1 is not a valid module to create a user manual for"; exit 1;;
esac

echo "Create user manual $1 from $doc_dir"

docker create --name erp-doc eu.gcr.io/gematik-all-infra-prod/shared/gematik-asciidoc-converter:latest /tmp/doc/$doc_dir/user_manual.adoc
docker cp ../docs erp-doc:/tmp/doc
docker start --attach erp-doc
mkdir ./target
docker cp erp-doc:/tmp/doc/$doc_dir/user_manual.pdf ./target/
docker rm erp-doc