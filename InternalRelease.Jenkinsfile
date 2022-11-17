@Library('gematik-jenkins-shared-library') _
def CREDENTIAL_ID_GEMATIK_GIT = 'svc_gitlab_prod_credentials'
def REPO_URL = createGitUrl('git/erezept/fachdienst/erp-e2e')
def BRANCH = 'Development_1.x'
def JIRA_PROJECT_ID = 'TSERP'
def GITLAB_PROJECT_ID = '843'
def TITLE_TEXT = 'Release'
def POM_PATH = 'pom.xml'


pipeline {
    options {
        disableConcurrentBuilds()
        skipDefaultCheckout()
        buildDiscarder logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '', daysToKeepStr: '', numToKeepStr: '1')
    }

    agent { label 'Docker-Maven' }

    parameters {
        string(name: 'NEW_VERSION', defaultValue: '', description: 'Bitte die nächste Version für das Projekt eingeben, format [0-9]+.[0-9]+.[0-9]+ \nHinweis: Version 0.0.[0-9] ist keine gültige Version!')
    }

    environment {
        TAGNAME = "v${RELEASE_VERSION}"
    }

    stages {
        stage('Initialise') {
            steps {
                useJdk('OPENJDK17')
                gitSetIdentity()
                checkVersion(NEW_VERSION)
            }
        }

        stage('Checkout') {
            steps {
                git branch: BRANCH,
                        credentialsId: CREDENTIAL_ID_GEMATIK_GIT,
                        url: REPO_URL
            }
        }

        stage('set Version') {
            steps {
                mavenSetVersion(RELEASE_VERSION, POM_PATH)
            }
        }
        stage('deploy') {
            steps {
                mavenDeploy()
            }
        }

        stage('Create Release-Tag') {
            steps {
                gitCreateAndPushTag(JIRA_PROJECT_ID, TAGNAME, BRANCH)
            }
        }

        stage('Create GitLab Release') {
            steps {
                gitLabCreateMavenRelease(JIRA_PROJECT_ID, GITLAB_PROJECT_ID, TITLE_TEXT, RELEASE_VERSION, TAGNAME, POM_PATH)
            }
        }

        stage('Release Jira-Version') {
            steps {
                jiraReleaseVersion(JIRA_PROJECT_ID, RELEASE_VERSION)
            }
        }
        stage('Create New Jira-Version') {
            steps {
                jiraCreateNewVersion(JIRA_PROJECT_ID, NEW_VERSION)
            }
        }
        stage('UpdateProject with new Version') {
            steps {
                mavenSetVersion("${NEW_VERSION}-SNAPSHOT")
                gitPushVersionUpdate(JIRA_PROJECT_ID, "${NEW_VERSION}-SNAPSHOT", BRANCH)
            }
        }
    }
    post {
        always {
            sendEMailNotification(getErpE2ETestsuiteEMailList())
        }
    }
}
