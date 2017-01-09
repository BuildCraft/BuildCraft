#!/bin/groovy

// necessary jenkins plugins
// - "Docker Pipeline" (http://wiki.jenkins-ci.org/display/JENKINS/Docker+Pipeline+Plugin)

///////////////////////////////////////////////////////////////////////////////

catchError() {

    properties([
        buildDiscarder(logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '14', daysToKeepStr: '14', numToKeepStr: '180')),
        disableConcurrentBuilds()
    ])

    milestone label: 'Start'

    node() {
        stage("Checkout") {
            checkout scm
            
            sh """set -x
                  git submodule foreach "git reset --hard || true" || true
                  git reset --hard || true
                  git submodule update --init || true
               """
        }

        milestone label: 'Checkout complete'

        withDockerContainer('openjdk:8-jdk') {
            stage("Build") {
                sh """set -x
                      ./gradlew build -x test
                   """

                archiveArtifacts artifacts: 'build/libs/*.jar', fingerprint: true, onlyIfSuccessful: true
            }
            
            milestone label: 'Build complete'
            
            stage("Test") {
                try {
                    sh """set -x
                          ./gradlew clean test
                       """

                    step([$class: 'JUnitResultArchiver', testResults: '**/build/test-results/TEST-*.xml'])
                } catch (e) {
                    currentBuild.result = "UNSTABLE"
                    echo "Exception caught while running test: ${e}"
                }
            }
            
            milestone label: 'Test complete'
        }
    }
} // catchError()
