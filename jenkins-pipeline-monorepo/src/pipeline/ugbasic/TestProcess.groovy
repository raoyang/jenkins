package pipeline.ugbasic

import entity.Context
import entity.ProjectItemConfig
import enums.DeployCluster
import pipeline.BaseProcess
import pipeline.ugbasic.entity.ProjectItemDeployConfig

class TestProcess extends BaseProcess{

    static final String DEPLOY_ENV =  "test"
    static final DeployCluster DEPLOY_CLUSTER = DeployCluster.UG_TEST

    TestProcess(Context context) {
        super(context)
    }

    @Override
    def careProjects() {
        List projectNames = new ArrayList(30);
        projectNames.addAll(UgBasicProjectNames.UG_CORP_PROJECT_NAMES)
        projectNames.addAll(UgBasicProjectNames.UG_NOTICE_PROJECT_NAMES)
        projectNames.addAll(UgBasicProjectNames.UG_USER_PROJECT_NAMES)
        projectNames.addAll(UgBasicProjectNames.UG_INFRA_PROJECT_NAMES)
        return projectNames.toArray()
    }

    @Override
    void execute() {
        Map<String, ProjectItemDeployConfig> configs = new HashMap<>(30)
        addProjects(configs, UgBasicProjectNames.UG_CORP_PROJECT_NAMES, "h3yun-corp")
        addProjects(configs, UgBasicProjectNames.UG_USER_PROJECT_NAMES, "h3yun-user")
        addProjects(configs, UgBasicProjectNames.UG_NOTICE_PROJECT_NAMES, "h3yun-notice")
        addProjects(configs, UgBasicProjectNames.UG_INFRA_PROJECT_NAMES, "ug-basic")

        finishedNotifyCmd.addToNotifyUserList(this.latestCommitMsg.committerName)

        // 通知
        for (Map.Entry<String, ProjectItemDeployConfig> entry : configs.entrySet()) {
            this.finishedNotifyCmd.addToNotifyUserList(entry.getValue().getItemConfig().projectMaintainer)
        }

        this.context.jenkins.stage("配置检查") {
            for (Map.Entry<String, ProjectItemDeployConfig> entry : configs.entrySet()) {
                this.projectConfigCheck(entry.getValue().getItemConfig(), DEPLOY_CLUSTER)
            }
        }

//        this.context.jenkins.stage("代码扫描") {
//            sonarCheck(itemConfig)
//        }

        this.context.jenkins.stage("编译镜像") {
            for (Map.Entry<String, ProjectItemDeployConfig> entry : configs.entrySet()) {
                ProjectItemDeployConfig deployConfig = entry.getValue()
                String image = this.buildAppProject(deployConfig.getItemConfig())
                deployConfig.setImage(image)
            }
        }

        this.context.jenkins.stage("部署") {
            for (Map.Entry<String, ProjectItemDeployConfig> entry : configs.entrySet()) {
                ProjectItemDeployConfig deployConfig = entry.getValue()
                this.deployAppProjectYaml(deployConfig.getItemConfig()
                        , deployConfig.getImage()
                        , deployConfig.getDeployEnv()
                        , deployConfig.getDeployNamespace())
            }
         }
    }

    def addProjects(Map<String, ProjectItemDeployConfig> configs, String[] projectNames, String deployNamespace) {
        for (String projectName: projectNames) {
            ProjectItemConfig itemConfig = this.context.projectsConfig.get(projectName)
            ProjectItemDeployConfig deployConfig = new ProjectItemDeployConfig(itemConfig, DEPLOY_ENV, deployNamespace)
            configs.put(projectName, deployConfig)
        }
    }

}
