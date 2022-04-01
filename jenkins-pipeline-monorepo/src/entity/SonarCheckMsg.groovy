package entity

class SonarCheckMsg implements Serializable {
    private String sonarUrl
    private boolean success = true
    private String errorMsg

    SonarCheckMsg() {
    }

    String getSonarUrl() {
        return sonarUrl
    }

    SonarCheckMsg setSonarUrl(String sonarUrl) {
        this.sonarUrl = sonarUrl
        return this
    }

    boolean getSuccess() {
        return this.success
    }

    SonarCheckMsg setSuccess(boolean success) {
        this.success = success
        return this
    }

    String getErrorMsg() {
        return errorMsg
    }

    SonarCheckMsg setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg
        return this
    }


    @Override
    String toString() {
        return "SonarCheckMsg{" +
                "sonarUrl='" + sonarUrl + '\'' +
                ", success='" + success + '\'' +
                ", errorMsg='" + errorMsg + '\'' +
                '}'
    }
}
