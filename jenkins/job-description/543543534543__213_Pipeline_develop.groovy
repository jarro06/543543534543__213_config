def config = [:]

def srvconf = new data.config_server()
def srv_config = srvconf.get("${JENKINS_URL}")
def job_config = [
        job: [
                name: "543543534543__213_Pipeline_develop"
        ],
        git: [
                branch: "develop"
        ]
]


def gConfig = utilities.Tools.mergeMap(job_config, srv_config)


def scripts = """
def lib = library identifier: 'BizDevOps_JSL@develop', retriever: modernSCM(
  [\$class: 'GitSCMSource',
   remote: 'https://github.developer.allianz.io/JEQP/BizDevOps-JSL.git',
   credentialsId: 'git-token-credentials']) 

def customLib = library identifier: '543543534543__213_JSL@develop', retriever: modernSCM(
  [\$class: 'GitSCMSource',
   remote: 'https://github.developer.allianz.io/jarro06/543543534543__213_lib.git',
   credentialsId: 'git-token-credentials']) 
   
def config = ${utilities.Tools.formatMap(gConfig)}

def jslGeneral    = lib.de.allianz.bdo.pipeline.JSLGeneral.new()
def jslGit        = lib.de.allianz.bdo.pipeline.JSLGit.new()
def jslGhe        = lib.de.allianz.bdo.pipeline.JSLGhe.new()

def jslCustom     = customLib.de.allianz.543543534543__213.new()

def manual_commit_sha

// for questions about this job ask mario akermann/tobias pfeifer from team pipeline

pipeline {
    agent { label "\${config.job.agent}" }

    stages {
        stage('Prepare') {
            steps {
                echo "prepare"
                script {
                    jslGeneral.clean()
                }
            }    
        }
        stage('Checkout') {
            steps {
                echo "checkout"
                script {
                    jslGit.checkout( config, "JEQP", "543543534543__213_Pipeline_develop", "develop")
                }
            }    
        }
        stage('Build') {
            steps {
                echo "Build"
                script {
                    dir ("543543534543__213") {
                        jslCustom.build()
                    }
                }
            }    
        }

        stage('Component Tests') {
            steps {
                echo "Component Tests"
                script {
                    dir ("543543534543__213") {
                        jslCustom.componentTest()
                    }
                }
            }    
        }

        stage('Integration Tests') {
            steps {
                echo "Integration Tests"
                script {
                    dir ("543543534543__213") {
                        jslCustom.integrationTest()
                    }
                }
            }    
        }


        stage('UAT Tests') {
            steps {
                echo "UAT Tests"
                script {
                    dir ("543543534543__213") {
                        jslCustom.uatTest()
                    }
                }
            }    
        }

        stage('Acceptance Tests') {
            steps {
                echo "Acceptance Tests"
                script {
                    dir ("543543534543__213") {
                        jslCustom.acceptanceTest()
                    }
                }
            }    
        }

        stage('Publish Artifacts') {
            steps {
                echo "Publish Artifacts"
                script {
                    dir ("543543534543__213") {
                        jslCustom.publishArtifacts()
                    }
                }
            }    
        }
        stage('Publish Results') {
            steps {
                echo "Publish Results"
                script {
                    dir ("543543534543__213") {
                        junit allowEmptyResults: true, testResults: '**/surefire-reports/TEST-*.xml'
                    }
                }
            }    
        }
    }
}
"""

def job = pipelineJob("${gConfig.job.name}")

job.with {

    definition {
        cps {
            script(scripts)
        }
    }
}  
