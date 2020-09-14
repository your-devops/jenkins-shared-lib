def call(Map pipelineParams) {

  pipeline {
    agent any
    stages {
      stage('Prepare Env Vars') {
        steps {
          script {
            docker.image('jakubsacha/docker-xmlstarlet:latest').inside {
              // Set default requiredApplicationCoverage for MUnit tests. May be overriden from Job Properties.
              if (env.requiredApplicationCoverage == null) {
                env.requiredApplicationCoverage = "75"
              }
              // Set tests to fail build if Application Coverage is below required value
              helper.pomSetMunitConfig('failBuild','true')
              // Set requiredApplicationCoverage
              helper.pomSetMunitConfig('requiredApplicationCoverage',requiredApplicationCoverage)
              // test
              sh "cat pom.xml"
              stash(name: "modifiedPomFile", includes: "pom.xml")
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
            unstash("modifiedPomFile")
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
  }
}