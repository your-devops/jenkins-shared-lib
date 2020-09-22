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
            sh "git status"
            }
          }
        }
      }
    }
    post {
      always {
        node("master") {
          script {
            SLACK_REPORT_MSG = "*${currentBuild.currentResult}:* Job <${env.BUILD_URL}display/redirect|${env.JOB_NAME} build #${BUILD_NUMBER}>:\n${currentBuild.getBuildCauses()[0].shortDescription}\nFailed on stage: ${LAST_EXECUTED_STAGE}\nTime total: " + sh(script: "echo '${currentBuild.durationString}' | sed 's/and counting//'", returnStdout: true).trim()
            echo SLACK_REPORT_MSG
          }
        }
      }
    }
  }
}