pipeline {
  agent any
  
   tools {
    jdk 'jdk_19'
  }
  
  stages {
    stage('Build') {
      steps {
        sh './gradlew -x check clean build'
        archiveArtifacts(artifacts: 'build/libs/*.jar', allowEmptyArchive: true)
      }
    }

  }
  post {
    always {
      archiveArtifacts(artifacts: 'build/libs/*.jar', fingerprint: true)
    }

  }
}