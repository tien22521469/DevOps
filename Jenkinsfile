pipeline {
    agent any
    tools {
        jdk 'jdk17'
        nodejs 'node16'
        maven 'Maven3'
    }
    environment {
        SCANNER_HOME = tool 'sonar-scanner'
        APP_NAME = "devops"
        RELEASE = "1.0.0"
        IMAGE_NAME = "${DOCKER_USER}" + "/" + "${APP_NAME}"
        IMAGE_TAG = "${RELEASE}-${BUILD_NUMBER}"
        DOCKER_REGISTRY = 'docker.io'
        AWS_REGION = 'us-east-1'
        EKS_CLUSTER_NAME = 'emartapp-cluster'
    }
    stages {
        stage("Cleanup & Checkout") {
            steps {
                cleanWs()
                git branch: 'main', credentialsId: 'github', url: 'https://github.com/tien22521469/DevOps.git'
            }
        }

     stage("Build & Test") {
            steps {
                dir('emartapp/javaapi') {
                    sh 'chmod +x ./mvnw'
                    sh './mvnw clean package -DskipTests'
                }
                
            }
        }

        stage("Security & Quality") {
            steps {
                script {
                    // SonarQube Analysis
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
                    waitForQualityGate abortPipeline: false, credentialsId: 'SonarQube-Token'

                     // Snyk Security Scan
                    withCredentials([string(credentialsId: 'SNYK_TOKEN', variable: 'SNYK_TOKEN')]) {
                        sh '''
                            snyk auth ${SNYK_TOKEN}
                            snyk test --all-projects
                        '''
                    }
                }
            }
        }
        stage('TRIVY FS SCAN') {
            steps {
                sh "trivy fs . > trivyfs.txt"
            }
        }
    }
}
