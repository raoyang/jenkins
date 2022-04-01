package pipeline.ugengine

import entity.Context
import entity.ProjectItemConfig
import enums.DeployCluster
import pipeline.BaseProcess

class DevMultiTenantProcess extends BaseProcess {
    DevMultiTenantProcess(Context context) {
        super(context)
    }

    @Override
    protected careProjects() {
        return [UgEngineProjectNames.ENGINE_TENANT_ALLOCATE_ALGORITHM,
                UgEngineProjectNames.ENGINE_TENANT_ARCHIVE_SCHEDULE,
                UgEngineProjectNames.ENGINE_TENANT_CONFIG_SYNC,
                UgEngineProjectNames.ENGINE_TENANT_DBSCHEMA_INITIAL,
                UgEngineProjectNames.ENGINE_TENANT_DIRECTORY,
                UgEngineProjectNames.ENGINE_TENANT_INACTIVECORP_SCANNER,
                UgEngineProjectNames.ENGINE_TENANT_REGISTER,
                UgEngineProjectNames.ENGINE_TENANT_SQL_IMPORTEXPORT]
    }

    @Override
    protected void execute() {
        this.finishedNotifyCmd.addToNotifyUserList(this.latestCommitMsg.committerName)

        ProjectItemConfig config_tenant_allocate_algorithm = context.projectsConfig.get(UgEngineProjectNames.ENGINE_TENANT_ALLOCATE_ALGORITHM)
        ProjectItemConfig config_tenant_archive_schedule = context.projectsConfig.get(UgEngineProjectNames.ENGINE_TENANT_ARCHIVE_SCHEDULE)
        ProjectItemConfig config_tenant_config_sync = context.projectsConfig.get(UgEngineProjectNames.ENGINE_TENANT_CONFIG_SYNC)
        ProjectItemConfig config_tenant_dbschema_initial = context.projectsConfig.get(UgEngineProjectNames.ENGINE_TENANT_DBSCHEMA_INITIAL)
        ProjectItemConfig config_tenant_directory = context.projectsConfig.get(UgEngineProjectNames.ENGINE_TENANT_DIRECTORY)
        ProjectItemConfig config_tenant_inactivecorp_scanner = context.projectsConfig.get(UgEngineProjectNames.ENGINE_TENANT_INACTIVECORP_SCANNER)
        ProjectItemConfig config_tenant_register = context.projectsConfig.get(UgEngineProjectNames.ENGINE_TENANT_REGISTER)
        ProjectItemConfig config_tenant_sql_importexprot = context.projectsConfig.get(UgEngineProjectNames.ENGINE_TENANT_SQL_IMPORTEXPORT)


        this.finishedNotifyCmd.addToNotifyUserList(config_tenant_allocate_algorithm.projectMaintainer)
        this.finishedNotifyCmd.addToNotifyUserList(config_tenant_archive_schedule.projectMaintainer)
        this.finishedNotifyCmd.addToNotifyUserList(config_tenant_config_sync.projectMaintainer)
        this.finishedNotifyCmd.addToNotifyUserList(config_tenant_dbschema_initial.projectMaintainer)
        this.finishedNotifyCmd.addToNotifyUserList(config_tenant_directory.projectMaintainer)
        this.finishedNotifyCmd.addToNotifyUserList(config_tenant_inactivecorp_scanner.projectMaintainer)
        this.finishedNotifyCmd.addToNotifyUserList(config_tenant_register.projectMaintainer)
        this.finishedNotifyCmd.addToNotifyUserList(config_tenant_sql_importexprot.projectMaintainer)

        this.context.jenkins.stage("配置检查") {
            this.projectConfigCheck(config_tenant_allocate_algorithm, DeployCluster.UG_DEV)
            this.projectConfigCheck(config_tenant_archive_schedule, DeployCluster.UG_DEV)
            this.projectConfigCheck(config_tenant_config_sync, DeployCluster.UG_DEV)
            this.projectConfigCheck(config_tenant_dbschema_initial, DeployCluster.UG_DEV)
            this.projectConfigCheck(config_tenant_directory, DeployCluster.UG_DEV)
            this.projectConfigCheck(config_tenant_inactivecorp_scanner, DeployCluster.UG_DEV)
            this.projectConfigCheck(config_tenant_register, DeployCluster.UG_DEV)
            this.projectConfigCheck(config_tenant_sql_importexprot, DeployCluster.UG_DEV)
        }

        this.context.jenkins.stage("代码扫描") {
            sonarCheck(config_tenant_allocate_algorithm)
            sonarCheck(config_tenant_archive_schedule)
            sonarCheck(config_tenant_config_sync)
            sonarCheck(config_tenant_dbschema_initial)
            sonarCheck(config_tenant_directory)
            sonarCheck(config_tenant_inactivecorp_scanner)
            sonarCheck(config_tenant_register)
            sonarCheck(config_tenant_sql_importexprot)
        }

        String image_tenant_allocate_algorithm = ""
        String image_tenant_archive_schedule = ""
        String image_tenant_config_sync = ""
        String image_tenant_dbschema_initial = ""
        String image_tenant_directory = ""
        String image_tenant_inactivecorp_scanner = ""
        String image_tenant_register = ""
        String image_tenant_sql_importexprot = ""

        this.context.jenkins.stage("编译镜像") {
            image_tenant_allocate_algorithm = this.buildAppProjectInner(config_tenant_allocate_algorithm)
            image_tenant_archive_schedule = this.buildAppProjectInner(config_tenant_archive_schedule)
            image_tenant_config_sync = this.buildAppProjectInner(config_tenant_config_sync)
            image_tenant_dbschema_initial = this.buildAppProjectInner(config_tenant_dbschema_initial)
            image_tenant_directory = this.buildAppProjectInner(config_tenant_directory)
            image_tenant_inactivecorp_scanner = this.buildAppProjectInner(config_tenant_inactivecorp_scanner)
            image_tenant_register = this.buildAppProjectInner(config_tenant_register)
            image_tenant_sql_importexprot = this.buildAppProjectInner(config_tenant_sql_importexprot)
        }

        this.context.jenkins.stage("部署") {
            deployProject(config_tenant_allocate_algorithm, image_tenant_allocate_algorithm)
            deployProject(config_tenant_archive_schedule, image_tenant_archive_schedule)
            deployProject(config_tenant_config_sync, image_tenant_config_sync)
            deployProject(config_tenant_dbschema_initial, image_tenant_dbschema_initial)
            deployProject(config_tenant_directory, image_tenant_directory)
            deployProject(config_tenant_inactivecorp_scanner, image_tenant_inactivecorp_scanner)
            deployProject(config_tenant_register, image_tenant_register)
            deployProject(config_tenant_sql_importexprot, image_tenant_sql_importexprot)
        }
    }

    private String buildAppProjectInner(ProjectItemConfig config){
        String image_url = ""
        if(!config.needBuild) {
            return image_url
        }
        this.context.jenkins.stage("编译镜像${config.projectName}") {
            image_url = this.buildAppProject(config)
        }
        return image_url
    }

    private void deployProject(ProjectItemConfig projectConfig, String projectImage){
        if(!projectConfig.needBuild) {
            return
        }
        this.context.jenkins.echo "开始部署${projectConfig.projectName}..."
        this.deployAppProjectYaml(projectConfig
                , projectImage
                , "dev"
                , "h3yun-engine")
    }
}
