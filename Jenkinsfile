pipeline {
    agent any
    tools {
        jdk 'jdk17'
        nodejs 'node16'
        maven 'maven3'
    }
    environment {
        SCANNER_HOME = tool 'sonar-scanner'
        APP_NAME = "devops"
        RELEASE = "1.0.0"
        DOCKER_USER = "nguyentienuit"
        DOCKER_PASS = 'tien160904'
        IMAGE_NAME = "${DOCKER_USER}" + "/" + "${APP_NAME}"
        IMAGE_TAG = "${RELEASE}-${BUILD_NUMBER}"
    }
    stages {
        stage("Cleanup Workspace") {
            steps {
                cleanWs()
            }
        }

        stage("Checkout from Git") {
            steps {
                git branch: 'main', credentialsId: 'github', url: 'https://github.com/tien22521469/DevOps.git'
            }
        }

        stage("Build Application") {
            steps {
                sh 'mvn -f emartapp/javaapi/pom.xml clean package'
            }
        }

        stage("Test Application") {
            steps {
                sh "mvn -f emartapp/javaapi/pom.xml test"
            }
        }

        stage("Sonarqube Analysis") {
            steps {
                withSonarQubeEnv('SonarQube-Server') {
                    sh '''
                        ${SCANNER_HOME}/bin/sonar-scanner \
                        -Dsonar.projectName=devops \
                        -Dsonar.projectKey=devops \
                        -Dsonar.sources=emartapp/javaapi/src/main/java \
                        -Dsonar.java.binaries=emartapp/javaapi/target/classes \
                        -Dsonar.java.test.binaries=emartapp/javaapi/target/test-classes \
                        -Dsonar.java.libraries=${WORKSPACE}/emartapp/javaapi/target/book-work-0.0.1-SNAPSHOT.jar \
                        -Dsonar.java.source=17 \
                        -Dsonar.sourceEncoding=UTF-8 \
                        -Dsonar.java.test.libraries=${WORKSPACE}/emartapp/javaapi/target/book-work-0.0.1-SNAPSHOT.jar \
                        -Dsonar.exclusions=**/*.xml,**/*.properties \
                        -Dsonar.test.inclusions=**/*Test.java,**/*Tests.java \
                        -Dsonar.coverage.exclusions=**/*Application.java,**/model/**,**/entity/**
                    '''
                }
            }
        }

        stage("Quality Gate") {
            steps {
                script {
                    waitForQualityGate abortPipeline: false, credentialsId: 'SonarQube-Token'
                }
            }
        }

        stage('Install Dependencies') {
            steps {
                sh "npm install"
            }
        }

        stage('TRIVY FS SCAN') {
            steps {
                sh "trivy fs . > trivyfs.txt"
            }
        }
       stage("Build & Push Docker Image") {
             steps {
               script {
                   withCredentials([usernamePassword(credentialsId: 'dockerhub', passwordVariable: 'DOCKER_PASSWORD', usernameVariable: 'DOCKER_USERNAME')]) {
                       sh """
                           echo "${DOCKER_PASSWORD}" | docker login -u "${DOCKER_USERNAME}" --password-stdin
                           docker build -t ${IMAGE_NAME}:${IMAGE_TAG} .
                           docker tag ${IMAGE_NAME}:${IMAGE_TAG} ${IMAGE_NAME}:latest
                           docker push ${IMAGE_NAME}:${IMAGE_TAG}
                           docker push ${IMAGE_NAME}:latest
                       """
                   }
               }
             }
         }
        stage("Trivy Image Scan") {
             steps {
                 script {
	              sh ('docker run -v /var/run/docker.sock:/var/run/docker.sock aquasec/trivy image nguyentienuit/devops:latest --no-progress --scanners vuln  --exit-code 0 --severity HIGH,CRITICAL --format table > trivyimage.txt')
                 }
             }
         }
	stage ('Cleanup Artifacts') {
             steps {
                 script {
                      sh "docker rmi ${IMAGE_NAME}:${IMAGE_TAG}"
                      sh "docker rmi ${IMAGE_NAME}:latest"
                 }
             }
         }
	
     	post {
       	 always {
           	emailext attachLog: true,
              	 subject: "'${currentBuild.result}'",
        	 body: "Project: ${env.JOB_NAME}<br/>" +
              	     "Build Number: ${env.BUILD_NUMBER}<br/>" +
                     "URL: ${env.BUILD_URL}<br/>",
               	 to: '22521469@gm.uit.edu.vn',                              
              	 attachmentsPattern: 'trivyfs.txt,trivyimage.txt'
        }
     }
    }
}
