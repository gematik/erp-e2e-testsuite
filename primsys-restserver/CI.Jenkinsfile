@Library('gematik-jenkins-shared-library') _

def CREDENTIAL_ID_GEMATIK_GIT = 'tst_tt_build_u_p'
def REPO_URL = 'https://build.top.local/source/git/erezept/fachdienst/erp-e2e'
def BRANCH = 'Development_1.x'
def JIRA_PROJECT_ID = 'TMD'
def GITLAB_PROJECT_ID = '843'
def TITLE_TEXT = 'Release'
def version = "latest"
def IMAGE_NAME = "erezept/primsys-rest"

pipeline {
    options {
        skipDefaultCheckout()
        disableConcurrentBuilds()
        buildDiscarder logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '', daysToKeepStr: '', numToKeepStr: '5')
    }
    agent { label 'Docker-Maven' }

    tools {
        maven 'Maven (Standard)'
    }

    parameters {
        //!!! RELEASE_VERION muss über https://wiki.gematik.de/pages/viewpage.action?pageId=139822144#JenkinsReleasePipeline(Executables)-Nutzereingaben angelegt werden!!!
        string(name: 'NEW_VERSION', defaultValue: '', description: 'Bitte die nächste Version für das Projekt eingeben, format [0-9]+.[0-9]+.[0-9]+ \nHinweis: Version 0.0.[0-9] ist keine gültige Version!')
        booleanParam(name: 'Vulnerabilities', defaultValue: true, description: 'Vulnerabilities-Scan')
    }

    stages {
        stage('Initialise') {
            steps {
                checkVersion(params.NEW_VERSION)
                gitSetIdentity()
                //nur für die Testumgebung notwendig!
                useTestBuildServer()
            }
        }

        stage('Checkout') {
            steps {
                git branch: BRANCH,
                        //nur für die Testumgebung notwendig!
                        credentialsId: CREDENTIAL_ID_GEMATIK_GIT,
                        url: REPO_URL
            }
        }

        stage('Build') {
            steps {
                mavenBuild()
            }
        }

//         stage('Test') {
//             steps {
//                 mavenTest()
//             }
//         }

//         stage('Vulnerabilities-Scan Dockerimage') {
//             when { expression { return params.Vulnerabilities } }
//             steps {
//                 trivyVulnerabilitiesScanAllAsHtml(IMAGE_NAME, version)
//             }
//         }

        stage('Push Dockerimage') {
            steps {
                script {
                    def buildDir = "primsys-restserver"
                    def dockerFile = "Dockerfile"
                    def dockerRegistry = dockerGetGematikRegistry()
                    def buildArgs = ""
                    dockerBuild(IMAGE_NAME, version, params.NEW_VERSION, buildArgs, dockerFile, dockerRegistry, buildDir)
                    dockerReTagImage(IMAGE_NAME, params.NEW_VERSION)
                    dockerPushImage(IMAGE_NAME, params.NEW_VERSION)
                }
            }
        }
    }
    post {
        always {
            dockerRemoveLocalImage(IMAGE_NAME, version)
        }
    }
}
