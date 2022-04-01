package enums

enum SonarServer {
    LATEINOS("lateinos", "http://sonarqube.puboa.h3yun.net"),
    ATHENA("athena", "https://sonarqube-monorepo.puboa.h3yun.net"),
    YANGUAN("yanguan", "http://sonar.h3yun.net"),

    private String code
    private String baseUrl

    SonarServer(String code, String baseUrl) {
        this.code = code
        this.baseUrl = baseUrl
    }

    String getCode() {
        return code
    }

    void setCode(String code) {
        this.code = code
    }

    String getBaseUrl() {
        return baseUrl
    }

    void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl
    }

    static SonarServer fromCode(String code) {
        if (LATEINOS.getCode().equalsIgnoreCase(code)) {
            return LATEINOS
        } else if (ATHENA.getCode().equalsIgnoreCase(code)) {
            return ATHENA
        } else if (YANGUAN.getCode().equalsIgnoreCase(code)) {
            return YANGUAN
        } else {
            return null
        }
    }
}