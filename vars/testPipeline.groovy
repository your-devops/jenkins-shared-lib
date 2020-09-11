def call(Map pipelineParams) {

  pipeline {
    agent none
    stages {
      stage('Prepare Env Vars') {
        steps {
          script {
            // Set default requiredApplicationCoverage for MUnit tests. May be overriden from Job Properties.
            if (env.requiredApplicationCoverage == null) {
              env.requiredApplicationCoverage = "75"
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
            // Set tests to fail build if Application Coverage is below required value
            helper.pomSetMunitConfig("failBuild","true")
            // Set requiredApplicationCoverage
            helper.pomSetMunitConfig("requiredApplicationCoverage",requiredApplicationCoverage)
            // test
            sh "cat pom.xml"
            // Run Unit Tests
            configFileProvider([configFile(fileId: 'maven_settings', variable: 'mavenSettingsFile')]) {
              withCredentials([usernamePassword(credentialsId: 'dev-encryptor-pwd', passwordVariable: 'encryptorPasswd', usernameVariable: 'anypointUser')]) {
                sh "mvn -s '$mavenSettingsFile' clean test -Denv=${environment} -DmuleKey=${encryptorPasswd}"
              }
            }
          }
        }
      }
    }
  }
}