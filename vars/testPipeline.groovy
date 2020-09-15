def call(Map pipelineParams) {

  pipeline {
    agent {
      label 'master'
    }
    stages {
      stage('Prepare Env Vars') {
        steps {
          script {
            LAST_EXECUTED_STAGE = STAGE_NAME
            // Set default requiredApplicationCoverage for MUnit tests. May be overriden from Job Properties.
            if (env.requiredApplicationCoverage == null) {
              env.requiredApplicationCoverage = "75"
            }
            docker.image('jakubsacha/docker-xmlstarlet:latest').inside("--entrypoint=''") {
              // Set tests to fail build if Application Coverage is below required value
              helper.pomSetMunitConfig('failBuild','true')
              // Set requiredApplicationCoverage
              helper.pomSetMunitConfig('requiredApplicationCoverage',requiredApplicationCoverage)
              // Output changed pom.xml
              echo "Building with following pom.xml:"
              sh "cat pom.xml"
            }
          }
        }
      }
      stage('Unit Tests') {
        steps {
          script {
            LAST_EXECUTED_STAGE = STAGE_NAME
            docker.image('maven:3.6-jdk-8').inside("-v $HOME/.m2:/root/.m2 -u root") {
              // Run Unit Tests
              configFileProvider([configFile(fileId: 'maven_settings', variable: 'mavenSettingsFile')]) {
                withCredentials([usernamePassword(credentialsId: 'dev-encryptor-pwd', passwordVariable: 'encryptorPasswd', usernameVariable: 'anypointUser')]) {
                  sh "mvn -s '$mavenSettingsFile' clean test -Denv=${environment} -Dapp.key=${encryptorPasswd}"
                }
              }
            }
 
          publishHTML (target: [
            allowMissing: true,
            alwaysLinkToLastBuild: false,
            keepAll: true,
            reportDir: '.',
            reportFiles: 'linter-output.txt',
            reportName: "Linter Output"
          ])
          }
        }
      }
    }
    post {
      always {
        script {
          echo "*${currentBuild.currentResult}:* Job <${env.JOB_URL}|${env.JOB_NAME}> <${env.BUILD_URL}console|build #${BUILD_NUMBER}>:\\n${currentBuild.getBuildCauses()[0].shortDescription}\\nTime total: ${currentBuild.durationString}"
        }
      }
    }
  }
}