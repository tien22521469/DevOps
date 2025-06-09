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
        DOCKER_REGISTRY = "nguyentienuit"
        AWS_REGION = 'us-east-1'
        EKS_CLUSTER_NAME = 'emart-cluster'
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

                    //Snyk Security Scan
                    withCredentials([string(credentialsId: 'SNYK_TOKEN', variable: 'SNYK_TOKEN')]) {
                        sh '''
                             npm install -g snyk
                            snyk auth ${SNYK_TOKEN}
                            snyk test --all-projects || true
                            snyk monitor --all-projects || true
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
        stage("Build & Push Docker Images") {
            steps {
                script {
                    withCredentials([usernamePassword(credentialsId: 'dockerhub', passwordVariable: 'DOCKER_PASSWORD', usernameVariable: 'DOCKER_USERNAME')]) {
                        sh """
                            echo "${DOCKER_PASSWORD}" | docker login -u "${DOCKER_USERNAME}" --password-stdin
                            
                            # Build and push javaapi (backend)
                            docker build -t ${DOCKER_REGISTRY}/emartapp-javaapi:${BUILD_NUMBER} --file emartapp/javaapi/Dockerfile ./emartapp/javaapi
                            docker push ${DOCKER_REGISTRY}/emartapp-javaapi:${BUILD_NUMBER}

                            # Build and push nodeapi (backend)
                            docker build -t ${DOCKER_REGISTRY}/emartapp-nodeapi:${BUILD_NUMBER} --file emartapp/nodeapi/Dockerfile ./emartapp/nodeapi
                            docker push ${DOCKER_REGISTRY}/emartapp-nodeapi:${BUILD_NUMBER}

                            # Build and push frontend
                            docker build -t ${DOCKER_REGISTRY}/emartapp-frontend:${BUILD_NUMBER} --file emartapp/client/Dockerfile ./emartapp/client
                            docker push ${DOCKER_REGISTRY}/emartapp-frontend:${BUILD_NUMBER}

                            # Scan images
                            trivy image ${DOCKER_REGISTRY}/emartapp-javaapi:${BUILD_NUMBER}
                            trivy image ${DOCKER_REGISTRY}/emartapp-nodeapi:${BUILD_NUMBER}
                            trivy image ${DOCKER_REGISTRY}/emartapp-frontend:${BUILD_NUMBER}
                        """
                    }
                }
            }
        }
    
        stage("Trigger CD Pipeline") {
            steps {
                script {
                    // Trigger CD pipeline using Jenkins CLI
                    withCredentials([usernamePassword(credentialsId: 'JENKINS_API_TOKEN', passwordVariable: 'JENKINS_TOKEN', usernameVariable: 'JENKINS_USERNAME')]) {
                        sh """
                            # Get Jenkins CLI JAR
                            curl -o jenkins-cli.jar http://34.228.8.171:8080/jnlpJars/jenkins-cli.jar
                            
                            # Trigger CD pipeline
                            java -jar jenkins-cli.jar -s http://34.228.8.171:8080 \
                                -auth ${JENKINS_USERNAME}:${JENKINS_TOKEN} \
                                build devops-gitops \
                                -p DOCKER_REGISTRY=${DOCKER_REGISTRY} \
                                -p BUILD_NUMBER=${BUILD_NUMBER} \
                                -p AWS_REGION=${AWS_REGION} \
                                -p EKS_CLUSTER_NAME=${EKS_CLUSTER_NAME}
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
