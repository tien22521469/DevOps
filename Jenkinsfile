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
        DOCKER_REGISTRY = 'your-docker-registry'
        AWS_REGION = 'us-east-1'
        EKS_CLUSTER_NAME = 'emartapp-cluster'
        // JENKINS_API_TOKEN = credentials("JENKINS_API_TOKEN")
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

        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        
        stage('Build Backend') {
            steps {
                dir('emartapp/javaapi') {
                    sh './mvnw clean package -DskipTests'
                }
            }
        }
        
        stage('Build Frontend') {
            steps {
                dir('emartapp/frontend') {
                    sh 'npm install'
                    sh 'npm run build'
                }
            }
        }
        
        stage('Security Scan') {
            steps {
                sh 'trivy fs .'
            }
        }
        
        stage('Build Docker Images') {
            steps {
                script {
                    // Build Backend Image
                    docker.build("${DOCKER_REGISTRY}/emartapp-backend:${BUILD_NUMBER}", "--file Dockerfile ./emartapp/javaapi")
                    
                    // Build Frontend Image
                    docker.build("${DOCKER_REGISTRY}/emartapp-frontend:${BUILD_NUMBER}", "--file emartapp/frontend/Dockerfile ./emartapp/frontend")
                }
            }
        }
        
        stage('Scan Docker Images') {
            steps {
                script {
                    sh "trivy image ${DOCKER_REGISTRY}/emartapp-backend:${BUILD_NUMBER}"
                    sh "trivy image ${DOCKER_REGISTRY}/emartapp-frontend:${BUILD_NUMBER}"
                }
            }
        }
        
        stage('Push Docker Images') {
            steps {
                script {
                    docker.withRegistry("https://${DOCKER_REGISTRY}", 'docker-credentials') {
                        docker.image("${DOCKER_REGISTRY}/emartapp-backend:${BUILD_NUMBER}").push()
                        docker.image("${DOCKER_REGISTRY}/emartapp-frontend:${BUILD_NUMBER}").push()
                    }
                }
            }
        }
        
        stage('Deploy to EKS') {
            steps {
                script {
                    // Update kustomization.yaml with new image tags
                    sh """
                        cd k8s
                        kustomize edit set image backend=${DOCKER_REGISTRY}/emartapp-backend:${BUILD_NUMBER}
                        kustomize edit set image frontend=${DOCKER_REGISTRY}/emartapp-frontend:${BUILD_NUMBER}
                    """
                    
                    // Deploy to EKS
                    withAWS(credentials: 'aws-credentials', region: env.AWS_REGION) {
                        sh """
                            aws eks update-kubeconfig --name ${EKS_CLUSTER_NAME} --region ${AWS_REGION}
                            kubectl apply -k k8s/
                        """
                    }
                }
            }
        }
        
        stage('Verify Deployment') {
            steps {
                script {
                    withAWS(credentials: 'aws-credentials', region: env.AWS_REGION) {
                        sh """
                            aws eks update-kubeconfig --name ${EKS_CLUSTER_NAME} --region ${AWS_REGION}
                            kubectl rollout status deployment/backend -n emartapp
                            kubectl rollout status deployment/frontend -n emartapp
                        """
                    }
                }
            }
        }
    }

    post {
        always {
            cleanWs()
        }
        success {
            emailext (
                subject: "Pipeline Successful: ${currentBuild.fullDisplayName}",
                body: "Your pipeline has completed successfully. Check console output at ${env.BUILD_URL}",
                to: '${DEFAULT_RECIPIENTS}'
            )
        }
        failure {
            emailext (
                subject: "Pipeline Failed: ${currentBuild.fullDisplayName}",
                body: "Your pipeline has failed. Check console output at ${env.BUILD_URL}",
                to: '${DEFAULT_RECIPIENTS}'
            )
        }
    }
}
