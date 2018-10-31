#!/usr/bin/env groovy

String projectName = env.JOB_NAME
Boolean isReleaseBuild = false

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

        stage("Get information")
        {
            def revision = env.rev ?: sh(script: "git log -n 1  --pretty=format:'%H'", returnStdout: true).trim()
            tag = sh(script: "git tag --contains ${revision} | tail -1", returnStdout: true).trim()
            if(tag && env.branch == 'master')
            {
                isReleaseBuild = true
            }

        }

        suGitHubBuildStatus
        {

            stage('Junit Tests')
            {
                sh './mvnw test'
            }

            stage('SonarQube analysis')
            {
                withSonarQubeEnv('sonarqube')
                {
                    if(isReleaseBuild)
                    {
                        sh './mvnw org.sonarsource.scanner.maven:sonar-maven-plugin:3.2:sonar -Dsonar.projectName=sukat-cxf-svc -Dsonar.branch.name=master'
                    }
                    else
                    {
                        sh './mvnw org.sonarsource.scanner.maven:sonar-maven-plugin:3.2:sonar -Dsonar.projectName=sukat-cxf-svc -Dsonar.branch.name=' + env.branch
                    }
                }
            }

            stage('Compile Code')
            {
                sh './mvnw package'
            }

            stage('Deploy Nexus')
            {
                sh './mvnw deploy'
            }
        }
    }
}
