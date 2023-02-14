@Library('gematik-jenkins-shared-library') _

String JIRA_PROJECT_ID = 'TSERP'    // will be required to fetch the version from JIRA

String POM_FILE = 'pom.xml'
DOCKER_BUILD_DIR = 'erp-tools/erp-cli-fhir'
String DOCKER_FILE = 'Dockerfile'
String DOCKER_LATEST_TAG = 'latest'

// Project-specific
String IMAGE_NAME = 'erezept/erp-cli-fhir'
String DOCKER_TAG = '0.3.0-SNAPSHOT'    // TODO: retrieve from JIRA/GitLab

pipeline {
    options {
        disableConcurrentBuilds()
        buildDiscarder logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '', daysToKeepStr: '', numToKeepStr: '5')
    }
    agent { label 'k8-maven' }

    parameters {
        string(name: 'IMAGE_VERSION', defaultValue: '', description: 'Version des Images')
    }

    stages {
        stage('Setup') {
            steps {
                gitSetIdentity()
                useJdk('OPENJDK17')
            }
        }

        stage('Build') {
            steps {
                // TODO: check if possible to build only erp-tools/erp-cli-fhir instead of whole project!
                // when building from Development_1.x building whole project should not be required!
                mavenBuild(POM_FILE)
            }
        }

        stage('Docker Build') {
            steps {
                dockerBuild(IMAGE_NAME, DOCKER_TAG, params.IMAGE_VERSION, '', DOCKER_FILE, dockerGetGematikRegistry(), DOCKER_BUILD_DIR)
            }
        }

        stage('Docker Push') {
            steps {
                dockerPushImage(IMAGE_NAME, DOCKER_TAG)
                // Retag as 'latest'
                dockerReTagImage(IMAGE_NAME, DOCKER_LATEST_TAG, DOCKER_TAG)
                dockerPushImage(IMAGE_NAME, DOCKER_LATEST_TAG)
                dockerRemoveLocalImage(IMAGE_NAME, DOCKER_LATEST_TAG)
            }
        }
        stage('Publish Documentation') {
            steps {
                script {
                    dockerLoginGematikRegistry()

                    sh '''
                    mkdir public
                    docker pull eu.gcr.io/gematik-all-infra-prod/shared/gematik-asciidoc-converter:latest
                    docker create --name erpf-doc eu.gcr.io/gematik-all-infra-prod/shared/gematik-asciidoc-converter:latest /tmp/docs/erp-tools/erp-cli-fhir/user_manual.adoc
                    docker cp ''' + pwd() + '''/docs erpf-doc:/tmp
                    docker cp ''' + pwd() + '''/erp-fhir erpf-doc:/tmp
                    docker start --attach erpf-doc
                    docker cp erpf-doc:/tmp/docs/erp-tools/erp-cli-fhir/user_manual.pdf ./public
                    docker cp erpf-doc:/tmp/docs/erp-tools/erp-cli-fhir/user_manual.html ./public
                    '''

                    archiveArtifacts allowEmptyArchive: true, artifacts: '**/public/user_manual.pdf', fingerprint: true
                    publishHTML([allowMissing: true, alwaysLinkToLastBuild: true, keepAll: true, reportDir: "public", reportFiles: 'user_manual.html', reportName: "User Manual ${params.IMAGE_VERSION}", reportTitles: ''])
                }
            }
        }
    }

    post {
        always {
            sh 'docker rm erpf-doc'
            dockerRemoveLocalImage(IMAGE_NAME, DOCKER_TAG)
        }
    }
}