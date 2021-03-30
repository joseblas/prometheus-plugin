pipeline {
    agent any
    stages {
        stage('Example Build') {
            steps {
                echo "starting the build"
                sh "mvn --show-version --batch-mode -Dmaven.test.failure.ignore=true -Dspotbugs.failOnError=false clean surefire-report:report install site"
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