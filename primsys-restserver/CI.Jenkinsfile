@Library('gematik-jenkins-shared-library') _

def webhookUrl = "https://gematikde.webhook.office.com/webhookb2/8534640c-0537-4106-ae8b-5ae473538b59@30092c62-4dbf-43bf-a33f-10d21b5b660a/IncomingWebhook/d1457d5a1a724a65b556d1c517617200/d61648f4-cbb3-4477-8962-e82e686443bc"

def BRANCH = 'Development_1.x'
def DOCKER_TAG_DEFAULT = "latest"
def IMAGE_NAME = "erezept/primsys-rest"
def IMAGE_NAME_NGINX = "erezept/primsys-nginx"
def BUILD_DATE = new Date().format("dd-MM-yyyy'T'HH:mm")

pipeline {
    options {
        disableConcurrentBuilds()
        buildDiscarder logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '', daysToKeepStr: '', numToKeepStr: '5')
    }
    agent { label 'k8-maven-small' }

    tools {
        maven 'Default'
    }

    parameters {
        //!!! RELEASE_VERION muss über https://wiki.gematik.de/pages/viewpage.action?pageId=139822144#JenkinsReleasePipeline(Executables)-Nutzereingaben angelegt werden!!!
        string(name: 'NEW_VERSION', defaultValue: '', description: 'Bitte die nächste Version für das Projekt eingeben, format [0-9]+.[0-9]+.[0-9]+ \nHinweis: Version 0.0.[0-9] ist keine gültige Version!')
        booleanParam(name: 'Vulnerabilities', defaultValue: true, description: 'Vulnerabilities-Scan')
    }

    stages {
        stage('Initialise') {
            steps {
                useJdk('OPENJDK17')
                checkVersion(params.NEW_VERSION)
                gitSetIdentity()
            }
        }

//        stage('Checkout') {
//            steps {
//                git branch: BRANCH,
//                        //nur für die Testumgebung notwendig!
//                        credentialsId: CREDENTIAL_ID_GEMATIK_GIT,
//                        url: REPO_URL
//            }
//        }

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

        stage('Build PrimsysRestserver') {
            steps {
                script {
                    def buildDir = "primsys-restserver"
                    def dockerFile = "Dockerfile"
                    def dockerRegistry = dockerGetGematikRegistry()
                    def buildArgs = "--build-arg BUILD_DATE=${BUILD_DATE}"
                    dockerBuild(IMAGE_NAME, DOCKER_TAG_DEFAULT, DOCKER_TAG_DEFAULT, buildArgs, dockerFile, dockerRegistry, buildDir)
                }
            }
        }
//         stage('Vulnerabilities-Scan Dockerimage') {
//             when { expression { return params.Vulnerabilities } }
//             steps {
//                 trivyVulnerabilitiesScanAllAsHtml(IMAGE_NAME, version)
//             }
//         }

        stage('Build PrimsysNginx') {
            steps {
                script {
                    def buildDir = "primsys-restserver/nginx"
                    def dockerFile = "Dockerfile"
                    def dockerRegistry = dockerGetGematikRegistry()
                    def buildArgs = ""
                    dockerBuild(IMAGE_NAME_NGINX, DOCKER_TAG_DEFAULT, DOCKER_TAG_DEFAULT, buildArgs, dockerFile, dockerRegistry, buildDir)
                }
            }
        }
//         stage('Vulnerabilities-Scan Dockerimage') {
//             when { expression { return params.Vulnerabilities } }
//             steps {
//                 trivyVulnerabilitiesScanAllAsHtml(IMAGE_NAME_NGINX, version)
//             }
//         }



        stage('Push PrimsysRestserver') {
            when { branch BRANCH }
            steps {
                script {
                    dockerPushImage(IMAGE_NAME, DOCKER_TAG_DEFAULT)
                    dockerReTagImage(IMAGE_NAME, params.NEW_VERSION, DOCKER_TAG_DEFAULT)
                    dockerPushImage(IMAGE_NAME, params.NEW_VERSION)
                    dockerRemoveLocalImage(IMAGE_NAME, params.NEW_VERSION)
                }
            }
        }

        stage('Push PrimsysNginx') {
            when { branch BRANCH }
            steps {
                script {
                    dockerPushImage(IMAGE_NAME_NGINX, DOCKER_TAG_DEFAULT)
                    dockerReTagImage(IMAGE_NAME_NGINX, params.NEW_VERSION, DOCKER_TAG_DEFAULT)
                    dockerPushImage(IMAGE_NAME_NGINX, params.NEW_VERSION)
                    dockerRemoveLocalImage(IMAGE_NAME_NGINX, params.NEW_VERSION)
                }
            }
        }
    }
    post {
        always {
            sendTeamsNotification(webhookUrl, getJobColor(currentBuild.currentResult), "Status: ${currentBuild.currentResult}", "Release new PrimSys Version ${params.NEW_VERSION}")
            dockerRemoveLocalImage(IMAGE_NAME, DOCKER_TAG_DEFAULT)
            dockerRemoveLocalImage(IMAGE_NAME_NGINX, DOCKER_TAG_DEFAULT)
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
