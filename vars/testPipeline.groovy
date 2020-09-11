def call(Map pipelineParams) {

  pipeline {
    agent none
    parameters {
      string(name: 'appStatusCheckAttempts', defaultValue: "4", description: 'Number of attempts for checking application status after deployment.')
      string(name: 'appStatusCheckInterval', defaultValue: "60", description: 'Interval, in seconds, between attempts to check application status.')
    }
    stages {
      stage('Prepare Env Vars') {
        steps {
          script {
            // Set default requiredApplicationCoverage for MUnit tests. May be overriden from Job Properties.
            if (env.requiredApplicationCoverage == null) {
              env.requiredApplicationCoverage = "75"
            }
            appStatus = "UNKNOWN"
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
            // Run Unit Tests
/*             configFileProvider([configFile(fileId: 'maven_settings', variable: 'mavenSettingsFile')]) {
              withCredentials([usernamePassword(credentialsId: 'anypoint-mule-key', passwordVariable: 'encryptorPasswd', usernameVariable: 'anypointUser')]) {
                sh "mvn -s '$mavenSettingsFile' clean test -Denv=${environment} -DmuleKey=${encryptorPasswd}"
              }
            } */
          }
        }
      }
    }
  }
}