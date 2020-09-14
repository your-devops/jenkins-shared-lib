def call(Map pipelineParams) {

  pipeline {
    agent {
      label 'master'
    }
    stages {
      stage('Prepare Env Vars') {
        steps {
          script {
            docker.image('jakubsacha/docker-xmlstarlet:latest').inside("--entrypoint=''") {
              // Set default requiredApplicationCoverage for MUnit tests. May be overriden from Job Properties.
              if (env.requiredApplicationCoverage == null) {
                env.requiredApplicationCoverage = "75"
              }
              // Set tests to fail build if Application Coverage is below required value
              helper.pomSetMunitConfig('failBuild','true')
              // Set requiredApplicationCoverage
              helper.pomSetMunitConfig('requiredApplicationCoverage',requiredApplicationCoverage)
              // test
              echo "Building with following pom.xml settings:"
              sh "cat pom.xml"
            }
          }
        }
      }
      stage('Unit Tests') {
        steps {
          script {
/*             docker.image('maven:3.6-jdk-8').inside("-v $HOME/.m2:/root/.m2 -u root") {
              // Run Unit Tests
              configFileProvider([configFile(fileId: 'maven_settings', variable: 'mavenSettingsFile')]) {
                withCredentials([usernamePassword(credentialsId: 'dev-encryptor-pwd', passwordVariable: 'encryptorPasswd', usernameVariable: 'anypointUser')]) {
                  sh "mvn -s '$mavenSettingsFile' clean test -Denv=${environment} -Dapp.key=${encryptorPasswd}"
                }
              }
            }
 */
          sh "echo 'test string to txt file publish to html\n2nd test string' > linter-output.txt"
          archiveArtifacts artifacts: 'linter-output.txt'
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
  }
}