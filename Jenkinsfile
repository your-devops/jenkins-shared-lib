library ('jenkins-shared-lib') _

pipeline {
agent any
stages {
    stage('Example Test') {
        steps {
            helloworld.helloworld()
        }
       }
     }
post {
    always {
        script {
            //telegram.sendTelegram("Jenkins job ''${JOB_NAME}'' has ended with following result: ${currentBuild.currentResult}. \nYou can check the job status here: ${JOB_URL}")
            }
		}
	}
}