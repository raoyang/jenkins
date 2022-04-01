package entity

class LatestCommitMsg implements Serializable{

    private String committerName
    private String committerEmail
    private String authorName
    private String authorEmail
    private String commitHash
    private String shortCommitHash
    private String committerDate
    private String subject
    private String fullMsg

    LatestCommitMsg() {
    }

    String getCommitterName() {
        return committerName
    }

    LatestCommitMsg setCommitterName(String committerName) {
        this.committerName = committerName
        return this
    }

    String getCommitterEmail() {
        return committerEmail
    }

    LatestCommitMsg setCommitterEmail(String committerEmail) {
        this.committerEmail = committerEmail
        return this
    }

    String getAuthorName() {
        return authorName
    }

    LatestCommitMsg setAuthorName(String authorName) {
        this.authorName = authorName
        return this
    }

    String getAuthorEmail() {
        return authorEmail
    }

    LatestCommitMsg setAuthorEmail(String authorEmail) {
        this.authorEmail = authorEmail
        return this
    }

    String getCommitHash() {
        return commitHash
    }

    LatestCommitMsg setCommitHash(String commitHash) {
        this.commitHash = commitHash
        return this
    }

    String getShortCommitHash() {
        return shortCommitHash
    }

    LatestCommitMsg setShortCommitHash(String shortCommitHash) {
        this.shortCommitHash = shortCommitHash
        return this
    }

    String getCommitterDate() {
        return committerDate
    }

    LatestCommitMsg setCommitterDate(String committerDate) {
        this.committerDate = committerDate
        return this
    }

    String getSubject() {
        return subject
    }

    LatestCommitMsg setSubject(String subject) {
        this.subject = subject
        return this
    }

    String getFullMsg() {
        return fullMsg
    }

    LatestCommitMsg setFullMsg(String fullMsg) {
        this.fullMsg = fullMsg
        return this
    }


    @Override
    String toString() {
        return "LatestCommitMsg{" +
                "committerName='" + committerName + '\'' +
                ", committerEmail='" + committerEmail + '\'' +
                ", authorName='" + authorName + '\'' +
                ", authorEmail='" + authorEmail + '\'' +
                ", commitHash='" + commitHash + '\'' +
                ", shortCommitHash='" + shortCommitHash + '\'' +
                ", committerDate='" + committerDate + '\'' +
                ", subject='" + subject + '\'' +
                ", fullMsg='" + fullMsg + '\'' +
                '}'
    }
}
