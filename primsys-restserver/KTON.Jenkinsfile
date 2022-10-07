@Library('gematik-jenkins-shared-library') _

def CREDENTIAL_ID_GEMATIK_GIT = 'tst_tt_build_u_p'
def REPO_URL = 'https://build.top.local/source/git/erezept/fachdienst/erp-e2e'
def BRANCH = 'kal/tmd-1507'
def JIRA_PROJECT_ID = 'TMD'
def GITLAB_PROJECT_ID = '843'
def TITLE_TEXT = 'Release'
def version = "latest"
def IMAGE_NAME = "erezept/kthon"

properties([
    buildDiscarder(
        logRotator(
            artifactDaysToKeepStr:      '',
            artifactNumToKeepStr:       '',
            daysToKeepStr:              '',
            numToKeepStr:               '5'
        )
    ),
    parameters ([
        choice(name: 'Environment', choices: ['TU', 'RU'], description: 'Testumgebung'),
        choice(name: 'Agent', choices: ['Docker-Maven', 'LTU_DEV_VM', 'LTU_DEV', 'LTU_RU', 'LTU_PROD', 'LTU_DEV_QS'], description: 'Docker Runtime Stage'),
    ])
])

pipeline {
    options {
        skipDefaultCheckout()
        disableConcurrentBuilds()
        buildDiscarder logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '', daysToKeepStr: '', numToKeepStr: '5')
    }

    agent { label Agent }

    tools {
        maven 'Maven (Standard)'
    }

    stages {
        stage('Initialise') {
            steps {
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

        stage('Run') {
            steps {
                script {
                    sh "java -jar primsys-restserver/target/kthon-jar-with-dependencies.jar"
                }
            }
        }
    }
}
