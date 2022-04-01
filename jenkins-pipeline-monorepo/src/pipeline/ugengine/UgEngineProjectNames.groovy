package pipeline.ugengine

class UgEngineProjectNames {
    /* statistics */
    static final String ENGINE_STATISTICS = "ug-engine-statistics"
    static final String ENGINE_STATISTICS_ACTIVE_CORP_COLLECTOR = "ug-engine-activecorp-collector"

    /* datachanged */
    static final String ENGINE_DATACHANGED_HISTORY = "ug-engine-datachanged-history"
    static final String ENGINE_DATACHANGED_DISPATCHER = "ug-engine-datachanged-dispatcher"

    /* cleaner */
    static final String ENGINE_CLEANER_FILE_CLEANER = "h3yun-file-cleaner"

    /* core */
    static final String ENGINE_CORE_ENGINE_DAO = "h3yun-engine-dao"
    static final String ENGINE_CORE_FILE_OPERATOR = "h3yun-engine-file-operator"
    static final String ENGINE_CORE_COLUMNCHANGE_EXECUTOR = "ug-engine-columnchange-executor"
    static final String ENGINE_CORE_METADATA_DAO = "ug-engine-metadata-dao"

    /*workflow*/
    static final String ENGINE_WORKFLOW_SCHEDULER = "h3yun-workflow-scheduler"
    static final String ENGINE_WORKFLOW_TASK_EXECUTOR = "ug-workflow-task-executor"
    static final String ENGINE_WORKFLOW_TASK_EXECUTOR_CHRONOS = "ug-workflow-task-executor-chronos"
    static final String ENGINE_WORKFLOW_WEBAPI = "h3yun-workflow-webapi"

    /*sdk*/
    static final String ENGINE_SDK_INFRASTRUCTRUE = "h3yun-engine-infrastructure"
    static final String ENGINE_SDK_ORM = "h3yun-engine-orm"
    static final String ENGINE_SDK_ORG = "h3yun-org-sdk"

    /*ha*/
    static final String ENGINE_HA_CONTROLLER = "h3yun-engine-controller"
    //static final String ENGINE_HA_TRAFFIC_PROXY = "h3yun-engine-traffic-proxy"

    /*multi-tenant*/
    static final String ENGINE_TENANT_CONFIG_SYNC = "h3yun-engine-config-sync"
    static final String ENGINE_TENANT_DBSCHEMA_INITIAL = "h3yun-engine-dbschema-initial"
    static final String ENGINE_TENANT_DIRECTORY = "h3yun-engine-directory"
    static final String ENGINE_TENANT_REGISTER = "h3yun-engine-register"
    static final String ENGINE_TENANT_ALLOCATE_ALGORITHM = "ug-engine-allocate-algorithm"
    static final String ENGINE_TENANT_ARCHIVE_SCHEDULE = "ug-engine-archive-schedule"
    static final String ENGINE_TENANT_INACTIVECORP_SCANNER = "ug-inactivecorp-scanner"
    static final String ENGINE_TENANT_SQL_IMPORTEXPORT = "ug-sql-importexport"

    /*app-integration*/
    static final String ENGINE_APP_INTEGRATION_DISPATCHER = "h3yun-app-integration-dispatcher"

    /*autotest*/
    static final String ENGINE_AUTOTEST = "ug-engine-autotest"

    /*engine-admin*/
    static final String ENGINE_ADMIN_WEBAPI = "h3yun-engine-admin-webapi"
    static final String ENGINE_ADMIN_WEBAPP = "h3yun-engine-admin-webapp"
    static final String ENGINE_ADMIN_UPGRADE = "h3yun-engine-upgrade"
}