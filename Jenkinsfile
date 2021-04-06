pipeline {
    agent any
    stages {
        stage('Example Build') {
            steps {
                echo "first stage"
                sh "mvn --show-version -q --batch-mode -Dmaven.test.failure.ignore=true -Dspotbugs.failOnError=false clean surefire-report:report install site"
            }
        }
        stage('what') {
                    steps {
                        echo "second stage"
                    }
                }
    }
    post {
            always {
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                archiveArtifacts artifacts: 'target/*.hpi', fingerprint: true
                junit 'target/surefire-reports/*.xml'
            }
        }
}