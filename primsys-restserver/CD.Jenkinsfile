@Library('gematik-jenkins-shared-library') _

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
                dockerRun(IMAGE_NAME, params.Version, SERVICE_NAME, "-ti -p6095:9095 --restart always -eTI_ENV=${params.Environment} -eVERSION=${params.Version} -eHTTP_PROXY=${http_proxy} -eHTTPS_PROXY=${http_proxy} -eNO_PROXY=${no_proxy}")
            }
        }

        stage('Stop Service') {
            when { expression { params.Action == 'stop' } }
            steps {
                dockerStop(SERVICE_NAME)
            }
        }

        stage('Stop/Delete Service') {
            when { expression { params.Action == 'delete' } }
            steps {
                dockerStop(SERVICE_NAME, true, true)
            }
        }
    }
}
