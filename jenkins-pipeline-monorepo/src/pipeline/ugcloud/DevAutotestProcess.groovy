package pipeline.ugcloud

import entity.Context
import entity.ProjectItemConfig
import enums.DeployCluster
import pipeline.BaseProcess

/**
 * @author lijq* @date 2022/3/26
 */
class DevAutotestProcess extends BaseProcess {

    static final String DEPLOY_ENV =  "dev"
    static final String DEPLOY_NAMESPACE =  "ug-cloud"
    static final DeployCluster DEPLOY_CLUSTER =  DeployCluster.UG_DEV

    DevAutotestProcess(Context context) {
        super(context)
    }

    @Override
    protected careProjects() {
        return [UgCloudProjectNames.QA_AUTOTEST]
    }

    @Override
    protected void execute() {
        this.finishedNotifyCmd.addToNotifyUserList(this.latestCommitMsg.committerName)

        ProjectItemConfig config = context.projectsConfig.get(UgCloudProjectNames.QA_AUTOTEST)
        this.finishedNotifyCmd.addToNotifyUserList(config.projectMaintainer)

        this.context.jenkins.stage("配置检查") {
            this.projectConfigCheck(config, DEPLOY_CLUSTER)
        }

        this.context.jenkins.stage("代码扫描") {
            sonarCheck(config)
        }

        String image = ""
        this.context.jenkins.stage("编译镜像${config.projectName}") {
            image = this.buildAppProject(config)
        }

        this.context.jenkins.stage("部署${config.projectName}") {
            this.deployAppProjectYaml(config
                    , image
                    , DEPLOY_ENV
                    , DEPLOY_NAMESPACE)

//            this.checkAppProjectRunningSuccess(config
//                    , image
//                    , DEPLOY_CLUSTER
//                    , DEPLOY_NAMESPACE)
        }
    }
}
