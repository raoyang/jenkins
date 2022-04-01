package pipeline.ugengine

import cmd.DotnetPackageCmd
import entity.Context
import entity.ProjectItemConfig
import enums.DeployCluster
import pipeline.BaseProcess
import pipeline.ugengine.autotest.AutoTestExecuteCMD
import pipeline.ugengine.autotest.UploadReportHtmlCMD
import pipeline.ugengine.sdk.BuildDotnetSDKCMD
import pipeline.ugengine.upgrade.ShardUpgradeCMD

import java.text.SimpleDateFormat

/**
 *  Test分支流程流水线
 */
class TestProcess extends BaseProcess {
    TestProcess(Context context) {
        super(context)
    }

    @Override
    protected careProjects() {
        return [
                UgEngineProjectNames.ENGINE_CORE_ENGINE_DAO,
                UgEngineProjectNames.ENGINE_CORE_METADATA_DAO,
                UgEngineProjectNames.ENGINE_CORE_COLUMNCHANGE_EXECUTOR,
                UgEngineProjectNames.ENGINE_CORE_FILE_OPERATOR,

                UgEngineProjectNames.ENGINE_STATISTICS,

                UgEngineProjectNames.ENGINE_WORKFLOW_SCHEDULER,
                UgEngineProjectNames.ENGINE_WORKFLOW_TASK_EXECUTOR,
                UgEngineProjectNames.ENGINE_WORKFLOW_TASK_EXECUTOR_CHRONOS,
                UgEngineProjectNames.ENGINE_WORKFLOW_WEBAPI,

                UgEngineProjectNames.ENGINE_CLEANER_FILE_CLEANER,

                UgEngineProjectNames.ENGINE_DATACHANGED_HISTORY,
                UgEngineProjectNames.ENGINE_DATACHANGED_DISPATCHER,

                UgEngineProjectNames.ENGINE_HA_CONTROLLER,

                UgEngineProjectNames.ENGINE_TENANT_ALLOCATE_ALGORITHM,
                UgEngineProjectNames.ENGINE_TENANT_ARCHIVE_SCHEDULE,
                UgEngineProjectNames.ENGINE_TENANT_CONFIG_SYNC,
                UgEngineProjectNames.ENGINE_TENANT_DBSCHEMA_INITIAL,
                UgEngineProjectNames.ENGINE_TENANT_DIRECTORY,
                UgEngineProjectNames.ENGINE_TENANT_INACTIVECORP_SCANNER,
                UgEngineProjectNames.ENGINE_TENANT_REGISTER,
                UgEngineProjectNames.ENGINE_TENANT_SQL_IMPORTEXPORT,

                UgEngineProjectNames.ENGINE_SDK_INFRASTRUCTRUE,
                UgEngineProjectNames.ENGINE_SDK_ORG,
                UgEngineProjectNames.ENGINE_SDK_ORM,

                UgEngineProjectNames.ENGINE_APP_INTEGRATION_DISPATCHER,

                UgEngineProjectNames.ENGINE_AUTOTEST,

                UgEngineProjectNames.ENGINE_ADMIN_UPGRADE,
                UgEngineProjectNames.ENGINE_ADMIN_WEBAPI,
                UgEngineProjectNames.ENGINE_ADMIN_WEBAPP
        ]
    }

    @Override
    protected void execute() {
        this.finishedNotifyCmd.addToNotifyUserList(this.latestCommitMsg.committerName)
        /*core*/
        ProjectItemConfig config_engine_dao = context.projectsConfig.get(UgEngineProjectNames.ENGINE_CORE_ENGINE_DAO)
        ProjectItemConfig config_core_metadata_dao = context.projectsConfig.get(UgEngineProjectNames.ENGINE_CORE_METADATA_DAO)
        ProjectItemConfig config_core_column_change_executor = context.projectsConfig.get(UgEngineProjectNames.ENGINE_CORE_COLUMNCHANGE_EXECUTOR)
        ProjectItemConfig config_core_file_operator = context.projectsConfig.get(UgEngineProjectNames.ENGINE_CORE_FILE_OPERATOR)

        /*statistics*/
        ProjectItemConfig config_statistics = context.projectsConfig.get(UgEngineProjectNames.ENGINE_STATISTICS)
        ProjectItemConfig config_statistics_active_corp_collector = context.projectsConfig.get(UgEngineProjectNames.ENGINE_STATISTICS_ACTIVE_CORP_COLLECTOR)

        /*workflow*/
        ProjectItemConfig config_workflow_scheduler = context.projectsConfig.get(UgEngineProjectNames.ENGINE_WORKFLOW_SCHEDULER)
        ProjectItemConfig config_workflow_task_executor = context.projectsConfig.get(UgEngineProjectNames.ENGINE_WORKFLOW_TASK_EXECUTOR)
        ProjectItemConfig config_workflow_task_executor_chronos = context.projectsConfig.get(UgEngineProjectNames.ENGINE_WORKFLOW_TASK_EXECUTOR_CHRONOS)
        ProjectItemConfig config_workflow_webapi = context.projectsConfig.get(UgEngineProjectNames.ENGINE_WORKFLOW_WEBAPI)

        /*cleaner*/
        ProjectItemConfig config_file_cleaner = context.projectsConfig.get(UgEngineProjectNames.ENGINE_CLEANER_FILE_CLEANER)

        /* datachanged */
        ProjectItemConfig config_datachanged_history = context.projectsConfig.get(UgEngineProjectNames.ENGINE_DATACHANGED_HISTORY)
        ProjectItemConfig config_datachanged_dispatcher = context.projectsConfig.get(UgEngineProjectNames.getENGINE_DATACHANGED_DISPATCHER())

        /*ha*/
        ProjectItemConfig config_ha_controller = context.projectsConfig.get(UgEngineProjectNames.ENGINE_HA_CONTROLLER)

        /*multi-tenant*/
        ProjectItemConfig config_tenant_allocate_algorithm = context.projectsConfig.get(UgEngineProjectNames.ENGINE_TENANT_ALLOCATE_ALGORITHM)
        ProjectItemConfig config_tenant_archive_schedule = context.projectsConfig.get(UgEngineProjectNames.ENGINE_TENANT_ARCHIVE_SCHEDULE)
        ProjectItemConfig config_tenant_config_sync = context.projectsConfig.get(UgEngineProjectNames.ENGINE_TENANT_CONFIG_SYNC)
        ProjectItemConfig config_tenant_dbschema_initial = context.projectsConfig.get(UgEngineProjectNames.ENGINE_TENANT_DBSCHEMA_INITIAL)
        ProjectItemConfig config_tenant_directory = context.projectsConfig.get(UgEngineProjectNames.ENGINE_TENANT_DIRECTORY)
        ProjectItemConfig config_tenant_inactivecorp_scanner = context.projectsConfig.get(UgEngineProjectNames.ENGINE_TENANT_INACTIVECORP_SCANNER)
        ProjectItemConfig config_tenant_register = context.projectsConfig.get(UgEngineProjectNames.ENGINE_TENANT_REGISTER)
        ProjectItemConfig config_tenant_sql_importexprot = context.projectsConfig.get(UgEngineProjectNames.ENGINE_TENANT_SQL_IMPORTEXPORT)

        /*sdk*/
        ProjectItemConfig config_engine_infrastructure = this.context.projectsConfig.get(UgEngineProjectNames.ENGINE_SDK_INFRASTRUCTRUE)
        ProjectItemConfig config_org_sdk = this.context.projectsConfig.get(UgEngineProjectNames.ENGINE_SDK_ORG)
        ProjectItemConfig config_orm_sdk = this.context.projectsConfig.get(UgEngineProjectNames.ENGINE_SDK_ORM)

        /*app-integration*/
        ProjectItemConfig config_app_integration_dispatcher = this.context.projectsConfig.get(UgEngineProjectNames.ENGINE_APP_INTEGRATION_DISPATCHER)

        /*autotest*/
        ProjectItemConfig config_autotest_job = this.context.projectsConfig.get(UgEngineProjectNames.ENGINE_AUTOTEST)

        /*engine-admin*/
        ProjectItemConfig config_admin_webapi = this.context.projectsConfig.get(UgEngineProjectNames.ENGINE_ADMIN_WEBAPI)
        ProjectItemConfig config_admin_webapp = this.context.projectsConfig.get(UgEngineProjectNames.ENGINE_ADMIN_WEBAPP)
        ProjectItemConfig config_admin_upgrade = this.context.projectsConfig.get(UgEngineProjectNames.ENGINE_ADMIN_UPGRADE)

        this.finishedNotifyCmd.addToNotifyUserList(config_datachanged_history.projectMaintainer)

        this.context.jenkins.stage("配置检查") {
            /*core*/
            this.projectConfigCheck(config_core_metadata_dao, DeployCluster.UG_TEST)
            this.projectConfigCheck(config_core_column_change_executor, DeployCluster.UG_TEST)
            this.projectConfigCheck(config_core_file_operator, DeployCluster.UG_TEST)

            /*statistics*/
            this.projectConfigCheck(config_statistics, DeployCluster.UG_TEST)

            /*workflow*/
            this.projectConfigCheck(config_workflow_scheduler, DeployCluster.UG_TEST)
            this.projectConfigCheck(config_workflow_task_executor, DeployCluster.UG_TEST)
            this.projectConfigCheck(config_workflow_task_executor_chronos, DeployCluster.UG_TEST)
            this.projectConfigCheck(config_workflow_webapi, DeployCluster.UG_TEST)

            /*cleaner*/
            this.projectConfigCheck(config_file_cleaner, DeployCluster.UG_TEST)

            /* datachanged */
            this.projectConfigCheck(config_datachanged_history, DeployCluster.UG_TEST)
            this.projectConfigCheck(config_datachanged_dispatcher, DeployCluster.UG_TEST)

            /*ha*/
            this.projectConfigCheck(config_ha_controller, DeployCluster.UG_TEST)

            /*multi-tenant*/
            this.projectConfigCheck(config_tenant_allocate_algorithm, DeployCluster.UG_TEST)
            this.projectConfigCheck(config_tenant_archive_schedule, DeployCluster.UG_TEST)
            this.projectConfigCheck(config_tenant_config_sync, DeployCluster.UG_TEST)
            this.projectConfigCheck(config_tenant_dbschema_initial, DeployCluster.UG_TEST)
            this.projectConfigCheck(config_tenant_directory, DeployCluster.UG_TEST)
            this.projectConfigCheck(config_tenant_inactivecorp_scanner, DeployCluster.UG_TEST)
            this.projectConfigCheck(config_tenant_register, DeployCluster.UG_TEST)
            this.projectConfigCheck(config_tenant_sql_importexprot, DeployCluster.UG_TEST)

            /*app-integration*/
            this.projectConfigCheck(config_app_integration_dispatcher, DeployCluster.UG_TEST)

            /*autotest*/
            this.projectConfigCheck(config_autotest_job, DeployCluster.UG_TEST)

            /*engine-admin*/
            this.projectConfigCheck(config_admin_webapi, DeployCluster.UG_TEST)
            this.projectConfigCheck(config_admin_webapp, DeployCluster.UG_TEST)
            this.projectConfigCheck(config_admin_upgrade, DeployCluster.UG_TEST)
        }

        this.context.jenkins.stage("代码扫描") {
            /*core*/
            sonarCheck(config_core_metadata_dao)
            sonarCheck(config_core_column_change_executor)
            sonarCheck(config_core_file_operator)

            /*statistics*/
            //sonarCheck(config_statistics)

            /*workflow*/
            sonarCheck(config_workflow_scheduler)
            sonarCheck(config_workflow_task_executor)
            sonarCheck(config_workflow_task_executor_chronos)
            sonarCheck(config_workflow_webapi)

            /*cleaner*/
            sonarCheck(config_file_cleaner)

            /* datachanged */
            sonarCheck(config_datachanged_history)
            sonarCheck(config_datachanged_dispatcher)

            /*ha*/
            sonarCheck(config_ha_controller)

            /*multi-tenant*/
            sonarCheck(config_tenant_allocate_algorithm)
            sonarCheck(config_tenant_archive_schedule)
            sonarCheck(config_tenant_config_sync)
            sonarCheck(config_tenant_dbschema_initial)
            sonarCheck(config_tenant_directory)
            sonarCheck(config_tenant_inactivecorp_scanner)
            sonarCheck(config_tenant_register)
            sonarCheck(config_tenant_sql_importexprot)

            /*app-integration*/
            this.sonarCheck(config_app_integration_dispatcher)

            /*autotest*/
            //this.sonarCheck(config_autotest_job)

            /*engine-admin*/
            this.sonarCheck(config_admin_webapi)
            this.sonarCheck(config_admin_webapp)
            this.sonarCheck(config_admin_upgrade)
        }

        this.context.jenkins.stage('编译SDK') {
            String version = new SimpleDateFormat("yyyy.MMddHHmm.")
                    .format(new Date(this.context.jenkins.currentBuild.startTimeInMillis))
            version += this.context.jenkins.env.BUILD_NUMBER

            buildDotnetSdk(config_engine_infrastructure, version)
            //buildSdk(config_org_sdk, version)
            buildDotnetSdk(config_orm_sdk, version)
        }
        /*core*/
        String image_engine_dao = buildAppProjectImage(config_engine_dao)
        String image_core_metadata_dao = ""
        String image_core_column_change_executor = ""
        String image_core_file_operator = ""

        /*statistics*/
        String image_statistics = ""
        String image_statistics_active_corp_collector = ""

        /*workflow*/
        String image_workflow_scheduler = ""
        String image_workflow_task_executor = ""
        String image_workflow_task_executor_chronos = ""
        String image_workflow_webapi = ""

        /*cleaner*/
        String image_file_cleaner = ""

        /*datachanged*/
        String image_datachanged_history = ""
        String image_datachanged_dispatcher = ""

        /*ha*/
        String image_ha_controller = ""

        /*multi-tenant*/
        String image_tenant_allocate_algorithm = ""
        String image_tenant_archive_schedule = ""
        String image_tenant_config_sync = ""
        String image_tenant_dbschema_initial = ""
        String image_tenant_directory = ""
        String image_tenant_inactivecorp_scanner = ""
        String image_tenant_register = ""
        String image_tenant_sql_importexprot = ""

        /*app-integration*/
        String image_app_integration_dispatcher = ""

        /*autotest*/
        //String image_autotest_job = ""

        /*engine-admin*/
        String image_admin_webapi = ""
        String image_admin_webapp = ""
        String image_admin_upgrade = ""

        this.context.jenkins.stage("编译镜像") {
            /*core*/
            image_core_metadata_dao = this.buildAppProjectInner(config_core_metadata_dao)
            image_core_column_change_executor = this.buildAppProjectInner(config_core_column_change_executor)
            image_core_file_operator = this.buildAppProjectInner(config_core_file_operator)

            /*statistics*/
            image_statistics = this.buildAppProjectInner(config_statistics)
            //image_statistics_active_corp_collector = this.buildAppProjectInner(config_statistics_active_corp_collector)

            /*workflow*/
            image_workflow_scheduler = this.buildAppProjectInner(config_workflow_scheduler)
            image_workflow_task_executor = this.buildAppProjectInner(config_workflow_task_executor)
            image_workflow_task_executor_chronos = this.buildAppProjectInner(config_workflow_task_executor_chronos)
            image_workflow_webapi = this.buildAppProjectInner(config_workflow_webapi)

            /*cleaner*/
            image_file_cleaner = this.buildAppProjectInner(config_file_cleaner)

            /* datachanged */
            image_datachanged_history = this.buildAppProjectInner(config_datachanged_history)
            image_datachanged_dispatcher = this.buildAppProjectInner(config_datachanged_dispatcher)

            /*ha*/
            image_ha_controller = this.buildAppProjectInner(config_ha_controller)

            /*multi-tenant*/
            image_tenant_allocate_algorithm = this.buildAppProjectInner(config_tenant_allocate_algorithm)
            image_tenant_archive_schedule = this.buildAppProjectInner(config_tenant_archive_schedule)
            image_tenant_config_sync = this.buildAppProjectInner(config_tenant_config_sync)
            image_tenant_dbschema_initial = this.buildAppProjectInner(config_tenant_dbschema_initial)
            image_tenant_directory = this.buildAppProjectInner(config_tenant_directory)
            image_tenant_inactivecorp_scanner = this.buildAppProjectInner(config_tenant_inactivecorp_scanner)
            image_tenant_register = this.buildAppProjectInner(config_tenant_register)
            image_tenant_sql_importexprot = this.buildAppProjectInner(config_tenant_sql_importexprot)

            /*app-integration*/
            image_app_integration_dispatcher = this.buildAppProjectInner(config_app_integration_dispatcher)

            /*autotest*/
            //image_autotest_job = this.buildAppProjectInner(config_autotest_job)

            /*engine-admin*/
            image_admin_webapi = this.buildAppProjectInner(config_admin_webapi)
            image_admin_webapp = this.buildAppProjectInner(config_admin_webapp)
            image_admin_upgrade = this.buildAppProjectInner(config_admin_upgrade)
        }

        if(image_engine_dao.length() > 0){
            this.context.jenkins.echo "h3yun-engine-dao image url : ${image_engine_dao}"
            deployAppProject_Engine_Shard_BySerial(config_engine_dao, image_engine_dao)
        }
        this.context.jenkins.stage("部署") {
            /*core*/
            deployProject(config_core_metadata_dao, image_core_metadata_dao)
            deployProject(config_core_column_change_executor, image_core_column_change_executor)
            deployProject(config_core_file_operator, image_core_file_operator)

            /*statistics*/
            deployProject(config_statistics, image_statistics)
            //deployProject(config_statistics_active_corp_collector, image_statistics_active_corp_collector)

            /*workflow*/
            deployProject(config_workflow_webapi, image_workflow_webapi)
            deployProject(config_workflow_scheduler, image_workflow_scheduler)
            deployProject(config_workflow_task_executor, image_workflow_task_executor)
            deployProject(config_workflow_task_executor_chronos, image_workflow_task_executor_chronos)

            /*cleaner*/
            //deployProject(config_file_cleaner, image_file_cleaner)

            /*datachanged*/
            deployProject(config_datachanged_history, image_datachanged_history)
            deployProject(config_datachanged_dispatcher, image_datachanged_dispatcher)

            /*ha*/
            deployProject(config_ha_controller, image_ha_controller)

            /*multi-tenant*/
            deployProject(config_tenant_allocate_algorithm, image_tenant_allocate_algorithm)
            deployProject(config_tenant_archive_schedule, image_tenant_archive_schedule)
            deployProject(config_tenant_config_sync, image_tenant_config_sync)
            deployProject(config_tenant_dbschema_initial, image_tenant_dbschema_initial)
            deployProject(config_tenant_directory, image_tenant_directory)
            deployProject(config_tenant_inactivecorp_scanner, image_tenant_inactivecorp_scanner)
            deployProject(config_tenant_register, image_tenant_register)
            deployProject(config_tenant_sql_importexprot, image_tenant_sql_importexprot)

            /*app-integration*/
            deployProject(config_app_integration_dispatcher, image_app_integration_dispatcher)

            /*autotest*/
            //deployProject(config_autotest_job, image_admin_upgrade)

            /*engine-admin*/
            deployProject(config_admin_upgrade, image_admin_upgrade)
            deployProject(config_admin_webapi, image_admin_webapi)
            deployProject(config_admin_webapp, image_admin_webapp)
        }

        String execute_autotest_result
        this.context.jenkins.stage("执行自动化测试") {
            AutoTestExecuteCMD test_executor = new AutoTestExecuteCMD(context)
            execute_autotest_result = test_executor.setCluster(DeployCluster.UG_TEST)
                    .setConfig_autotest(config_autotest_job)
                    .setProjectName(config_autotest_job.projectName)
                    .setRunEnv("test")
                    .execute()
                    .getResult()
            this.context.jenkins.echo "自动化测试结果：${execute_autotest_result}"
            String reportHtml = uploadReportHtml(config_autotest_job)
            finishedNotifyCmd.buttonUrl["自动化测试报告"] = reportHtml
        }
        if(execute_autotest_result == "1") {
            // TODO 回滚
        }
    }

    /**
     * 编译一个应用镜像
     * @param itemConfig
     * @return 编译的Docker的镜像地址
     */
    protected String buildAppProjectInner(ProjectItemConfig itemConfig) {
        if(!itemConfig.needBuild){
            return ""
        }
        this.context.jenkins.echo "开始编译镜像${itemConfig.projectName}..."
        return this.buildAppProject(itemConfig)
    }

    private void deployProject(ProjectItemConfig projectConfig, String projectImage){
        if(!projectConfig.needBuild){
            return
        }
        this.context.jenkins.echo "开始部署${projectConfig.projectName}..."
        this.deployAppProjectYaml(projectConfig
                , projectImage
                , "test"
                , "h3yun-engine")
    }

    private void buildDotnetSdk(ProjectItemConfig config_engine_sdk, String version){
        if(!config_engine_sdk.needBuild){
            return
        }
        finishedNotifyCmd.addToNotifyUserList(config_engine_sdk.projectMaintainer)
        BuildDotnetSDKCMD buildDotnetSDKCMD = new BuildDotnetSDKCMD(this.context)
        buildDotnetSDKCMD.setConfig_sdk(config_engine_sdk)
                .setNeedPush(false)
                .execute()
    }

    private String uploadReportHtml(ProjectItemConfig config_autotest){
        UploadReportHtmlCMD uploadReportHtmlCMD = new UploadReportHtmlCMD(this.context)
        return uploadReportHtmlCMD.setProjectName(config_autotest.projectName)
                .execute().getResult()
    }

    //编译镜像
    private String buildAppProjectImage(ProjectItemConfig config){
        String image_url = ""
        if(!config.needBuild){
            return image_url
        }
        this.context.jenkins.stage("编译镜像${config.projectName}") {
            image_url = this.buildAppProject(config)
        }
        return image_url
    }

    private def deployAppProject_Engine_Shard_BySerial(ProjectItemConfig config, String image_url) {
        if(image_url.length() == 0){
            return null
        }
        String[] shrads = ["shard1","shard2"]
        for (String shard : shrads) {
            deployAppProject_Engine_Shard(shard, config, image_url)
        }
    }
    private def deployAppProject_Engine_Shard(String shardKey, ProjectItemConfig config, String image_url){
        if(image_url.length() == 0){
            return null
        }
        this.context.jenkins.stage("部署${config.projectName}-${shardKey}"){
            //执行升级
            ShardUpgradeCMD upgradeCMD = new ShardUpgradeCMD(context)
            //获取镜像版本
            String version = upgradeCMD.get_image_version(image_url)
            def upgradeResult = upgradeCMD.setImage(image_url)
                    .setShardKey(shardKey)
                    .setVersion(version)
                    .setDescription("流水线更新")
                    .setClusterName(DeployCluster.UG_TEST.getCode())
                    .execute().getResult()
            return upgradeResult
        }
    }
}