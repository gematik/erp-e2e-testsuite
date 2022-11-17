@Library('gematik-jenkins-shared-library') _

def CREDENTIAL_ID_GEMATIK_GIT = 'tst_tt_build_u_p'
def REPO_URL = 'https://build.top.local/source/git/erezept/fachdienst/erp-e2e'
// def BRANCH = 'kal/primsys-rest'
def JIRA_PROJECT_ID = 'TMD'
def GITLAB_PROJECT_ID = '843'
// def TITLE_TEXT = 'Release'
def version = "latest"
def IMAGE_NAME = "erezept/primsys-rest"
def SERVICE_NAME = "primsys_rest"

properties([
    buildDiscarder(
        logRotator(
            artifactDaysToKeepStr:      '',
            artifactNumToKeepStr:       '',
            daysToKeepStr:              '',
            numToKeepStr:               '5'
        )
    ),
    parameters([
        string(name: 'Version', defaultValue: 'latest', description: 'Version for Image'),
        choice(name: "Action", choices: ['start', 'stop', 'delete'], description: 'Action die für den Container ausgeführt wird'),
        choice(name: 'DockerAgent', choices: ['LTU_DEV', 'LTU_RU', 'LTU_PROD', 'LTU_DEV_QS'], description: 'Docker Runtime Stage'),
        choice(name: 'Environment', choices: ['TU', 'RU'], description: 'Staging Environment des E-Rezept FD'),
    ])
])

pipeline {
    agent { label DockerAgent }

    environment {
        http_proxy = "http://10.11.98.2:3128"
        no_proxy = "127.0.0.1,localhost,*.splitdns.ti-dienste.de"
    }

    stages {
        stage('Start Service') {
            when { expression { params.Action == 'start' } }
            steps {
                echo "Start Service"
                dockerRun(IMAGE_NAME, params.Version, SERVICE_NAME, "-ti -p6095:9095 --restart always -eTI_ENV=${params.Environment} -eHTTP_PROXY=${http_proxy} -eHTTPS_PROXY=${http_proxy} -eNO_PROXY=${no_proxy}")
            }
        }

        stage('Stop Service') {
            when { expression { params.Action == 'stop' } }
            steps {
                echo "Stop Service"
                dockerStop(SERVICE_NAME)
            }
        }

        stage('Delete Service') {
            when { expression { params.Action == 'delete' } }
            steps {
                echo "Delete Service"
                dockerStop(SERVICE_NAME, true)
            }
        }
    }
}
