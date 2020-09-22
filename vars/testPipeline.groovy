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
            env.BITBUCKET_SOURCE_BRANCH = sh(script: "echo $GIT_BRANCH | grep -oP '(?<=origin/).*'", returnStdout: true).trim()
            sh "git status"
          }
        }
      }
    }
    post {
      always {
        node("master") {
          jiraSendBuildInfo site: 'yourdevops.atlassian.net', branch: "${env.BITBUCKET_SOURCE_BRANCH}"
          jiraSendDeploymentInfo site: 'yourdevops.atlassian.net', environmentId: 'qa-1', environmentName: 'QA-1', environmentType: 'QA'
          script {
            if (env.JIRA_ISSUE_KEY != null) echo "JIRA_ISSUE_KEY: ${env.JIRA_ISSUE_KEY}"
            SLACK_REPORT_MSG = "*${currentBuild.currentResult}:* Job <${env.BUILD_URL}display/redirect|${env.JOB_NAME} build #${BUILD_NUMBER}>:\n${currentBuild.getBuildCauses()[0].shortDescription}\nFailed on stage: ${LAST_EXECUTED_STAGE}\nTime total: " + sh(script: "echo '${currentBuild.durationString}' | sed 's/and counting//'", returnStdout: true).trim()
            echo SLACK_REPORT_MSG
          }
        }
      }
    }
  }
}