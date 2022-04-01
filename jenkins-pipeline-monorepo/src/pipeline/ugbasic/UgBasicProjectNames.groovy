package pipeline.ugbasic

/**
 * @author lijq* @date 2022/3/9
 */
class UgBasicProjectNames {

    // CORP
    static final String ORG_DAO =  "h3yun-org-dao"
    static final String CORP_DAO =  "h3yun-corp-dao"
    static final String CONTACTS_WEBAPI =  "h3yun-contacts-webapi"
    static final String CORP_INTEGRATE_WEBAPI =  "h3yun-corp-integrate-webapi"
    static final String CORP_INVITATION_DAO =  "h3yun-corp-invitation-dao"
    static final String CORP_REGISTER =  "h3yun-corp-register"
    static final String ORG_SYNCHRONIZER =  "h3yun-org-synchronizer"
    static final String ORG_WEBAPI =  "h3yun-org-webapi"
    static final String DING_ENTRY =  "ug-ding-entry"
    static final String DING_ENTRY_RATE_LIMITER =  "ug-ding-entry-ratelimiter"
    static final String ENTRY_CONFIG =  "ug-entry-config"
    static final String FEATURE_GATES_DAO =  "ug-feature-gates-dao"
    static final String PROFILE_PICTURE_COMPRESSOR =  "ug-profile-picture-compressor"
    static final String WECHAT_OFFICIAL_ENTRY =  "ug-wechat-official-entry"
    static final String WECOM_ENTRY =  "ug-wecom-entry"

    static final String[] SKIP_SONAR_CHECK = [
            "h3yun-org-dao"
    ]

    static final String[] UG_CORP_PROJECT_NAMES = [
        "h3yun-contacts-webapi",
        "h3yun-org-dao",
        "h3yun-corp-dao",
        "h3yun-corp-integrate-webapi",
        "h3yun-corp-invitation-dao",
        "h3yun-corp-register",
        "h3yun-org-synchronizer",
        "h3yun-org-webapi",
        "ug-ding-entry",
        "ug-entry-config",
        "ug-feature-gates-dao",
        "ug-profile-picture-compressor",
        "ug-wechat-official-entry"
    ]

    static final String[] UG_USER_PROJECT_NAMES = [
        "h3yun-user-dao",
        "h3yun-token-dao",
        "h3yun-user-captcha",
        "h3yun-idaas-webapi",
        "h3yun-user-webapi",
        "h3yun-user-register",
        "ug-token-dao",
    ]

    static final String[] UG_NOTICE_PROJECT_NAMES = [
        "h3yun-app-letter-dao",
        "h3yun-app-letter-webapi",
        "h3yun-email",
        "h3yun-notice-daemon",
        "h3yun-shortmsg",
        "ug-thirdparty-msgpush"
    ]

    static final String[] UG_INFRA_PROJECT_NAMES = [
            "ug-shortlink-dao"
    ]
}
