package pipeline.ugbasic

import entity.Context
import entity.ProjectItemConfig
import enums.DeployCluster
import pipeline.BaseProcess

abstract class UgBasicBaseProcess extends BaseProcess {

    private String deployEnv
    private DeployCluster deployCluster

    UgBasicBaseProcess(Context context, String deployEnv, DeployCluster deployCluster) {
        super(context)
        this.deployEnv = deployEnv
        this.deployCluster = deployCluster
    }

    protected abstract String[] skipSonarCheckProjects()

    protected abstract String getDeployNamespace()

    private String deployEnv() {
        return deployEnv
    }

    private DeployCluster deployCluster() {
        return deployCluster
    }

    @Override
    void execute() {
        finishedNotifyCmd.addToNotifyUserList(this.latestCommitMsg.committerName)

        Map<String, ProjectItemConfig> itemConfigs = new HashMap<>(10);
        Map<String, String> itemImages = new HashMap<>(10);

        for (String projectName : careProjects()) {
            ProjectItemConfig itemConfig = this.context.projectsConfig.get(projectName)
            itemConfigs.put(projectName, itemConfig)
        }

        for (Map.Entry<String, ProjectItemConfig> entry : itemConfigs.entrySet()) {
            this.finishedNotifyCmd.addToNotifyUserList(entry.getValue().projectMaintainer)
        }

        this.context.jenkins.stage("配置检查") {
            for (Map.Entry<String, ProjectItemConfig> entry : itemConfigs.entrySet()) {
                this.projectConfigCheck(entry.getValue(), deployCluster())
            }
        }

        this.context.jenkins.stage("代码扫描") {
            for (Map.Entry<String, ProjectItemConfig> entry : itemConfigs.entrySet()) {
                if (!skipSonarCheckProjects().contains(entry.getKey())) {
                    sonarCheck(entry.getValue())
                }
            }
        }

        this.context.jenkins.stage("编译镜像") {
            for (Map.Entry<String, ProjectItemConfig> entry : itemConfigs.entrySet()) {
                String image = this.buildAppProject(entry.getValue())
                itemImages.put(entry.getKey(), image)
            }
        }

        this.context.jenkins.stage("部署") {
            for (Map.Entry<String, ProjectItemConfig> entry : itemConfigs.entrySet()) {
                this.deployAppProjectYaml(entry.getValue()
                        , itemImages.get(entry.getKey())
                        , deployEnv()
                        , getDeployNamespace())
            }

//            this.deployAppProjectYaml(contactsWebapiItemConfig
//                    , image
//                    , "dev"
//                    , "h3yun-corp")

//            this.checkAppProjectRunningSuccess(contactsWebapiItemConfig
//                    , image
//                    , DeployCluster.UG_DEV
//                    , "h3yun-corp")
        }
    }

}
