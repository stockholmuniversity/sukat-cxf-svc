#!/usr/bin/env groovy

String projectName = env.JOB_NAME

suSetProperties(["github": "true"])

node('agent')
{
    stage("Cleanup workspace")
    {
        cleanWs()
    }

    stage("Prepare docker environment")
    {
        suDockerBuildAndPull(projectName)
    }

    docker.image(projectName).inside('-v /local/jenkins/conf:/local/jenkins/conf -v /local/jenkins/libexec:/local/jenkins/libexec -v /local/jenkins/conf/settings.xml:/root/.m2/settings.xml:ro')
    {
        stage('Checkout Code')
        {
            suCheckoutCode([projectName: projectName])
        }

        suGitHubBuildStatus
        {

            stage('Unit tests')
            {
                sh 'mvn test'
            }

            stage('Build and deploy')
            {
                sh 'mvn deploy -DskipTests'
            }
        }
    }
}
