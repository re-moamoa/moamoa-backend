// ====================================================
// Jenkinsfile (Server Module - 효율적인 버전)
// ====================================================
pipeline {
    agent any

    

    environment {
        MODULE_NAME                   = 'moamoa'
        BUILD_ARTIFACT_DIR            = "build/libs"
        DISCORD_WEBHOOK_URL           = credentials('DISCORD_WEBHOOK_URL_CREDENTIAL')
        GITHUB_CREDENTIALS_ID         = 'GITHUB_TOKEN_CREDENTIAL'
        
        // 메인 리포지토리 URL (Moamoa)
        GITHUB_REPOSITORY_URL         = 'https://github.com/HBBHBank/Moamoa'
        
        AWS_REGION                    = 'ap-northeast-2'
        AWS_ACCOUNT_ID                = '968382676238' // <-- 실제 AWS 계정 ID로 변경 필수
        ECR_REPOSITORY                = "${MODULE_NAME}"
        ECR_IMAGE_FULL_URI            = "${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/hbbhbank/${MODULE_NAME}"
        
        // 매니페스트 리포지토리 URL
        GIT_MANIFEST_REPOSITORY_URL   = 'https://github.com/HBBHBank/kube-manifest'
    }

    stages {
        stage('Checkout Main Repository') {
            steps {
                
                echo "[Orchestrator] GitHub 저장소에서 코드 체크아웃 중..."
                checkout([
                    $class: 'GitSCM',
                    branches: [[name: "*/main"]],
                    userRemoteConfigs: [[
                        url: "${GITHUB_REPOSITORY_URL}",
                        credentialsId: "${GITHUB_CREDENTIALS_ID}"
                    ]]
                ])
            }
        }

        stage('Build Module') {
            steps {
                // 'src' 디렉토리 안의 해당 모듈 디렉토리로 이동하여 빌드workspace, not in a 'src' subdirectory. If your structure is 'src/account', please revert to 'src/${env.MODULE_NAME}'.
                    sh "chmod +x gradlew"
                    // 모노레포 루트의 gradlew를 사용하기 위해 'src' 디렉토리로 이동 후 빌드 명령 실행
                    sh "./gradlew clean build -x test"
            }
        }

        stage('Archive Artifacts') {
            steps {
                // 아티팩트 경로는 모듈 디렉토리 내부에 있으므로 경로를 올바르게 지정
                archiveArtifacts artifacts: "${env.MODULE_NAME}/${env.BUILD_ARTIFACT_DIR}/*.jar", // Adjusted path
                                 fingerprint: true
                echo "Artifacts archived: ${env.MODULE_NAME}/${env.BUILD_ARTIFACT_DIR}/*.jar"
            }
        }

        stage('Build and Push Docker Image to ECR') {
            
            steps {
                script {
                    withAWS(credentials: 'AWS_ECR_CREDENTIALS', region: "${env.AWS_REGION}") {
                        sh "aws ecr get-login-password --region ${env.AWS_REGION} | docker login --username AWS --password-stdin ${env.AWS_ACCOUNT_ID}.dkr.ecr.${env.AWS_REGION}.amazonaws.com"
                    }

                    def fullImageName = "${env.ECR_IMAGE_FULL_URI}:${env.BUILD_NUMBER}"
                    def latestImageName = "${env.ECR_IMAGE_FULL_URI}:latest"

                    echo "Building Docker image: ${fullImageName}"
                    // Dockerfile이 모듈 디렉토리 안에 있으므로 해당 디렉토리에서 Docker 빌드 실행
                    dir("${env.MODULE_NAME}") { // Adjusted path
                        sh "docker build -t ${fullImageName} -t ${latestImageName} ."
                    }

                    echo "Pushing Docker image: ${fullImageName} and ${latestImageName}"
                    sh "docker push ${fullImageName}"
                    sh "docker push ${latestImageName}"
                    echo "Docker image pushed to ECR: ${fullImageName}"
                }
            }
        }

        stage('Checkout_to_github_manifest') {
            steps {
                git branch: "main", credentialsId: "${env.GITHUB_CREDENTIALS_ID}", url: "${env.GIT_MANIFEST_REPOSITORY_URL}"
            }
        }

        stage('Update rollout.yaml and Push') {
            steps {
                script {
                    def rolloutFilePath = "base/services/${env.MODULE_NAME}/${env.MODULE_NAME}-deployment.yaml"
                    def newImageTag = "image: ${env.ECR_IMAGE_FULL_URI}:${env.BUILD_NUMBER}"

                    def fileContent = readFile(rolloutFilePath)
                    def modifiedContent = fileContent.readLines().collect { line ->
                        if (line.trim().startsWith("image:")) {
                            return "          ${newImageTag}"
                        } else {
                            return line
                        }
                    }.join("\n")
                    writeFile file: rolloutFilePath, text: modifiedContent

                    withCredentials([usernamePassword(credentialsId: "${env.GITHUB_CREDENTIALS_ID}", usernameVariable: 'GITHUB_USERNAME', passwordVariable: 'GITHUB_TOKEN')]) {
                        sh """
                        git config user.name 'mango0422'
                        git config user.email 'tom990422@gmail.com'
                        git add ${rolloutFilePath}
                        git commit -m 'Update ${env.MODULE_NAME} image version to ${env.BUILD_NUMBER}' || echo '[INFO] Nothing to commit'
                        git push https://${GITHUB_USERNAME}:${GITHUB_TOKEN}@github.com/HBBHBank/kube-manifest.git ${params.BRANCH_NAME}
                        """
                    }
                }
            }
        }
    }

    post {
        success {
            discordSend description: "성공 알림", footer: "${env.MODULE_NAME} 파이프라인이 성공적으로 완료되었습니다.", link: env.BUILD_URL, result: currentBuild.currentResult, title: "[${env.MODULE_NAME}] 빌드 성공", webhookURL: "${env.DISCORD_WEBHOOK_URL}"
        }
        failure {
            discordSend description: "실패 알림", footer: "${env.MODULE_NAME} 파이프라인 빌드 중 오류가 발생했습니다.", link: env.BUILD_URL, result: currentBuild.currentResult, title: "[${env.MODULE_NAME}] 빌드 실패", webhookURL: "${env.DISCORD_WEBHOOK_URL}"
        }
        aborted {
            discordSend description: "중단 알림", footer: "${env.MODULE_NAME} 파이프라인이 중단되었습니다.", link: env.BUILD_URL, result: currentBuild.currentResult, title: "[${env.MODULE_NAME}] 파이프라인 중단", webhookURL: "${env.DISCORD_WEBHOOK_URL}"
        }
        unstable {
            discordSend description: "불안정 알림", footer: "${env.MODULE_NAME} 파이프라인이 불안정합니다. (예: 테스트 실패)", link: env.BUILD_URL, result: currentBuild.currentResult, title: "[${env.MODULE_NAME}] 파이프라인 불안정", webhookURL: "${env.DISCORD_WEBHOOK_URL}"
        }
        always {
            cleanWs()
        }
    }
}