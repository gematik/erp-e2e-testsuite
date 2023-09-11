@Library('gematik-jenkins-shared-library') _

def webhookUrl = "https://gematikde.webhook.office.com/webhookb2/8534640c-0537-4106-ae8b-5ae473538b59@30092c62-4dbf-43bf-a33f-10d21b5b660a/IncomingWebhook/d1457d5a1a724a65b556d1c517617200/d61648f4-cbb3-4477-8962-e82e686443bc"
def SERVICE_NAME = "primsysrest"

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
                choice(name: "Action", choices: ['restart', 'start', 'stop'], description: 'Action die für den Container ausgeführt wird'),
                choice(name: 'DockerAgent', choices: ['LTU_DEV', 'LTU_RU', 'LTU_PROD', 'LTU_DEV_QS'], description: 'Docker Runtime Stage'),
        ])
])

pipeline {
    agent { label DockerAgent }

    environment {
        http_proxy = "http://10.11.98.2:3128"
        no_proxy = "127.0.0.1,localhost,*.splitdns.ti-dienste.de"
    }

    stages {
        stage('Stop Service') {
            when { expression { params.Action == 'stop' || params.Action == 'restart'} }
            steps {
                dockerComposeStopServices("primsys-restserver/docker-compose.yaml", SERVICE_NAME, "", true, true, true)
            }
            post {
                always {
                    sendTeamsNotification(webhookUrl, getJobColor(currentBuild.currentResult), "Status: ${currentBuild.currentResult}", "Stoppe $SERVICE_NAME")
                }
            }
        }

        stage('Start Service') {
            when { expression { params.Action == 'start' || params.Action == 'restart'} }
            steps {
                dockerLoginGematikRegistry()
                dockerComposeStartServices("primsys-restserver/docker-compose.yaml", SERVICE_NAME)
            }
            post {
                always {
                    sendTeamsNotification(webhookUrl, getJobColor(currentBuild.currentResult), "Status: ${currentBuild.currentResult}", "Starte $SERVICE_NAME")
                }
            }
        }
    }
}

static def getJobColor(String result) {
    if (result.equalsIgnoreCase("success")) {
        return "#00ff00"
    } else if (result.equalsIgnoreCase("failure")) {
        return "#ff0000"
    } else if(result.equalsIgnoreCase("unstable")) {
        return "#ffff00"
    } else {
        return ""
    }
}
