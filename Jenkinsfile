pipeline {
    agent any
    stages {
        stage('Example Build') {
            steps {
                sh "mvn --show-version --batch-mode -Dmaven.test.failure.ignore=true -Dspotbugs.failOnError=false install"
            }
        }
    }
}