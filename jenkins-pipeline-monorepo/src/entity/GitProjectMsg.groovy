package entity

class GitProjectMsg implements Serializable{
    private String name
    private String description
    private String namespacePath
    private String url

    GitProjectMsg(String name, String description, String namespacePath, String url) {
        this.name = name
        this.description = description
        this.namespacePath = namespacePath
        this.url = url
    }

    GitProjectMsg() {
    }

    String getName() {
        return name
    }

    void setName(String name) {
        this.name = name
    }

    String getDescription() {
        return description
    }

    void setDescription(String description) {
        this.description = description
    }

    String getNamespacePath() {
        return namespacePath
    }

    void setNamespacePath(String namespacePath) {
        this.namespacePath = namespacePath
    }

    String getUrl() {
        return url
    }

    void setUrl(String url) {
        this.url = url
    }
}
