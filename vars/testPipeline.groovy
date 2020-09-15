def call(Map pipelineParams) {

  pipeline {
    agent none
    stages {
      stage('Prepare Env Vars') {
        agent {
          label 'master'
        }
        steps {
          script {
            LAST_EXECUTED_STAGE = STAGE_NAME
            // Set default requiredApplicationCoverage for MUnit tests. May be overriden from Job Properties.
            if (env.requiredApplicationCoverage == null) {
              env.requiredApplicationCoverage = "75"
            }
            docker.image('yourdevops/docker-xmlstarlet:latest').inside("--entrypoint=''") {
              // Set tests to fail build if Application Coverage is below required value
              helper.pomSetMunitConfig('failBuild','true')
              // Set requiredApplicationCoverage
              helper.pomSetMunitConfig('requiredApplicationCoverage',requiredApplicationCoverage)
              // Output changed pom.xml
              echo "Building with following pom.xml:"
              sh "cat pom.xml"
              stash includes: 'pom.xml', name: 'parsedPom'
            }
          }
        }
      }
      stage('Unit Tests') {
        agent {
          docker {
            image 'maven:3.6-jdk-8'
            args '-v $HOME/.m2:/root/.m2 -u root'
          }
        }
        steps {
          script {
            LAST_EXECUTED_STAGE = STAGE_NAME
            unstash 'parsedPom'
            sh "cat pom.xml"
            // Run Unit Tests
            configFileProvider([configFile(fileId: 'maven_settings', variable: 'mavenSettingsFile')]) {
              withCredentials([usernamePassword(credentialsId: 'dev-encryptor-pwd', passwordVariable: 'encryptorPasswd', usernameVariable: 'anypointUser')]) {
                sh "mvn -s '$mavenSettingsFile' clean test -Denv=${environment} -Dapp.key=${encryptorPasswd}"
              }
            }
          }
        }
      }
    }
    post {
      always {
        node("master") {
          script {
            SLACK_REPORT_MSG = "*${currentBuild.currentResult}:* Job <${env.JOB_URL}|${env.JOB_NAME}> <${env.BUILD_URL}console|build #${BUILD_NUMBER}>:\n${currentBuild.getBuildCauses()[0].shortDescription}\nFailed on stage:${LAST_EXECUTED_STAGE}\nTime total: " + sh(script: "echo '${currentBuild.durationString}' | sed 's/and counting//'", returnStdout: true).trim()
            echo SLACK_REPORT_MSG
          }
        }
      }
    }
  }
}