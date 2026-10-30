pipeline {
    agent any

    environment {
        // Set this to your own Docker Hub namespace/repository before the first run.
        DOCKERHUB_REPO  = 'lakshyakapoor/calculator'
        // ID of the Jenkins "Username with password" credential holding the Docker Hub login.
        DOCKERHUB_CREDS = 'dockerhub-credentials'

        // The pom targets Java 21, while the host default JDK is newer.
        JAVA_HOME    = '/usr/lib/jvm/java-21-openjdk-amd64'

        IMAGE_TAG    = "${env.BUILD_NUMBER}"
        PROXY_PORT   = '8081'
        NOTIFY_EMAIL = 'lakshya.kapoor@iiitb.ac.in'
    }

    options {
        timestamps()
        timeout(time: 30, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '15'))
        disableConcurrentBuilds()
    }

    triggers {
        // Fallback for when the GitHub webhook cannot reach this Jenkins.
        pollSCM('H/5 * * * *')
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                sh 'git log -1 --oneline'
            }
        }

        stage('Build') {
            steps {
                sh './mvnw -B clean package -DskipTests'
            }
        }

        stage('Test') {
            steps {
                sh './mvnw -B test'
            }
            post {
                always {
                    // Fails the build if any test failed, and publishes the trend graph.
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('Docker Build') {
            steps {
                sh '''
                    docker build \
                        -t "$DOCKERHUB_REPO:$IMAGE_TAG" \
                        -t "$DOCKERHUB_REPO:latest" \
                        .
                '''
            }
        }

        stage('Push') {
            steps {
                withCredentials([usernamePassword(
                        credentialsId: "${DOCKERHUB_CREDS}",
                        usernameVariable: 'DOCKER_USER',
                        passwordVariable: 'DOCKER_PASS')]) {
                    sh '''
                        echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin
                        docker push "$DOCKERHUB_REPO:$IMAGE_TAG"
                        docker push "$DOCKERHUB_REPO:latest"
                        docker logout
                    '''
                }
            }
        }

        stage('Deploy') {
            steps {
                sh '''
                    # "up -d" is idempotent: unchanged services are left running.
                    IMAGE_NAME="$DOCKERHUB_REPO" \
                    IMAGE_TAG="$IMAGE_TAG" \
                    PROXY_PORT="$PROXY_PORT" \
                        docker compose up -d --remove-orphans

                    # Verify the deployment through the nginx proxy.
                    for i in $(seq 1 30); do
                        if curl -fs "http://localhost:$PROXY_PORT/actuator/health" | grep -q '"status":"UP"'; then
                            echo "Deployment is healthy"
                            curl -s "http://localhost:$PROXY_PORT/api/v1/add?a=2&b=3"
                            exit 0
                        fi
                        sleep 2
                    done

                    echo "Deployment did not become healthy in time"
                    docker compose ps
                    docker compose logs --tail=50
                    exit 1
                '''
            }
        }
    }

    post {
        always {
            sh 'docker image prune -f --filter "until=168h" || true'
        }
        success {
            emailext(
                to: "${NOTIFY_EMAIL}",
                subject: "SUCCESS: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                body: """Build #${env.BUILD_NUMBER} succeeded.

Image:  ${DOCKERHUB_REPO}:${env.BUILD_NUMBER}
Stack:  http://localhost:${PROXY_PORT}
Logs:   ${env.BUILD_URL}console
"""
            )
        }
        failure {
            emailext(
                to: "${NOTIFY_EMAIL}",
                subject: "FAILURE: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                body: """Build #${env.BUILD_NUMBER} failed.

Logs: ${env.BUILD_URL}console
""",
                attachLog: true
            )
        }
    }
}
