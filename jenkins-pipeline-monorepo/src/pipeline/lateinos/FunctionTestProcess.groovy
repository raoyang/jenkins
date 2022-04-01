package pipeline.lateinos

import cmd.ConfigValidateCmd
import cmd.EnvDeployImageCmd
import cmd.GenerateDeployYamls
import cmd.SonarCheckCmd
import entity.Context
import entity.ProjectItemConfig
import entity.SonarCheckMsg
import entity.config.ConfigValidateResult
import enums.DeployCluster
import pipeline.AppServicePublisher
import pipeline.BaseProcess

class FunctionTestProcess extends BaseProcess {
    FunctionTestProcess(Context context) {
        super(context)
    }

    @Override
    protected careProjects() {
        return [
                LateinosProjectNames.API_GATEWAY
                , LateinosProjectNames.ALLPACK_DOTNET
        ]
    }

    void appServicePublishTest() {
        ProjectItemConfig controllerAgentConfig = this.context.projectsConfig.get(LateinosProjectNames.CONTROLLER_AGENT)
        controllerAgentConfig.needBuild = true
        String targetControllerAgentImage = ""

        ProjectItemConfig runtimeServerConfig = this.context.projectsConfig.get(LateinosProjectNames.RUNTIME_SERVER)
        String targetRuntimeServerImage = ""

        ProjectItemConfig apiServerConfig = this.context.projectsConfig.get(LateinosProjectNames.API_SERVER)
        String targetApiServerImage = ""

        // 首先确认快速获取预发布环境的镜像
        this.context.jenkins.stage("提取镜像") {
            targetControllerAgentImage = this.getDeployImage(controllerAgentConfig, "dev")
            targetRuntimeServerImage = this.getDeployImage(runtimeServerConfig, "dev")
            targetApiServerImage = this.getDeployImage(apiServerConfig, "dev")
        }

        // 配置检测
        this.context.jenkins.stage("配置检测") {
            this.projectConfigCheck(controllerAgentConfig, DeployCluster.UG_TEST)
            this.projectConfigCheck(runtimeServerConfig, DeployCluster.UG_TEST)
            this.projectConfigCheck(apiServerConfig, DeployCluster.UG_TEST)
        }

        // 发布Controller Agent
        new AppServicePublisher(this.context, this)
                .setItemConfig(controllerAgentConfig)
                .setTargetImage(targetControllerAgentImage)
                .setDefaultReplicas(1)
                .setTargetDeployCluster(DeployCluster.UG_TEST)
                .setTargetDeployEnv("test")
                .setTargetDeployNamespace("lateinos")
                .publish(this.finishedNotifyCmd)
    }

    @Override
    protected void execute() {
        this.finishedNotifyCmd.setTitle("function-test...")

        this.context.jenkins.stage("测试阶段") {
            List<String> projectNames = careProjects()
            for (int index = projectNames.size() - 1; index >= 0; --index) {
                String projectName = projectNames.get(index)
                this.context.jenkins.echo "projectName=${projectName}"
            }
        }

//        this.manualVerification(["wangli"])


//        new GenerateDeployYamls(this.context)
//                .setCodeType("java")
//                .setTargetDir("/tmp/generate-deploy-yamls-test")
//                .setDeployImage("test-image")
//                .setDeployReplicas("2")
//                .setDeployName("lateinos-rt-asynctask-cleaner")
//                .setDeployNamespace("lateinos")
//                .setDeployBaseTemplate("cronjob")
//                .setVariables([ativeDeadlineSeconds: '3600', schedule: '0 4 * * *'])
//                .execute()

//        ProjectItemConfig itemConfig = this.context.projectsConfig.get("lateinos-rt-asynctask-scheduler")

//        this.context.jenkins.echo "deployVaraibels=${itemConfig.deployVariables}"
//        ProjectItemConfig itemConfig = this.context.projectsConfig.get("lateinos-runtime-server")
//
//        EnvDeployImageCmd deployImageCmd = new EnvDeployImageCmd(this.context)
//                .setProjectName(itemConfig.projectName)
//                .setArgocdGitUrl(this.context.globalConfig.defaultArgocdGitUrl)
//                .setDeliveryGroup(this.context.globalConfig.deliveryGroup)
//                .setDeployYamlFileName(itemConfig.deployYamlFileName)
//                .setDeployEnv("dev")
//        String originImage = deployImageCmd.execute().getResult()
//
//        this.context.jenkins.echo "${itemConfig.projectName}'s originImage=${originImage}"

//
//        projectConfigCheck(itemConfig, DeployCluster.UG_TEST)
//        this.context.jenkins.stage("代码扫描") {
//            sonarCheck(itemConfig)
//        }

//        this.context.jenkins.parallel("FirstStage" : {
//            this.context.jenkins.echo "FirstStage..."
//        }, "SecondStage": {
//            this.context.jenkins.echo "SecondStage..."
//
//        })
//
//        String token = new GetGlobalTokenCmd(this.context)
//                .setTokenId("LateinosControllerAgentToken")
//                .execute()
//                .getResult()
//        this.context.jenkins.echo "itemConfig.needBuild=${itemConfig.needBuild}, token=${token}"


//        def parameters = [
//                choices: [ "是", "否"],
//                description: "测试通过选项",
//                name: "TEST_PASSED"
//        ]

//        InputMessageCmd messageCmd = new InputMessageCmd(this.context, this)
//                .setNotifyMsg("开发集成测试环境已经准备完毕，请进行测试验证！！！")
//                .addNotifyUserName(this.context.globalConfig.teamLeader)
//                .setOkBtnText("确定")
//                .addSubmitter(this.context.globalConfig.teamLeader)
//                .addParameter(this.context.jenkins.choice(
//                        name:"TestResultChoice", description: "测试结果", choices:"NO\nYES"))
//                .addParameter(this.context.jenkins.text(
//                        name: "TestResultDescription", description: "测试结果描述"))
//        if (itemConfig.needBuild) {
//            messageCmd.addParameter(this.context.jenkins.booleanParam(
//                    name: "rollback-${itemConfig.projectName}", description: "回滚${itemConfig.projectName}?", defaultValue: false))
//        }
//
//        def testResultInput = messageCmd.execute().getResult()

    }
}
