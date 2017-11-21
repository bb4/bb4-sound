#!groovy

pipeline {
    agent any
    stages {
        stage('Checkout source') {
            steps {
                git url: 'https://github.com/bb4/bb4-sound.git', branch: 'master'
            }
        }

        stage('build/test') {
            steps {
                gradleCmd("clean build --refresh-dependencies")
            }
        }

        stage('documentation') {
            steps {
                gradleCmd("javadoc")
            }
        }

        stage('publish') {
            steps {
                gradleCmd("publishArtifacts --info --refresh-dependencies")
            }
        }
    }
    post {
        always {
            junit 'build/test-results/test/*.xml'
            step([$class: 'JavadocArchiver', javadocDir: 'build/docs/javadoc', keepAll: true])
        }
        success {
            mail to: 'barrybecker4@gmail.com',
                 subject: "Successful Pipeline: ${currentBuild.fullDisplayName}",
                 body: "This build succeeded: ${env.BUILD_URL}"
        }
        failure {
            mail to: 'barrybecker4@gmail.com',
                 subject: "Failed Pipeline: ${currentBuild.fullDisplayName}",
                 body: "Something is wrong with ${env.BUILD_URL}"
        }
        unstable {
            echo 'This build is unstable.'
        }
    }
}

def gradleCmd(cmd) {
    if (isUnix()) {
        sh "./gradlew ${cmd}"
    } else {
        bat "./gradlew.bat ${cmd}"
    }
}
