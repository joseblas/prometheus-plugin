pipeline {
    agent any
    stages {
        stage('Example Build') {
            steps {
                sh "mvn --show-version -q --batch-mode -Dmaven.test.failure.ignore=true -Dspotbugs.failOnError=false clean install site"
            }
        }
    }
}