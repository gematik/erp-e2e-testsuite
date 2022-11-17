@Library('gematik-jenkins-shared-library') _

def version = "latest"
def IMAGE_NAME = "erezept/pharmacy-service-provider"
def SERVICE_NAME = "pharmacy-service-provider"

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
    ])
])

pipeline {
    agent { label 'LTU_DEV' }

    stages {
        stage('Start Service') {
            when { expression { params.Action == 'start' } }
            steps {
                echo "Start Service"
                dockerRun(IMAGE_NAME, params.Version, SERVICE_NAME, "-ti -p6003:9095 --restart always")
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
