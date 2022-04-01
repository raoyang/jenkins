package enums

/**
 * 部署集群枚举
 */
enum DeployCluster {

    UG_DEV("ug-dev"),
    UG_TEST("ug-test"),
    UG_IDC("ug-idc"),
    UG_PUBLIC("ug-public")

    private String code

    DeployCluster(String code) {
        this.code = code
    }

    String getCode() {
        return code
    }

    void setCode(String code) {
        this.code = code
    }
}